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
package io.ton.walletkit.demo.presentation.ui.screen

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import io.ton.walletkit.ITONWalletKit
import io.ton.walletkit.api.ChainIds
import io.ton.walletkit.api.MAINNET
import io.ton.walletkit.api.TESTNET
import io.ton.walletkit.api.TETRA
import io.ton.walletkit.api.WalletVersions
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.demo.R
import io.ton.walletkit.demo.designsystem.components.button.TonButton
import io.ton.walletkit.demo.designsystem.components.button.TonButtonConfig
import io.ton.walletkit.demo.designsystem.components.segmentedcontrol.TonSegmentedControl
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.theme.SmoothCornerShape
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.domain.model.WalletInterfaceType
import io.ton.walletkit.demo.presentation.ui.dialog.UrlPromptDialog
import io.ton.walletkit.demo.presentation.ui.screen.iframesec.WalletKitRealBridgeIframeScreen
import io.ton.walletkit.demo.presentation.util.QrScanner
import io.ton.walletkit.demo.presentation.util.TestTags

private enum class InvestigationPage { Tonconnect, Browser, AddWallet, RealBridge }

/**
 * Developer "Wallet Kit Investigation" screen: a list of debug tools reached from the wallet-home
 * gear icon. Hosts the TonConnect helper plus the iframe-security investigation screens (synthetic
 * diagnostic matrix and the real-bridge matrix) and a plain dApp WebView.
 */
@Composable
fun WalletKitInvestigationScreen(
    onBack: () -> Unit,
    onConnect: (String) -> Unit,
    onOpenBrowser: (String, Boolean) -> Unit,
    onImportWallet: (String, TONNetwork, List<String>, String, String, WalletInterfaceType) -> Unit,
    onGenerateWallet: (String, TONNetwork, String, WalletInterfaceType) -> Unit,
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
        InvestigationPage.Browser -> {
            BackHandler { page = null }
            BrowserLauncherScreen(onBack = { page = null }, onOpenBrowser = onOpenBrowser, modifier = modifier)
            return
        }
        InvestigationPage.AddWallet -> {
            BackHandler { page = null }
            TestAddWalletScreen(
                onBack = { page = null },
                onImport = { name, network, mnemonic, secretKey, version, interfaceType ->
                    onImportWallet(name, network, mnemonic, secretKey, version, interfaceType)
                    page = null
                },
                onGenerate = { name, network, version, interfaceType ->
                    onGenerateWallet(name, network, version, interfaceType)
                    page = null
                },
                modifier = modifier,
            )
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
                modifier = Modifier.testTag(TestTags.INVESTIGATION_TONCONNECT_ROW),
            )
            InvestigationRow(
                title = "dApp Browser",
                onClick = { page = InvestigationPage.Browser },
                modifier = Modifier.testTag(TestTags.INVESTIGATION_BROWSER_ROW),
            )
            InvestigationRow(
                title = "Add Wallet",
                onClick = { page = InvestigationPage.AddWallet },
                modifier = Modifier.testTag(TestTags.INVESTIGATION_ADD_WALLET_ROW),
            )
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
                modifier = Modifier.testTag(TestTags.INVESTIGATION_CONNECT_ROW),
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

private const val DEFAULT_DAPP_URL = "https://tonconnect-demo-dapp-with-react-ui.vercel.app/"

@Composable
private fun BrowserLauncherScreen(
    onBack: () -> Unit,
    onOpenBrowser: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    var url by remember { mutableStateOf(DEFAULT_DAPP_URL) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgSecondary),
    ) {
        SubScreenTopBar(title = "dApp Browser", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            TextField(
                value = url,
                onValueChange = { url = it },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag(TestTags.BROWSER_URL_FIELD),
            )
            InvestigationRow(
                title = "Open (injected)",
                onClick = { onOpenBrowser(url, true) },
                modifier = Modifier.testTag(TestTags.BROWSER_INJECT_BUTTON),
            )
            InvestigationRow(
                title = "Open (no-inject)",
                onClick = { onOpenBrowser(url, false) },
                modifier = Modifier.testTag(TestTags.BROWSER_NO_INJECT_BUTTON),
            )
        }
    }
}

