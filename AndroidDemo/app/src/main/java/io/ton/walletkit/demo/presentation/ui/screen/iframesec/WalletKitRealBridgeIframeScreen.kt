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
@file:SuppressLint("SetJavaScriptEnabled")

package io.ton.walletkit.demo.presentation.ui.screen.iframesec

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.webkit.WebViewCompat
import androidx.webkit.WebViewFeature
import io.ton.walletkit.ITONWalletKit
import io.ton.walletkit.demo.core.WalletKitDemoApp
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.ui.screen.SubScreenTopBar
import io.ton.walletkit.event.TONWalletKitEvent
import io.ton.walletkit.extensions.injectTonConnect
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Iframe-security matrix against the REAL WalletKit bridge. Loads the demo dApp with
 * injectTonConnect(walletKit), then injects iframe topologies that fire real signData requests.
 *
 * On Android the bridge attributes every frame's request to webView.url (the main dApp domain),
 * so cross-origin / data: / sandboxed iframes are all authorized — the native sign-data sheet
 * appears for frames that never connected. The diagnostic `iframeSecLog` listener shows the true
 * per-frame origin for contrast; the blue SDK rows show the (main-frame) domain the bridge used.
 */
@Composable
fun WalletKitRealBridgeIframeScreen(
    walletKit: ITONWalletKit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val app = remember(context) { context.applicationContext as WalletKitDemoApp }
    val log = remember { IframeSecLog() }
    var webView by remember { mutableStateOf<WebView?>(null) }
    var selected by remember { mutableStateOf(RealBridgeIframeCase.CROSS_ORIGIN_DATA) }

    // Mirror every event the SDK actually surfaces, with the domain it attributed (ground truth).
    LaunchedEffect(Unit) {
        log.addNative(action = "Ready — connect in the dApp, then pick a case and tap Inject", domain = "—")
        app.sdkEvents.collect { event -> log.addNative(describeEvent(event), eventDomain(event), eventPayload(event)) }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgSecondary),
    ) {
        SubScreenTopBar(title = "Real Bridge", onBack = onBack)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(TonTheme.colors.bgPrimary)
                .padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(selected.title, style = TonTheme.typography.bodySemibold.style, color = TonTheme.colors.textPrimary)
            Text(selected.summary, style = TonTheme.typography.caption1.style, color = TonTheme.colors.textSecondary)
            Text(selected.expectation, style = TonTheme.typography.caption1.style, color = Color(0xFFE5484D))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Scrolling case chips take the remaining width...
            Row(
                modifier = Modifier
                    .weight(1f)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RealBridgeIframeCase.entries.forEach { case ->
                    val isSel = case == selected
                    Text(
                        text = case.shortTitle,
                        style = TonTheme.typography.caption1.style,
                        color = if (isSel) Color.White else TonTheme.colors.textPrimary,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSel) TonTheme.colors.bgBrand else TonTheme.colors.bgFillTertiary)
                            .clickable { selected = case }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                    )
                }
            }

            // ...while the Inject button stays pinned on the right, outside the scroll.
            Text(
                text = "Inject",
                style = TonTheme.typography.bodySemibold.style,
                color = Color.White,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5484D))
                    .clickable {
                        val wv = webView
                        if (wv == null) {
                            log.addNative("⚠️ WebView not ready", "—")
                        } else {
                            log.addNative("▶ Inject: ${selected.title}", "(main frame)")
                            wv.evaluateJavascript(selected.spawnJs(), null)
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp),
            )
        }

        HorizontalDivider()

        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true

                    // Real WalletKit bridge (AndroidTonConnect + window.ton.tonconnect, all frames).
                    injectTonConnect(walletKit)

                    // Parallel diagnostic listener: reports the TRUE per-frame origin the platform
                    // sees, so it can be contrasted with the domain the SDK attributes.
                    if (WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)) {
                        WebViewCompat.addWebMessageListener(
                            this,
                            "iframeSecLog",
                            setOf("*"),
                        ) { _, message, sourceOrigin, isMainFrame, _ ->
                            val json = runCatching { JSONObject(message.data ?: "") }.getOrNull()
                            if (json != null) {
                                log.add(
                                    IframeSecLogEntry(
                                        frameLabel = json.optString("frameLabel", "?"),
                                        action = json.optString("action", "?"),
                                        claimedOrigin = json.optString("claimedOrigin", "?"),
                                        actualOrigin = sourceOrigin?.toString().orEmpty().ifEmpty { "null (opaque)" },
                                        isMainFrame = isMainFrame,
                                        payload = json.optString("payload", ""),
                                    ),
                                )
                            }
                        }
                    }

                    webView = this
                    loadUrl(RealBridgeIframeCase.DAPP_URL)
                }
            },
        )

        HorizontalDivider()
        RealBridgeLogPanel(log)
    }
}

