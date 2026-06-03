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
package io.ton.walletkit.demo.presentation.ui.sheet.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import io.ton.walletkit.api.generated.TONAssetType
import io.ton.walletkit.api.generated.TONStructuredItem
import io.ton.walletkit.api.generated.TONTransactionRequest
import io.ton.walletkit.api.generated.TONTransactionRequestMessage
import io.ton.walletkit.api.generated.TONTransactionTraceMoneyFlow
import io.ton.walletkit.api.generated.TONTransactionTraceMoneyFlowItem
import io.ton.walletkit.demo.R
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.theme.SmoothCornerShape
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.util.TextCommentPayloadDecoder
import io.ton.walletkit.demo.presentation.util.TonFormatter

private const val ADDRESS_TRUNCATION = 8
private const val PAYLOAD_TRUNCATION = 48
private val InnerCardShape = SmoothCornerShape(12.dp)

/** Renders the structured items (ton/jetton/nft) of [request], falling back to its raw messages. */
@Composable
internal fun TransactionEntries(request: TONTransactionRequest) {
    val items = request.items ?: emptyList()
    val messages = request.messages
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        when {
            items.isNotEmpty() -> items.forEachIndexed { index, item -> ItemEntry(item, index) }
            messages.isNotEmpty() -> messages.forEachIndexed { index, msg -> MessageEntry(msg, index) }
            else -> TonText(
                text = stringResource(R.string.transaction_request_no_messages),
                style = TonTheme.typography.subheadline2,
                color = TonTheme.colors.textSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(12.dp),
            )
        }
    }
}

@Composable
private fun ItemEntry(item: TONStructuredItem, index: Int) {
    when (item) {
        is TONStructuredItem.Ton -> item.value.let { ton ->
            EntryCard(
                title = "Send TON #${index + 1}",
                toAddress = ton.address,
                amount = tonAmount(ton.amount),
                extraLines = emptyList(),
                pills = listOfNotNull(
                    "State init".takeIf { ton.stateInit != null },
                    "Extra currencies".takeIf { !ton.extraCurrency.isNullOrEmpty() },
                ),
                payloads = listOfNotNull(ton.payload?.let { "Payload" to it.value }),
            )
        }
        is TONStructuredItem.Jetton -> item.value.let { jetton ->
            EntryCard(
                title = "Send jetton #${index + 1}",
                toAddress = jetton.destination,
                amount = TonFormatter.formatNanoTon(jetton.amount),
                extraLines = listOf("Jetton ${shortAddress(jetton.master)}"),
                pills = listOfNotNull(
                    jetton.attachAmount?.let { "Attach ${tonAmount(it)}" },
                    jetton.forwardAmount?.let { "Forward ${tonAmount(it)}" },
                    jetton.responseDestination?.let { "Response ${shortAddress(it)}" },
                ),
                payloads = listOfNotNull(
                    jetton.customPayload?.let { "Custom payload" to it.value },
                    jetton.forwardPayload?.let { "Forward payload" to it.value },
                ),
            )
        }
        is TONStructuredItem.Nft -> item.value.let { nft ->
            EntryCard(
                title = "Transfer NFT #${index + 1}",
                toAddress = nft.newOwner,
                amount = null,
                extraLines = listOf("NFT ${shortAddress(nft.nftAddress)}"),
                pills = listOfNotNull(
                    nft.attachAmount?.let { "Attach ${tonAmount(it)}" },
                    nft.forwardAmount?.let { "Forward ${tonAmount(it)}" },
                    nft.responseDestination?.let { "Response ${shortAddress(it)}" },
                ),
                payloads = listOfNotNull(
                    nft.customPayload?.let { "Custom payload" to it.value },
                    nft.forwardPayload?.let { "Forward payload" to it.value },
                ),
            )
        }
    }
}

@Composable
private fun MessageEntry(message: TONTransactionRequestMessage, index: Int) {
    EntryCard(
        title = "Message #${index + 1}",
        toAddress = message.address,
        amount = tonAmount(message.amount),
        extraLines = emptyList(),
        pills = listOfNotNull(
            "State init".takeIf { message.stateInit != null },
            message.mode?.base?.let { "Mode $it" },
            "Extra currencies".takeIf { !message.extraCurrency.isNullOrEmpty() },
        ),
        payloads = listOfNotNull(message.payload?.let { "Payload" to it.value }),
    )
}

