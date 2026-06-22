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
package io.ton.walletkit.demo.presentation.ui.sheet

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import io.ton.walletkit.ITONWallet
import io.ton.walletkit.ITONWalletKit
import io.ton.walletkit.api.MAINNET
import io.ton.walletkit.api.TESTNET
import io.ton.walletkit.api.TETRA
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.api.generated.TONStakingQuote
import io.ton.walletkit.api.generated.TONStakingQuoteDirection
import io.ton.walletkit.api.generated.TONUnstakeMode
import io.ton.walletkit.demo.R
import io.ton.walletkit.demo.designsystem.components.button.TonButton
import io.ton.walletkit.demo.designsystem.components.button.TonButtonConfig
import io.ton.walletkit.demo.designsystem.components.loader.TonLoader
import io.ton.walletkit.demo.designsystem.components.segmentedcontrol.TonSegmentedControl
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.model.WalletSummary
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetErrorBox
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetHeader
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetLabeledRow
import io.ton.walletkit.demo.presentation.ui.sheet.components.SheetTextField
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetScaffold
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetSection
import io.ton.walletkit.demo.presentation.util.TestTags
import io.ton.walletkit.demo.presentation.util.TonFormatter
import io.ton.walletkit.demo.presentation.viewmodel.StakingViewModel
import java.math.BigDecimal
import java.math.RoundingMode

@Composable
fun StakingSheet(
    wallet: WalletSummary,
    walletKit: ITONWalletKit,
    sheetKey: Long,
    onDismiss: () -> Unit,
) {
    var tonWallet by remember(wallet.address) { mutableStateOf<ITONWallet?>(null) }
    var isLoadingWallet by remember(wallet.address) { mutableStateOf(true) }

    LaunchedEffect(wallet.address) {
        isLoadingWallet = true
        tonWallet = walletKit.getWallets().firstOrNull { it.address().value == wallet.address }
        isLoadingWallet = false
    }

    when {
        isLoadingWallet -> LoadingContent()
        tonWallet == null -> MissingWalletContent(onDismiss = onDismiss)
        else -> StakingContent(
            wallet = wallet,
            tonWallet = tonWallet!!,
            walletKit = walletKit,
            sheetKey = sheetKey,
            onDismiss = onDismiss,
        )
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(40.dp),
        contentAlignment = Alignment.Center,
    ) {
        TonLoader(size = 28.dp, color = TonTheme.colors.textBrand)
    }
}