@Composable
private fun RealBridgeLogPanel(log: IframeSecLog) {
    val listState = rememberLazyListState()
    LaunchedEffect(log.entries.size) {
        if (log.entries.isNotEmpty()) listState.animateScrollToItem(log.entries.size - 1)
    }
    Column(modifier = Modifier.fillMaxWidth().height(240.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(TonTheme.colors.bgSecondary)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Log (${log.entries.size}) · orange = injected frame · blue = SDK event",
                style = TonTheme.typography.caption1.style,
                color = TonTheme.colors.textSecondary,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { log.clear() }, enabled = log.entries.isNotEmpty()) { Text("Clear") }
        }
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(log.entries, key = { it.id }) { entry -> LogRow(entry) }
        }
    }
}

private fun describeEvent(event: TONWalletKitEvent): String = when (event) {
    is TONWalletKitEvent.ConnectRequest -> "→ SDK connectRequest"
    is TONWalletKitEvent.SignDataRequest -> "→ SDK signDataRequest — SHEET SHOULD APPEAR"
    is TONWalletKitEvent.SignMessageRequest -> "→ SDK signMessageRequest"
    is TONWalletKitEvent.SendTransactionRequest -> "→ SDK transactionRequest"
    is TONWalletKitEvent.Disconnect -> "→ SDK disconnect"
    is TONWalletKitEvent.RequestError -> "→ SDK requestError"
}

private fun eventDomain(event: TONWalletKitEvent): String = when (event) {
    is TONWalletKitEvent.ConnectRequest -> event.request.event.domain ?: "(nil)"
    is TONWalletKitEvent.SignDataRequest -> event.request.event.domain ?: "(nil)"
    is TONWalletKitEvent.SignMessageRequest -> event.request.event.domain ?: "(nil)"
    is TONWalletKitEvent.SendTransactionRequest -> event.request.event.domain ?: "(nil)"
    is TONWalletKitEvent.Disconnect -> event.event.domain ?: "(nil)"
    is TONWalletKitEvent.RequestError -> "—"
}

private fun eventPayload(event: TONWalletKitEvent): String = when (event) {
    is TONWalletKitEvent.SignDataRequest -> "tabId=${event.request.event.tabId ?: "nil"}"
    is TONWalletKitEvent.SendTransactionRequest -> "tabId=${event.request.event.tabId ?: "nil"}"
    is TONWalletKitEvent.SignMessageRequest -> "tabId=${event.request.event.tabId ?: "nil"}"
    else -> ""
}

private val timeFormat = SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

@Composable
private fun LogRow(entry: IframeSecLogEntry) {
    val claimedMatches = entry.claimedOrigin == entry.actualOrigin
    val badgeColor = when {
        entry.isNative -> Color(0xFF2D7DF6)
        claimedMatches -> Color(0xFF2EA043)
        else -> Color(0xFFE5484D)
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .background(if (entry.isNative) Color(0x142D7DF6) else TonTheme.colors.bgPrimary)
            .border(0.5.dp, Color(0x33808080), RoundedCornerShape(6.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(4.dp))
                    .background(badgeColor.copy(alpha = 0.18f))
                    .padding(horizontal = 6.dp, vertical = 2.dp),
            ) {
                Text(entry.frameLabel, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = badgeColor)
            }
            Text(
                text = "  ${entry.action}",
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                color = TonTheme.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            if (!entry.isMainFrame && !entry.isNative) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(3.dp))
                        .background(Color(0x33FF9800))
                        .padding(horizontal = 4.dp, vertical = 1.dp),
                ) {
                    Text("iframe", fontSize = 9.sp, color = Color(0xFFE08600))
                }
            }
            Text(
                text = timeFormat.format(Date(entry.timestamp)),
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TonTheme.colors.textTertiary,
            )
        }
        OriginRow("real:", entry.actualOrigin, TonTheme.colors.textPrimary)
        if (!entry.isNative) {
            OriginRow("claimed:", entry.claimedOrigin, if (claimedMatches) TonTheme.colors.textPrimary else Color(0xFFE5484D))
        }
        if (entry.payload.isNotEmpty()) {
            Text(
                text = entry.payload,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                color = TonTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun OriginRow(title: String, value: String, color: Color) {
    Row {
        Text(title, fontSize = 10.sp, color = TonTheme.colors.textTertiary)
        Text("  $value", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = color)
    }
}
