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

package io.ton.walletkit.demo.presentation.ui.screen

import android.annotation.SuppressLint
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.ton.walletkit.ITONWalletKit
import io.ton.walletkit.demo.R
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.theme.SmoothCornerShape
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.ui.dialog.UrlPromptDialog
import io.ton.walletkit.demo.presentation.ui.screen.iframesec.RealBridgeIframeCase
import io.ton.walletkit.demo.presentation.ui.screen.iframesec.WalletKitRealBridgeIframeScreen
import io.ton.walletkit.demo.presentation.util.QrScanner
import io.ton.walletkit.extensions.injectTonConnect

private enum class InvestigationPage { Tonconnect, DappWebView, RealBridge }

/**
 * Developer "Wallet Kit Investigation" screen: a list of debug tools reached from the wallet-home
 * gear icon. Hosts the TonConnect helper plus the iframe-security investigation screens (synthetic
 * diagnostic matrix and the real-bridge matrix) and a plain dApp WebView.
 */
@Composable
fun WalletKitInvestigationScreen(
    onBack: () -> Unit,
    onConnect: (String) -> Unit,
    walletKit: ITONWalletKit,
    modifier: Modifier = Modifier,
) {
    var page by remember { mutableStateOf<InvestigationPage?>(null) }

    when (page) {
        InvestigationPage.Tonconnect -> {
            BackHandler { page = null }
            WalletKitTonconnectScreen(onBack = { page = null }, onConnect = onConnect, modifier = modifier)
            return
        }
        InvestigationPage.DappWebView -> {
            BackHandler { page = null }
            DappWebViewScreen(onBack = { page = null }, walletKit = walletKit, modifier = modifier)
            return
        }
        InvestigationPage.RealBridge -> {
            BackHandler { page = null }
            WalletKitRealBridgeIframeScreen(walletKit = walletKit, onBack = { page = null }, modifier = modifier)
            return
        }
        null -> Unit
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgSecondary),
    ) {
        SubScreenTopBar(title = stringResource(R.string.investigation_title), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InvestigationRow(
                title = stringResource(R.string.investigation_tonconnect),
                onClick = { page = InvestigationPage.Tonconnect },
            )
            InvestigationRow(title = "dApp WebView", onClick = { page = InvestigationPage.DappWebView })
            InvestigationRow(
                title = "Iframe Security — Real dApp Bridge",
                onClick = { page = InvestigationPage.RealBridge },
            )
        }
    }
}

@Composable
private fun WalletKitTonconnectScreen(
    onBack: () -> Unit,
    onConnect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPrompt by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val scanFailedMessage = stringResource(R.string.investigation_scan_failed)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgSecondary),
    ) {
        SubScreenTopBar(title = stringResource(R.string.investigation_tonconnect), onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            InvestigationRow(
                title = stringResource(R.string.investigation_connect_to_dapp),
                onClick = { showPrompt = true },
            )
            InvestigationRow(
                title = stringResource(R.string.investigation_scan_qr),
                onClick = {
                    QrScanner.scan(
                        context = context,
                        onResult = onConnect,
                        onError = {
                            Toast.makeText(context, scanFailedMessage, Toast.LENGTH_SHORT).show()
                        },
                    )
                },
            )
        }
    }

    if (showPrompt) {
        UrlPromptDialog(
            onDismiss = { showPrompt = false },
            onConfirm = { url ->
                showPrompt = false
                onConnect(url)
            },
        )
    }
}

/** Minimal dApp WebView with the real WalletKit injection — mirrors the iOS investigation entry. */
@Composable
private fun DappWebViewScreen(
    onBack: () -> Unit,
    walletKit: ITONWalletKit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgSecondary),
    ) {
        SubScreenTopBar(title = "dApp WebView", onBack = onBack)
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                WebView(ctx).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                    )
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    injectTonConnect(walletKit)
                    loadUrl(RealBridgeIframeCase.DAPP_URL)
                }
            },
        )
    }
}

@Composable
private fun InvestigationRow(title: String, onClick: () -> Unit) {
    val shape = SmoothCornerShape(12.dp)
    TonText(
        text = title,
        style = TonTheme.typography.body,
        color = TonTheme.colors.textPrimary,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TonTheme.colors.bgPrimary)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}
