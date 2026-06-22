/*
 * Copyright (c) 2025 TonTech
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.ton.walletkit.browser

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import io.ton.walletkit.ITONWalletKit
import io.ton.walletkit.WebViewTonConnectInjector
import io.ton.walletkit.bridge.BuildConfig
import io.ton.walletkit.bridge.optJsonArray
import io.ton.walletkit.bridge.optJsonObject
import io.ton.walletkit.bridge.optString
import io.ton.walletkit.config.TONWalletKitConfiguration
import io.ton.walletkit.core.TONWalletKit
import io.ton.walletkit.engine.WalletKitEngine
import io.ton.walletkit.internal.constants.BridgeMethodConstants
import io.ton.walletkit.internal.constants.BrowserConstants
import io.ton.walletkit.internal.constants.ResponseConstants
import io.ton.walletkit.internal.util.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.put
import java.lang.ref.WeakReference
import java.util.concurrent.ConcurrentHashMap

/**
 * TonConnect injector for WebViews.
 *
 * This class manages the TonConnect bridge lifecycle, injecting JavaScript code into web pages
 * and automatically routing all requests to the provided TONWalletKit instance.
 *
 * **Automatic Cleanup**: This class automatically cleans up resources when the WebView is
 * detached from the window, preventing memory leaks. You can also call [cleanup] manually
 * if needed (e.g., when reusing a WebView).
 *
 * You typically get an instance of this class by calling `webView.injectTonConnect(walletKit)`.
 */