@Composable
private fun EntryCard(
    title: String,
    toAddress: String,
    amount: String?,
    extraLines: List<String>,
    pills: List<String>,
    payloads: List<Pair<String, String>>,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(InnerCardShape)
            .background(TonTheme.colors.bgPrimary)
            .border(1.dp, TonTheme.colors.bgLightGray, InnerCardShape)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                TonText(title, style = TonTheme.typography.bodySemibold, color = TonTheme.colors.textPrimary)
                TonText(
                    text = "To ${shortAddress(toAddress)}",
                    style = TonTheme.typography.subheadline2,
                    color = TonTheme.colors.textBrand,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                extraLines.forEach { line ->
                    TonText(
                        text = line,
                        style = TonTheme.typography.subheadline2,
                        color = TonTheme.colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
            amount?.let {
                TonText(
                    text = it,
                    style = TonTheme.typography.bodySemibold,
                    color = TonTheme.colors.textPrimary,
                    maxLines = 1,
                )
            }
        }
        if (pills.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                pills.forEach { TonBadge(it) }
            }
        }
        payloads.forEach { (label, value) -> PayloadRow(label, value) }
    }
}

@Composable
private fun PayloadRow(label: String, value: String) {
    val decoded = TextCommentPayloadDecoder.decode(value)
    val display = decoded?.let { "Comment “$it”" } ?: truncate(value, PAYLOAD_TRUNCATION)
    TonText(
        text = buildAnnotatedString {
            withStyle(
                SpanStyle(color = TonTheme.colors.textPrimary, fontWeight = FontWeight.SemiBold),
            ) { append("$label: ") }
            withStyle(SpanStyle(color = TonTheme.colors.textSecondary)) { append(display) }
        },
        style = TonTheme.typography.subheadline2,
    )
}

/** Whether the money-flow card should be shown (preview succeeded and there's flow to show). */
internal fun shouldShowMoneyFlow(moneyFlow: TONTransactionTraceMoneyFlow): Boolean = moneyFlow.ourTransfers.isNotEmpty() || moneyFlow.outputs != "0" || moneyFlow.inputs != "0"

@Composable
internal fun MoneyFlowContent(moneyFlow: TONTransactionTraceMoneyFlow) {
    if (moneyFlow.ourTransfers.isEmpty()) {
        TonText(
            text = stringResource(R.string.transaction_request_no_transfers),
            style = TonTheme.typography.subheadline2,
            color = TonTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth(),
        )
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            moneyFlow.ourTransfers.forEach { MoneyFlowRow(it) }
        }
    }
}

@Composable
private fun MoneyFlowRow(transfer: TONTransactionTraceMoneyFlowItem) {
    val isPositive = !transfer.amount.trimStart().startsWith("-")
    val amountText = TonFormatter.formatNanoTon(transfer.amount.removePrefix("-"))
    val prefix = if (isPositive) "+" else "-"
    val unit = when (transfer.assetType) {
        TONAssetType.ton -> "TON"
        TONAssetType.nft -> "NFT"
        TONAssetType.jetton -> ""
    }
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        TonText(
            text = assetName(transfer),
            style = TonTheme.typography.bodySemibold,
            color = TonTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        TonText(
            text = "$prefix$amountText${if (unit.isEmpty()) "" else " $unit"}",
            style = TonTheme.typography.bodySemibold,
            color = if (isPositive) TonTheme.colors.textSuccess else TonTheme.colors.textError,
            maxLines = 1,
        )
    }
}

@Composable
internal fun TransactionPreviewError(message: String) {
    val shape = InnerCardShape
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(TonTheme.colors.bgPrimary)
            .border(1.dp, TonTheme.colors.textError.copy(alpha = 0.3f), shape)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        TonText(
            text = stringResource(R.string.transaction_request_error_prefix),
            style = TonTheme.typography.bodySemibold,
            color = TonTheme.colors.textError,
        )
        TonText(
            text = message,
            style = TonTheme.typography.subheadline2,
            color = TonTheme.colors.textError,
        )
    }
}

private fun assetName(transfer: TONTransactionTraceMoneyFlowItem): String = when (transfer.assetType) {
    TONAssetType.ton -> "TON"
    TONAssetType.jetton -> transfer.tokenAddress?.value?.let(::shortAddress) ?: "Jetton"
    TONAssetType.nft -> transfer.tokenAddress?.value?.let(::shortAddress) ?: "NFT"
}

private fun tonAmount(nano: String): String = "${TonFormatter.formatNanoTon(nano)} TON"

private fun shortAddress(address: String): String = if (address.length > ADDRESS_TRUNCATION * 2) {
    "${address.take(ADDRESS_TRUNCATION)}...${address.takeLast(ADDRESS_TRUNCATION)}"
} else {
    address
}

private fun truncate(value: String, max: Int): String = if (value.length > max) value.take(max) + "..." else value
