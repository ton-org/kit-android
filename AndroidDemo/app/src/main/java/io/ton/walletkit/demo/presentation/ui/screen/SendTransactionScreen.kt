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

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import io.ton.walletkit.ITONWallet
import io.ton.walletkit.ITONWalletKit
import io.ton.walletkit.api.WalletVersions
import io.ton.walletkit.demo.designsystem.components.button.TonButton
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.components.toggle.TonSwitch
import io.ton.walletkit.demo.designsystem.theme.SmoothCornerShape
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.model.FeeAsset
import io.ton.walletkit.demo.presentation.model.SendableToken
import io.ton.walletkit.demo.presentation.model.WalletSummary
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetErrorBox
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetHeader
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetTextField
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetScaffold
import io.ton.walletkit.demo.presentation.viewmodel.SendTokensViewModel

private enum class PickerMode { None, Token, FeeAsset }

@Composable
fun SendTransactionScreen(
    wallet: WalletSummary,
    walletKit: ITONWalletKit,
    onBack: () -> Unit,
) {
    var tonWallet by remember(wallet.address) { mutableStateOf<ITONWallet?>(null) }
    var resolving by remember(wallet.address) { mutableStateOf(true) }

    LaunchedEffect(wallet.address) {
        resolving = true
        tonWallet = walletKit.getWallets().firstOrNull { it.address().value == wallet.address }
        resolving = false
    }

    // tonapi's gasless relay only supports W5 (v5r1) wallets, so gasless is offered only for those.
    val gaslessSupported = wallet.version.equals(WalletVersions.V5R1, ignoreCase = true)

    when {
        resolving -> SendStatusBox("Loading wallet…")
        tonWallet == null -> SendStatusBox("Could not resolve the active wallet.", onBack)
        else -> SendTokensContent(tonWallet!!, walletKit, gaslessSupported, onBack)
    }
}

@Composable
private fun SendTokensContent(
    wallet: ITONWallet,
    walletKit: ITONWalletKit,
    gaslessSupported: Boolean,
    onBack: () -> Unit,
) {
    val viewModel: SendTokensViewModel = viewModel(
        key = "send:${wallet.address().value}",
        factory = SendTokensViewModel.factory(wallet, walletKit, gaslessSupported),
    )
    val state by viewModel.state.collectAsState()
    var picker by remember { mutableStateOf(PickerMode.None) }

    LaunchedEffect(state.sent) {
        if (state.sent) {
            viewModel.acknowledgeSent()
            onBack()
        }
    }

    // Resolve fee-asset metadata (ticker / icon / decimals) once the fee list appears, so the picker
    // rows and the inline selected-asset label fill in from short addresses to tickers as they load.
    LaunchedEffect(state.feeAssets.size) {
        if (state.feeAssets.isNotEmpty()) viewModel.loadFeeAssetMetadata()
    }

    when (picker) {
        PickerMode.Token -> AssetPicker(
            title = "Select token",
            rows = state.tokens.map { token ->
                AssetRowData(token.imageSource, token.symbol, token.name, token.symbol, token.displayBalance, token.id == state.selectedToken?.id) {
                    viewModel.selectToken(token)
                    picker = PickerMode.None
                }
            },
            onClose = { picker = PickerMode.None },
        )

        PickerMode.FeeAsset -> AssetPicker(
            title = "Fee asset",
            rows = state.feeAssets.map { asset ->
                AssetRowData(asset.imageSource, asset.symbol, asset.symbol, shortAddress(asset.address), null, asset.address == state.selectedFeeAsset?.address) {
                    viewModel.selectFeeAsset(asset)
                    picker = PickerMode.None
                }
            },
            onClose = { picker = PickerMode.None },
        )

        PickerMode.None -> SendForm(
            state = state,
            viewModel = viewModel,
            onBack = onBack,
            openTokenPicker = { picker = PickerMode.Token },
            openFeePicker = { picker = PickerMode.FeeAsset },
        )
    }
}

@Composable
private fun SendForm(
    state: SendTokensViewModel.UiState,
    viewModel: SendTokensViewModel,
    onBack: () -> Unit,
    openTokenPicker: () -> Unit,
    openFeePicker: () -> Unit,
) {
    val token = state.selectedToken

    TonConnectSheetScaffold(
        footer = {
            TonText(
                text = "Transfers are irreversible. Double-check the recipient address before sending.",
                style = TonTheme.typography.caption2Medium,
                color = TonTheme.colors.textTertiary,
                modifier = Modifier.fillMaxWidth(),
            )
            TonButton(
                text = if (state.isSending) "Sending…" else state.sendButtonTitle,
                onClick = viewModel::send,
                enabled = state.canSend,
            )
        },
    ) {
        SheetHeader(title = "Send", onClose = onBack)

        TokenSelectorCard(token, onClick = openTokenPicker)

        SheetTextField(
            value = state.recipient,
            onValueChange = viewModel::setRecipient,
            label = "Recipient address",
            placeholder = "EQ… / UQ…",
        )

        Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TonText(
                    "Amount (${token?.symbol ?: ""})",
                    style = TonTheme.typography.bodySemibold,
                    color = TonTheme.colors.textPrimary,
                    modifier = Modifier.weight(1f),
                )
                TonText(
                    "Max",
                    style = TonTheme.typography.subheadline2,
                    color = TonTheme.colors.textBrand,
                    modifier = Modifier
                        .clip(SmoothCornerShape(8.dp))
                        .clickable { viewModel.useMax() }
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }
            SheetTextField(
                value = state.amount,
                onValueChange = viewModel::setAmount,
                label = "",
                placeholder = "0.0",
                keyboardType = KeyboardType.Decimal,
                trailing = token?.symbol,
                supporting = token?.requiredAmountInfo,
            )
        }

        if (state.canUseGasless) {
            GaslessCard(state, viewModel, openFeePicker)
        }

        state.error?.let { SheetErrorBox(it) }
    }
}