internal class TonConnectInjector(
    private val webView: WebView,
    private val walletKit: ITONWalletKit,
    private val walletId: String? = null,
) : WebViewTonConnectInjector {
    // Helper to access internal engine - cast to concrete implementation
    private val engine: WalletKitEngine?
        get() = (walletKit as? TONWalletKit)?.engine

    companion object {
        private const val TAG = "TonConnectInjector"
        private const val ERROR_WALLET_ENGINE_NOT_INITIALIZED = "Wallet engine not initialized"
        private const val ERROR_FAILED_PROCESS_REQUEST = "Failed to process request"
        private const val ERROR_CODE_INTERNAL = 500
        private const val ERROR_CODE_FORBIDDEN = 403
        private const val FORBIDDEN_IFRAME_MESSAGE =
            "TonConnect request rejected: the iframe has an opaque origin and is not part of a connected dApp"
        private const val METHOD_SEND = "send"

        // Registry of active WebViews for JS Bridge sessions
        // Maps sessionId -> WeakReference<WebView> to allow garbage collection
        private val activeWebViews = ConcurrentHashMap<String, WeakReference<WebView>>()

        /**
         * Register a WebView for a JS Bridge session.
         * Called internally when a session is created.
         */
        @JvmStatic
        internal fun registerWebView(sessionId: String, webView: WebView) {
            Logger.d(TAG, "Registering WebView for session: $sessionId")
            activeWebViews[sessionId] = WeakReference(webView)
            cleanupStaleReferences()
        }

        /**
         * Unregister a WebView for a JS Bridge session.
         * Called internally when a session is disconnected or WebView is destroyed.
         */
        @JvmStatic
        internal fun unregisterWebView(sessionId: String) {
            Logger.d(TAG, "Unregistering WebView for session: $sessionId")
            activeWebViews.remove(sessionId)
        }

        /**
         * Get the WebView associated with a JS Bridge session.
         * Returns null if the WebView has been garbage collected or was never registered.
         */
        @JvmStatic
        internal fun getWebViewForSession(sessionId: String): WebView? {
            val webViewRef = activeWebViews[sessionId]
            val webView = webViewRef?.get()

            if (webView == null && webViewRef != null) {
                activeWebViews.remove(sessionId)
            }

            return webView
        }

        /**
         * Clean up stale WebView references where the WebView has been garbage collected.
         * Called periodically to prevent map from growing indefinitely.
         */
        @JvmStatic
        private fun cleanupStaleReferences() {
            val staleKeys = activeWebViews.entries
                .filter { it.value.get() == null }
                .map { it.key }

            staleKeys.forEach { activeWebViews.remove(it) }

            if (staleKeys.isNotEmpty()) {
                Logger.d(TAG, "Cleaned up ${staleKeys.size} stale WebView references")
            }
        }

        /**
         * Clear all WebView registrations.
         * Called internally during cleanup.
         */
        @JvmStatic
        internal fun clearAllRegistrations() {
            Logger.d(TAG, "Clearing all WebView registrations")
            activeWebViews.clear()
        }

        /**
         * Broadcast an event to all registered WebViews.
         * Used when sessionId is not available (e.g., wallet-initiated disconnect).
         */
        @JvmStatic
        internal fun broadcastEventToAllWebViews(event: JsonObject) {
            cleanupStaleReferences()

            val webViews = activeWebViews.values.mapNotNull { it.get() }.distinct()

            for (webView in webViews) {
                try {
                    // Use the same tag that's used when attaching injectors
                    val injector = webView.getTag("tonconnect_injector".hashCode()) as? TonConnectInjector
                    if (injector != null) {
                        injector.sendEvent(event)
                    }
                } catch (e: Exception) {
                    Logger.w(TAG, "Failed to send event to WebView", e)
                }
            }
        }
    }

    // Use application context to avoid leaking Activity context
    private val context = webView.context.applicationContext
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val pendingRequests = ConcurrentHashMap<String, PendingRequest>()
    private var isCleanedUp = false

    // Store reference to BridgeInterface for response delivery
    private lateinit var bridgeInterface: BridgeInterface

    // Track the current dApp URL for domain extraction
    @Volatile
    private var currentUrl: String? = null

    /**
     * Set up TonConnect support with default WebViewClient.
     * This will replace any existing WebViewClient and WebChromeClient.
     */
    @SuppressLint("SetJavaScriptEnabled")
    override fun setup() {
        // Bridge communication intake.
        //
        // SECURITY: prefer WebViewCompat.addWebMessageListener over addJavascriptInterface.
        // A @JavascriptInterface method carries NO information about which frame called it,
        // so the bridge previously attributed every request to webView.url (the main-frame
        // URL) — letting any embedded iframe issue connect/sign/transaction calls under the
        // host dApp's identity. A WebMessageListener instead delivers the platform-
        // authenticated sourceOrigin and isMainFrame for the calling frame, which JS cannot
        // forge. Registering it under the same JS object name the bundle already uses
        // (window.<JS_INTERFACE_NAME>.postMessage) makes it a drop-in for request intake;
        // responses are still delivered via postWebMessage and are unaffected.
        bridgeInterface = BridgeInterface(
            onMessage = { json, type -> handleBridgeMessage(json, type, sourceOrigin = null, isMainFrame = true) },
            onError = { error -> Logger.e(TAG, "Bridge error: $error") },
        )

        if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
            WebViewCompat.addWebMessageListener(
                webView,
                BrowserConstants.JS_INTERFACE_NAME,
                setOf("*"),
            ) { _, message, sourceOrigin, isMainFrame, _ ->
                handleSecureBridgeMessage(message.data, sourceOrigin.toString(), isMainFrame)
            }
            Logger.d(TAG, "Bridge intake via WebMessageListener (per-frame origin enforced)")
        } else {
            // Legacy WebView without per-frame origin support: fall back to the origin-blind
            // JS interface. Frame identity is unavailable here, so requests are treated as
            // main-frame (best effort) — these old WebView versions are increasingly rare.
            Logger.w(TAG, "WEB_MESSAGE_LISTENER unsupported — per-frame origin enforcement unavailable")
            webView.addJavascriptInterface(bridgeInterface, BrowserConstants.JS_INTERFACE_NAME)
        }

        // Register WebView with existing sessions (important when app is reopened)
        // This ensures disconnect and other events work after the WebView is recreated
        scope.launch {
            try {
                val sessions = engine?.callBridgeMethod(BridgeMethodConstants.METHOD_LIST_SESSIONS, null)
                val items = sessions?.optJsonArray(ResponseConstants.KEY_ITEMS)
                if (items != null) {
                    for (item in items) {
                        val session = item as? JsonObject ?: continue
                        val sessionId = session.optString(ResponseConstants.KEY_SESSION_ID)
                        if (sessionId.isNotEmpty()) {
                            registerWebView(sessionId, webView)
                        }
                    }
                }
            } catch (e: Exception) {
                Logger.w(TAG, "Failed to query and register existing sessions", e)
            }
        }

        // CRITICAL: Use WebViewCompat.addDocumentStartJavaScript for early injection
        // This is the proper Android API to inject JavaScript before HTML parsing begins
        if (WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            try {
                // Load inject.mjs from assets
                val injectionScript = context.assets.open(BrowserConstants.INJECT_SCRIPT_PATH)
                    .bufferedReader()
                    .use { it.readText() }

                // Build injection options from WalletKit configuration
                val config = (walletKit as? TONWalletKit)?.engine?.getConfiguration()
                val injectOptions = buildInjectOptions(config)

                // Create initialization script that calls window.injectWalletKit(options).
                // The WebMessageListener object (window.<JS_INTERFACE_NAME>) may attach a
                // moment after this document-start script runs, so wait briefly for it before
                // initialising the bridge — otherwise the bundle's transport would not find it.
                val fullScript = """
                    $injectionScript
                    (function () {
                        var init = function () { window.injectWalletKit($injectOptions); };
                        if (typeof ${BrowserConstants.JS_INTERFACE_NAME} !== 'undefined') { init(); return; }
                        var tries = 0;
                        var timer = setInterval(function () {
                            if (typeof ${BrowserConstants.JS_INTERFACE_NAME} !== 'undefined' || tries++ > 50) {
                                clearInterval(timer);
                                init();
                            }
                        }, 2);
                    })();
                """.trimIndent()

                // Allow all origins (*) since this is a wallet browser that loads any dApp
                val allowedOrigins = setOf("*")

                WebViewCompat.addDocumentStartJavaScript(
                    webView,
                    fullScript,
                    allowedOrigins,
                )
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to add document start script", e)
            }
        } else {
            Logger.w(TAG, "DOCUMENT_START_SCRIPT not supported on this Android version")
        }

        // Set custom WebViewClient
        webView.webViewClient = object : WebViewClient() {
            override fun onPageStarted(view: WebView?, url: String?, favicon: Bitmap?) {
                super.onPageStarted(view, url, favicon)
                url?.let {
                    currentUrl = it

                    scope.launch {
                        try {
                            engine?.callBridgeMethod(
                                method = BridgeMethodConstants.METHOD_EMIT_BROWSER_PAGE_STARTED,
                                params = buildJsonObject { put(ResponseConstants.KEY_URL, it) },
                            )
                        } catch (e: Exception) {
                            Logger.w(TAG, "Failed to emit page started event", e)
                        }
                    }
                }
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                url?.let {
                    scope.launch {
                        try {
                            engine?.callBridgeMethod(
                                method = BridgeMethodConstants.METHOD_EMIT_BROWSER_PAGE_FINISHED,
                                params = buildJsonObject { put(ResponseConstants.KEY_URL, it) },
                            )
                        } catch (e: Exception) {
                            Logger.w(TAG, "Failed to emit page finished event", e)
                        }
                    }
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?,
            ) {
                super.onReceivedError(view, request, error)
                val errorMessage = error?.description?.toString() ?: "Unknown error"
                Logger.e(TAG, "WebView error: $errorMessage")

                scope.launch {
                    try {
                        engine?.callBridgeMethod(
                            method = BridgeMethodConstants.METHOD_EMIT_BROWSER_ERROR,
                            params = buildJsonObject { put(ResponseConstants.KEY_MESSAGE, errorMessage) },
                        )
                    } catch (e: Exception) {
                        Logger.w(TAG, "Failed to emit error event", e)
                    }
                }
            }
        }

        // Set custom WebChromeClient for console logging
        webView.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage): Boolean {
                if (BuildConfig.LOG_LEVEL != "OFF") {
                    Logger.d(TAG, "[JS Console] ${consoleMessage.message()} (${consoleMessage.sourceId()}:${consoleMessage.lineNumber()})")
                }
                return true
            }
        }
    }

    /**
     * Send a response back to a dApp for a specific request.
     *
     * @param messageId The ID from the original request
     * @param response The response data to send back
     */
    fun sendResponse(messageId: String, response: JsonObject) {
        val pending = pendingRequests.remove(messageId)
        if (pending == null) {
            Logger.w(TAG, "No pending request found for messageId: $messageId")
            return
        }

        // If this is a successful connect response, register this WebView for the session
        // We need to register with BOTH messageId AND sessionId because:
        // - Responses are tagged with messageId (from the original request)
        // - Events (like disconnect) are tagged with sessionId (from the wallet)
        if (pending.method == BrowserConstants.EVENT_CONNECT && ResponseConstants.KEY_PAYLOAD in response) {
            try {
                // Register with messageId (for immediate responses)
                registerWebView(messageId, webView)

                // CRITICAL: Query the wallet for the newly created session to get its sessionId
                // We do this synchronously so the sessionId is registered BEFORE the browser might close
                scope.launch {
                    try {
                        // Query sessions from the wallet
                        val sessions = engine?.callBridgeMethod(BridgeMethodConstants.METHOD_LIST_SESSIONS, null)

                        // Find the session that was just created for this messageId
                        // The wallet should have created a session during the connect processing
                        val items = sessions?.optJsonArray(ResponseConstants.KEY_ITEMS)
                        if (items != null && items.isNotEmpty()) {
                            // The most recently created session should be ours
                            // Register it with the WebView (heuristic: pick the last one)
                            val lastSession = items.last() as? JsonObject ?: return@launch
                            val sessionId = lastSession.optString(ResponseConstants.KEY_SESSION_ID)
                            if (sessionId.isNotEmpty() && sessionId != messageId) {
                                registerWebView(sessionId, webView)
                            }
                        }
                    } catch (e: Exception) {
                        Logger.w(TAG, "Failed to query sessionId after connect", e)
                    }
                }
            } catch (e: Exception) {
                Logger.w(TAG, "Failed to register WebView for session", e)
            }
        }

        sendResponseToFrame(pending, response)
    }

    /**
     * Broadcast an event to all frames (main page + all iframes).
     *
     * Use this to notify the dApp about wallet state changes (e.g., disconnect).
     *
     * @param event The event data to broadcast
     */
    fun sendEvent(event: JsonObject) {
        // CRITICAL FIX: The event from Engine already has the correct structure:
        // { type: "TONCONNECT_BRIDGE_EVENT", source: "...", event: {...} }
        // Extract the inner event
        val actualEvent = event.optJsonObject(BrowserConstants.KEY_EVENT) ?: event

        // Create the message structure for the event
        val eventMessage = buildJsonObject {
            put(BrowserConstants.KEY_TYPE, BrowserConstants.MESSAGE_TYPE_BRIDGE_EVENT)
            put(BrowserConstants.KEY_EVENT, actualEvent)
        }

        webView.post {
            webView.postWebMessage(
                android.webkit.WebMessage(eventMessage.toString()),
                android.net.Uri.EMPTY,
            )
        }
    }

    /**
     * Restores an existing TonConnect session for the given dApp URL.
     * When a user reopens the browser to a dApp they previously connected to,
     * this sends a "connect" event to restore the UI state.
     */
    /**
     * Clean up resources.
     *
     * This is called when the browser/WebView is closed, but it does NOT unregister
     * the session from the global registry. This allows disconnect events to still
     * be processed even after the browser is closed (the session remains active until
     * explicitly disconnected).
     *
     * Session cleanup happens when:
     * - User explicitly disconnects (removes session from registry)
     * - WebView is garbage collected (WeakReference returns null)
     */
    override fun cleanup() {
        if (isCleanedUp) {
            return // Already cleaned up
        }
        isCleanedUp = true

        Logger.d(TAG, "Cleaning up TonConnect injector (browser closed, but session remains active)")

        // NOTE: We do NOT unregister sessions here! The session should remain registered
        // even after the browser closes, so that disconnect events can still be processed.
        // The WeakReference will automatically return null when the WebView is garbage collected.

        scope.cancel()
        pendingRequests.clear()
    }

    private fun handleBridgeMessage(json: JsonObject, type: String, sourceOrigin: String?, isMainFrame: Boolean) {
        scope.launch {
            when (type) {
                BrowserConstants.MESSAGE_TYPE_BRIDGE_REQUEST -> handleBridgeRequest(json, sourceOrigin, isMainFrame)
                else -> Logger.w(TAG, "Unknown message type: $type")
            }
        }
    }

    /**
     * Entry point for the WebMessageListener transport: carries the platform-authenticated
     * [sourceOrigin] and [isMainFrame] for the frame that posted the message.
     */
    private fun handleSecureBridgeMessage(message: String?, sourceOrigin: String?, isMainFrame: Boolean) {
        val raw = message ?: return
        val json = try {
            Json.parseToJsonElement(raw).jsonObject
        } catch (e: Exception) {
            Logger.e(TAG, "Failed to parse secure bridge message", e)
            return
        }
        val type = json.optString(BrowserConstants.KEY_TYPE)
        handleBridgeMessage(json, type, sourceOrigin, isMainFrame)
    }

    private fun handleBridgeRequest(json: JsonObject, sourceOrigin: String?, isMainFrame: Boolean) {
        val frameId = json.optString(BrowserConstants.KEY_FRAME_ID, BrowserConstants.DEFAULT_FRAME_ID)
        val messageId = json.optString(BrowserConstants.KEY_MESSAGE_ID)
        val method = json.optString(BrowserConstants.KEY_METHOD, BrowserConstants.DEFAULT_METHOD)

        if (messageId.isEmpty()) {
            Logger.e(TAG, "Bridge request missing messageId")
            return
        }

        // SECURITY (per-origin authorization — same model as iOS): a request from ANY frame
        // is allowed, but it is attributed to the platform-authenticated origin of the frame
        // that actually sent it (sourceOrigin) — never to webView.url, which is the main-frame
        // URL for every frame. The wallet's session lookup is keyed by domain, so a frame
        // whose origin matches a connected session (the dApp itself, or a same-origin iframe
        // that is part of it) is authorized, while a cross-origin iframe finds no session for
        // its own origin and is rejected by the wallet.
        val resolvedOrigin: String? = if (isMainFrame) {
            // Main frame: prefer the authenticated origin; fall back to the WebView URL on
            // legacy WebViews where sourceOrigin is unavailable.
            sourceOrigin?.takeIf { it.isNotBlank() && it != "null" } ?: webView.url ?: currentUrl
        } else {
            // Sub-frame: use ONLY its own authenticated origin. Never fall back to webView.url
            // (that would let an iframe inherit the host dApp's identity).
            sourceOrigin?.takeIf { it.isNotBlank() && it != "null" }
        }

        // An opaque-origin sub-frame (data:/sandboxed, or srcdoc with no inherited origin)
        // can never belong to a connected session — reject it up front instead of letting it
        // fall through to the main-frame URL or the "internal-browser" domain.
        if (!isMainFrame && resolvedOrigin == null) {
            Logger.w(TAG, "Blocked TonConnect '$method' from opaque-origin sub-frame (frameId=$frameId)")
            pendingRequests[messageId] = PendingRequest(frameId, messageId, method, System.currentTimeMillis())
            sendResponse(messageId, errorResponse(FORBIDDEN_IFRAME_MESSAGE, ERROR_CODE_FORBIDDEN))
            return
        }

        // Store pending request with frame info
        val pending = PendingRequest(
            frameId = frameId,
            messageId = messageId,
            method = method,
            timestamp = System.currentTimeMillis(),
        )
        pendingRequests[messageId] = pending

        // Get the engine from the provided TONWalletKit instance
        val engine = engine
        if (engine == null) {
            Logger.e(TAG, "WalletKit engine not available!")
            sendResponse(messageId, errorResponse(ERROR_WALLET_ENGINE_NOT_INITIALIZED, ERROR_CODE_INTERNAL))
            return
        }

        // Emit browser bridge request event for UI tracking
        scope.launch {
            try {
                engine.callBridgeMethod(
                    method = BridgeMethodConstants.METHOD_EMIT_BROWSER_BRIDGE_REQUEST,
                    params = buildJsonObject {
                        put(BrowserConstants.KEY_MESSAGE_ID, messageId)
                        put(BrowserConstants.KEY_METHOD, method)
                        put(BrowserConstants.KEY_REQUEST, json.toString())
                    },
                )
            } catch (e: Exception) {
                Logger.w(TAG, "Failed to emit browser bridge request event", e)
            }
        }

        // Forward to TONWalletKit engine - it handles everything internally!
        scope.launch {
            try {
                // Params can be a JsonObject, a JsonArray, or absent. Send the raw JSON
                // string so the engine can parse it back per the TonConnect method contract.
                val paramsJson: String? = json[ResponseConstants.KEY_PARAMS]?.toString()

                // Domain for this request = the per-frame origin resolved above. For the main
                // frame this is its authenticated origin (or webView.url on legacy WebViews);
                // for a sub-frame it is the iframe's own origin, so the wallet's domain-keyed
                // session lookup decides whether that origin is authorized.
                val dAppUrl = resolvedOrigin ?: webView.url ?: currentUrl

                engine.handleTonConnectRequest(
                    messageId = messageId,
                    method = method,
                    paramsJson = paramsJson,
                    url = dAppUrl,
                    responseCallback = { response ->
                        sendResponse(messageId, response)
                    },
                    walletId = walletId,
                )
            } catch (e: Exception) {
                Logger.e(TAG, "Failed to forward request to WalletKit engine", e)
                sendResponse(messageId, errorResponse(e.message ?: ERROR_FAILED_PROCESS_REQUEST, ERROR_CODE_INTERNAL))
            }
        }
    }

    private fun sendResponseToFrame(pending: PendingRequest, response: JsonObject) {
        scope.launch(Dispatchers.Main) {
            deliverResponse(pending, response)
        }
    }

    private fun deliverResponse(pending: PendingRequest, response: JsonObject) {
        val responseJson = buildJsonObject {
            put(BrowserConstants.KEY_TYPE, BrowserConstants.MESSAGE_TYPE_BRIDGE_RESPONSE)
            put(BrowserConstants.KEY_MESSAGE_ID, pending.messageId)
            put(BrowserConstants.KEY_SUCCESS, true)
            put(BrowserConstants.KEY_PAYLOAD, response)
        }

        webView.postWebMessage(
            android.webkit.WebMessage(responseJson.toString()),
            android.net.Uri.EMPTY,
        )
    }

    private fun errorResponse(message: String, code: Int): JsonObject = buildJsonObject {
        put(
            ResponseConstants.KEY_ERROR,
            buildJsonObject {
                put(ResponseConstants.KEY_MESSAGE, message)
                put(ResponseConstants.KEY_CODE, code)
            },
        )
    }

    @Serializable
    private data class InjectOptions(
        val isWalletBrowser: Boolean,
        val deviceInfo: DeviceInfo,
        val walletInfo: WalletInfo,
    )

    @Serializable
    private data class DeviceInfo(
        val platform: String,
        val appName: String,
        val appVersion: String,
        val maxProtocolVersion: Int,
        val features: List<String>,
    )

    @Serializable
    private data class WalletInfo(
        val name: String,
        val app_name: String,
        val about_url: String,
        val image: String,
        val platforms: List<String>,
        val jsBridgeKey: String,
        val injected: Boolean,
        val embedded: Boolean,
        val tondns: String? = null,
        val bridgeUrl: String,
    )

    private fun buildInjectOptions(config: TONWalletKitConfiguration?): String {
        val manifest = config?.walletManifest
        val deviceInfo = config?.deviceInfo

        val features = buildFeaturesList(deviceInfo?.features ?: config?.features)

        val options = InjectOptions(
            isWalletBrowser = true,
            deviceInfo = DeviceInfo(
                platform = deviceInfo?.platform ?: "android",
                appName = deviceInfo?.appName ?: manifest?.appName ?: "TON Wallet",
                appVersion = deviceInfo?.appVersion ?: "1.0.0",
                maxProtocolVersion = deviceInfo?.maxProtocolVersion ?: 2,
                features = features,
            ),
            walletInfo = WalletInfo(
                name = manifest?.name ?: "tonwallet",
                app_name = manifest?.appName ?: "TON Wallet",
                about_url = manifest?.aboutUrl ?: "",
                image = manifest?.imageUrl ?: "",
                platforms = listOf("android"),
                jsBridgeKey = manifest?.jsBridgeKey ?: manifest?.appName ?: "tonwallet",
                injected = true,
                embedded = true,
                tondns = manifest?.tondns,
                bridgeUrl = manifest?.bridgeUrl ?: "",
            ),
        )

        return Json.encodeToString(options)
    }

    private fun buildFeaturesList(features: List<TONWalletKitConfiguration.Feature>?): List<String> {
        if (features.isNullOrEmpty()) return listOf("SendTransaction")

        val result = mutableListOf<String>()
        for (feature in features) {
            when (feature) {
                is TONWalletKitConfiguration.SendTransactionFeature -> {
                    if (feature.maxMessages != null) {
                        val optionsJson = Json.encodeToString(mapOf("maxMessages" to feature.maxMessages))
                        result.add("SendTransaction")
                        result.add("SendTransaction:$optionsJson")
                    } else {
                        result.add("SendTransaction")
                    }
                }
                is TONWalletKitConfiguration.SignDataFeature -> {
                    if (feature.types.isNotEmpty()) {
                        val types = feature.types.map { it.name.lowercase() }
                        val typesJson = Json.encodeToString(mapOf("types" to types))
                        result.add("SignData:$typesJson")
                    }
                }
            }
        }
        return result.distinct()
    }
}