@Composable
private fun MissingWalletContent(onDismiss: () -> Unit) {
    TonConnectSheetScaffold(
        footer = {
            TonButton(text = "Close", onClick = onDismiss)
        },
    ) {
        SheetHeader(title = "Staking", onClose = onDismiss)
        TonConnectSheetSection(label = "Wallet not found") {
            TonText(
                "The staking screen could not resolve the active wallet instance.",
                style = TonTheme.typography.subheadline2,
                color = TonTheme.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun StakingContent(
    wallet: WalletSummary,
    tonWallet: ITONWallet,
    walletKit: ITONWalletKit,
    sheetKey: Long,
    onDismiss: () -> Unit,
) {
    val viewModel: StakingViewModel = viewModel(
        key = "staking:${wallet.address}:$sheetKey",
        factory = StakingViewModel.factory(tonWallet, walletKit, wallet.network),
    )
    val state by viewModel.state.collectAsState()
    val isBusy = state.isLoadingQuote || state.isExecuting
    val availableBalanceNano = state.availableBalanceNano ?: wallet.balanceNano
    val networkName = networkLabel(wallet.network)

    LaunchedEffect(viewModel) {
        viewModel.load()
    }

    val amountInvalid = state.amount.isNotEmpty() && !isValidAmount(state.amount)
    val belowMinimum = state.direction == TONStakingQuoteDirection.stake && isAmountBelowMinimumStake(state.amount)
    val aboveBalance = state.direction == TONStakingQuoteDirection.stake &&
        isAmountAboveBalance(state.amount, availableBalanceNano)
    val canAct = !isBusy &&
        state.amount.isNotBlank() &&
        isValidAmount(state.amount) &&
        !belowMinimum &&
        !aboveBalance

    TonConnectSheetScaffold(
        footer = {
            TonText(
                text = "Use a funded wallet. The first tap previews the quote, and the second tap confirms the blockchain transaction.",
                style = TonTheme.typography.caption2Medium,
                color = TonTheme.colors.textTertiary,
                modifier = Modifier.fillMaxWidth(),
            )
            if (state.currentQuote != null) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    TonButton(
                        text = "Reset",
                        onClick = viewModel::cancelQuote,
                        config = TonButtonConfig.Secondary,
                        enabled = !isBusy,
                        stretch = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(TestTags.STAKING_CANCEL_BUTTON),
                    )
                    TonButton(
                        text = state.buttonTitle,
                        onClick = viewModel::buttonAction,
                        config = TonButtonConfig.Primary.isLoading(isBusy),
                        enabled = canAct,
                        stretch = false,
                        modifier = Modifier
                            .weight(1f)
                            .testTag(TestTags.STAKING_ACTION_BUTTON),
                    )
                }
            } else {
                TonButton(
                    text = state.buttonTitle,
                    onClick = viewModel::buttonAction,
                    config = TonButtonConfig.Primary.isLoading(isBusy),
                    enabled = canAct,
                    modifier = Modifier.testTag(TestTags.STAKING_ACTION_BUTTON),
                )
            }
        },
    ) {
        SheetHeader(title = "Staking", onClose = onDismiss)

        TonConnectSheetSection(label = "Balance") {
            SheetLabeledRow("Wallet", wallet.name)
            SheetLabeledRow("Network", networkName)
            SheetLabeledRow("Available balance", formatTonDisplay(availableBalanceNano, wallet.balance))
            SheetLabeledRow("Staked balance", state.stakedBalance?.let { formatTokenDisplay(it.stakedBalance) } ?: "Loading…")
            SheetLabeledRow("Instant unstake available", state.stakedBalance?.let { formatTokenDisplay(it.instantUnstakeAvailable) } ?: "Loading…")
            SheetLabeledRow("Provider APY", state.formattedAPY ?: "Loading…")
        }

        TonSegmentedControl(
            selection = state.direction,
            items = listOf(TONStakingQuoteDirection.stake, TONStakingQuoteDirection.unstake),
            title = { it.directionLabel() },
            onSelect = { if (!isBusy) viewModel.setDirection(it) },
            modifier = Modifier.fillMaxWidth(),
        )

        SheetTextField(
            value = state.amount,
            onValueChange = viewModel::setAmount,
            label = "Amount",
            placeholder = "0.00",
            keyboardType = KeyboardType.Decimal,
            trailing = state.inputTokenSymbol,
            enabled = !isBusy,
            isError = amountInvalid || belowMinimum || aboveBalance,
            supporting = when {
                amountInvalid -> "Enter a valid positive amount."
                belowMinimum -> "TonStakers minimum stake is 1 GRAM."
                aboveBalance -> "Amount is higher than the available wallet balance."
                else -> null
            },
            modifier = Modifier.testTag(TestTags.STAKING_AMOUNT_FIELD),
        )

        if (state.direction == TONStakingQuoteDirection.unstake && state.supportedModes.isNotEmpty()) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                TonText("Unstake mode", style = TonTheme.typography.footnoteCaps, color = TonTheme.colors.textSecondary)
                TonSegmentedControl(
                    selection = state.unstakeMode,
                    items = state.supportedModes,
                    title = { it.toDisplayLabel() },
                    onSelect = { if (!isBusy) viewModel.setUnstakeMode(it) },
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }

        state.currentQuote?.let { quote ->
            QuoteSection(quote = quote, receiveTokenSymbol = state.receiveTokenSymbol)
        }

        state.error?.let { SheetErrorBox(it) }
    }
}

@Composable
private fun QuoteSection(
    quote: TONStakingQuote,
    receiveTokenSymbol: String,
) {
    TonConnectSheetSection(label = "Quote") {
        SheetLabeledRow("You send", "${formatTokenDisplay(quote.amountIn)} ${quote.direction.inputSymbol()}")
        SheetLabeledRow("You receive", "${formatTokenDisplay(quote.amountOut)} $receiveTokenSymbol")
        SheetLabeledRow("Provider", quote.providerId)
        quote.unstakeMode?.let { mode ->
            SheetLabeledRow("Unstake mode", mode.toDisplayLabel())
        }
    }
}

@Composable
private fun networkLabel(network: TONNetwork): String = when (network.chainId) {
    TONNetwork.MAINNET.chainId -> stringResource(R.string.network_mainnet)
    TONNetwork.TESTNET.chainId -> stringResource(R.string.network_testnet)
    TONNetwork.TETRA.chainId -> stringResource(R.string.network_tetra)
    else -> "Unknown"
}

private fun TONStakingQuoteDirection.directionLabel(): String = when (this) {
    TONStakingQuoteDirection.stake -> "Stake"
    TONStakingQuoteDirection.unstake -> "Unstake"
}

private fun TONUnstakeMode.toDisplayLabel(): String = when (this) {
    TONUnstakeMode.instant -> "Instant"
    TONUnstakeMode.whenAvailable -> "When Available"
    TONUnstakeMode.roundEnd -> "Round End"
}

private fun TONStakingQuoteDirection.inputSymbol(): String = when (this) {
    TONStakingQuoteDirection.stake -> "GRAM"
    TONStakingQuoteDirection.unstake -> "tsTON"
}

private fun isValidAmount(value: String): Boolean = runCatching {
    (value.toBigDecimalOrNull() ?: return false) > BigDecimal.ZERO
}.getOrDefault(false)

private fun isAmountAboveBalance(value: String, balanceNano: String?): Boolean = runCatching {
    if (value.isBlank() || balanceNano.isNullOrBlank()) return false
    val amountNano = TonFormatter.tonToNano(value).toBigDecimal()
    amountNano > balanceNano.toBigDecimal()
}.getOrDefault(false)

private fun isAmountBelowMinimumStake(value: String): Boolean = runCatching {
    if (value.isBlank()) return false
    value.toBigDecimal() < BigDecimal.ONE
}.getOrDefault(false)

private fun formatTonDisplay(balanceNano: String?, fallback: String?): String = when {
    !balanceNano.isNullOrBlank() -> TonFormatter.formatTon(balanceNano)
    !fallback.isNullOrBlank() -> fallback
    else -> "0"
}

private fun formatTokenDisplay(value: String?): String = runCatching {
    if (value.isNullOrBlank()) {
        return "0"
    }
    BigDecimal(value)
        .setScale(4, RoundingMode.DOWN)
        .stripTrailingZeros()
        .toPlainString()
}.getOrDefault(value ?: "0")