@Composable
private fun TokenSelectorCard(token: SendableToken?, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmoothCornerShape(12.dp))
            .clickable(onClick = onClick)
            .background(TonTheme.colors.bgSecondary)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AssetIcon(token?.imageSource, token?.symbol ?: "?")
        Column(modifier = Modifier.weight(1f)) {
            TonText(token?.name ?: "Select token", style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textPrimary)
            TonText(
                "Balance: ${token?.displayBalance ?: "0"} ${token?.symbol ?: ""}",
                style = TonTheme.typography.subheadline2,
                color = TonTheme.colors.textSecondary,
            )
        }
        TonText("▾", style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textSecondary)
    }
}

@Composable
private fun GaslessCard(
    state: SendTokensViewModel.UiState,
    viewModel: SendTokensViewModel,
    openFeePicker: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(SmoothCornerShape(12.dp))
            .background(TonTheme.colors.bgSecondary)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                TonText("Gasless", style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textPrimary)
                TonText(
                    if (state.gaslessSupported) "Pay the network fee in a jetton" else "Requires a W5 (v5r1) wallet",
                    style = TonTheme.typography.subheadline2,
                    color = TonTheme.colors.textSecondary,
                )
            }
            if (state.gaslessSupported) {
                TonSwitch(checked = state.gaslessEnabled, onCheckedChange = viewModel::setGaslessEnabled)
            } else {
                TonSwitch(checked = false, onCheckedChange = {}, modifier = Modifier.alpha(0.4f))
            }
        }

        if (state.gaslessEnabled) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothCornerShape(8.dp))
                    .clickable(onClick = openFeePicker)
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TonText("Fee asset", style = TonTheme.typography.body, color = TonTheme.colors.textSecondary, modifier = Modifier.weight(1f))
                TonText(
                    state.selectedFeeAsset?.symbol ?: "Select",
                    style = TonTheme.typography.bodySemibold,
                    color = if (state.selectedFeeAsset == null) TonTheme.colors.textSecondary else TonTheme.colors.textPrimary,
                )
                TonText(" ▾", style = TonTheme.typography.subheadline2, color = TonTheme.colors.textSecondary)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                TonText("Gas fee", style = TonTheme.typography.body, color = TonTheme.colors.textSecondary, modifier = Modifier.weight(1f))
                TonText(
                    when {
                        state.gaslessError != null -> "—"
                        state.isQuoting -> "Calculating…"
                        else -> state.gaslessFeeText ?: "—"
                    },
                    style = TonTheme.typography.bodySemibold,
                    color = TonTheme.colors.textPrimary,
                )
            }

            state.gaslessError?.let {
                TonText(it, style = TonTheme.typography.subheadline2, color = TonTheme.colors.textError)
            }
        }
    }
}

private data class AssetRowData(
    val imageSource: String?,
    val placeholderSymbol: String,
    val title: String,
    val subtitle: String,
    val trailing: String?,
    val isSelected: Boolean,
    val onClick: () -> Unit,
)

@Composable
private fun AssetPicker(title: String, rows: List<AssetRowData>, onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        SheetHeader(title = title, onClose = onClose)
        rows.forEach { row ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(SmoothCornerShape(12.dp))
                    .clickable(onClick = row.onClick)
                    .padding(vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                AssetIcon(row.imageSource, row.placeholderSymbol)
                Column(modifier = Modifier.weight(1f)) {
                    TonText(row.title, style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textPrimary)
                    TonText(row.subtitle, style = TonTheme.typography.subheadline2, color = TonTheme.colors.textSecondary)
                }
                row.trailing?.let {
                    TonText(it, style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textPrimary)
                }
                if (row.isSelected) {
                    TonText("✓", style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textBrand)
                }
            }
        }
    }
}

@Composable
private fun AssetIcon(imageSource: String?, symbol: String) {
    if (!imageSource.isNullOrEmpty()) {
        AsyncImage(
            model = imageSource,
            contentDescription = symbol,
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape),
        )
    } else {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(TonTheme.colors.bgFillTertiary),
            contentAlignment = Alignment.Center,
        ) {
            TonText(
                symbol.take(3).uppercase(),
                style = TonTheme.typography.subheadline2,
                color = TonTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun SendStatusBox(message: String, onBack: (() -> Unit)? = null) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        TonText(
            message,
            style = TonTheme.typography.body,
            color = TonTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
        onBack?.let { TonButton(text = "Close", onClick = it) }
    }
}

private fun shortAddress(address: String): String = if (address.length > 8) "${address.take(4)}…${address.takeLast(4)}" else address