@Composable
private fun TestAddWalletScreen(
    onBack: () -> Unit,
    onImport: (String, TONNetwork, List<String>, String, String, WalletInterfaceType) -> Unit,
    onGenerate: (String, TONNetwork, String, WalletInterfaceType) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by remember { mutableStateOf("Test wallet") }
    var network by remember { mutableStateOf(TONNetwork.MAINNET) }
    var version by remember { mutableStateOf(WalletVersions.V5R1) }
    var interfaceType by remember { mutableStateOf(WalletInterfaceType.MNEMONIC) }
    var mnemonic by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(TonTheme.colors.bgSecondary),
    ) {
        SubScreenTopBar(title = "Add Wallet", onBack = onBack)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AddWalletFieldLabel("Name")
            TextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.ADD_WALLET_NAME_FIELD),
            )

            AddWalletFieldLabel("Network")
            TonSegmentedControl(
                selection = network,
                items = listOf(TONNetwork.MAINNET, TONNetwork.TESTNET, TONNetwork.TETRA),
                title = ::networkLabel,
                onSelect = { network = it },
                modifier = Modifier.fillMaxWidth(),
            )

            AddWalletFieldLabel("Version")
            TonSegmentedControl(
                selection = version,
                items = listOf(WalletVersions.V5R1, WalletVersions.V4R2),
                title = { it },
                onSelect = { version = it },
                modifier = Modifier.fillMaxWidth(),
            )

            AddWalletFieldLabel("Interface")
            TonSegmentedControl(
                selection = interfaceType,
                items = WalletInterfaceType.entries,
                title = ::interfaceLabel,
                onSelect = { interfaceType = it },
                modifier = Modifier.fillMaxWidth(),
            )

            if (interfaceType == WalletInterfaceType.SECRET_KEY) {
                AddWalletFieldLabel("Secret key (hex)")
                TextField(
                    value = secretKey,
                    onValueChange = { secretKey = it.trim() },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.ADD_WALLET_SECRET_KEY_FIELD),
                )
            } else {
                AddWalletFieldLabel("Recovery phrase")
                TextField(
                    value = mnemonic,
                    onValueChange = { mnemonic = it },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth().testTag(TestTags.ADD_WALLET_MNEMONIC_FIELD),
                )
            }

            TonButton(
                text = "Import",
                onClick = {
                    val words = mnemonic.trim().lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
                    onImport(name, network, words, secretKey, version, interfaceType)
                },
                config = TonButtonConfig.Primary,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.ADD_WALLET_IMPORT_BUTTON),
            )
            TonButton(
                text = "Generate",
                onClick = { onGenerate(name, network, version, interfaceType) },
                config = TonButtonConfig.Secondary,
                enabled = interfaceType != WalletInterfaceType.SECRET_KEY,
                modifier = Modifier.fillMaxWidth().testTag(TestTags.ADD_WALLET_GENERATE_BUTTON),
            )
        }
    }
}

@Composable
private fun AddWalletFieldLabel(text: String) {
    TonText(
        text = text,
        style = TonTheme.typography.subheadline2,
        color = TonTheme.colors.textSecondary,
    )
}

private fun networkLabel(network: TONNetwork): String = when (network.chainId) {
    ChainIds.MAINNET -> "Mainnet"
    ChainIds.TESTNET -> "Testnet"
    ChainIds.TETRA -> "Tetra"
    else -> "Unknown"
}

private fun interfaceLabel(type: WalletInterfaceType): String = when (type) {
    WalletInterfaceType.MNEMONIC -> "Mnemonic"
    WalletInterfaceType.SECRET_KEY -> "Secret Key"
    WalletInterfaceType.SIGNER -> "Signer"
}

@Composable
private fun InvestigationRow(title: String, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = SmoothCornerShape(12.dp)
    TonText(
        text = title,
        style = TonTheme.typography.body,
        color = TonTheme.colors.textPrimary,
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TonTheme.colors.bgPrimary)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    )
}
