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

import android.net.Uri
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import io.ton.walletkit.api.generated.TONResult
import io.ton.walletkit.demo.R
import io.ton.walletkit.demo.designsystem.components.button.TonHoldToSignButton
import io.ton.walletkit.demo.presentation.model.SignMessageRequestUi
import io.ton.walletkit.demo.presentation.model.WalletSummary
import io.ton.walletkit.demo.presentation.ui.sheet.components.MoneyFlowContent
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetDisclaimer
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetHeader
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetScaffold
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetSection
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectWalletPicker
import io.ton.walletkit.demo.presentation.ui.sheet.components.TransactionEntries
import io.ton.walletkit.demo.presentation.ui.sheet.components.TransactionPreviewError
import io.ton.walletkit.demo.presentation.ui.sheet.components.shouldShowMoneyFlow
import io.ton.walletkit.demo.presentation.util.TestTags

/**
 * dApp sign-message (sign-only) approval sheet: the wallet signs but does NOT broadcast — the dApp
 * relays the resulting BoC. Same anatomy as the transaction sheet (paired logos header, read-only
 * wallet row, entry cards, optional money flow + preview error) scrolling above a pinned footer
 * (disclaimer + hold-to-sign button), under a "The dApp can submit" section and sign-message copy.
 */
@Composable
fun SignMessageRequestSheet(
    request: SignMessageRequestUi,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    wallet: WalletSummary? = null,
) {
    val event = request.signMessageRequest?.event
    val txRequest = event?.request
    val preview = event?.preview?.`data`
    val moneyFlow = preview?.moneyFlow

    TonConnectSheetScaffold(
        testTag = TestTags.SIGN_MESSAGE_REQUEST_SHEET,
        footer = {
            TonConnectSheetDisclaimer(text = stringResource(R.string.sign_message_request_disclaimer))
            TonHoldToSignButton(
                text = stringResource(R.string.sign_message_request_action_hold),
                onComplete = onApprove,
                modifier = Modifier.testTag(TestTags.SIGN_MESSAGE_APPROVE_BUTTON),
            )
        },
    ) {
        TonConnectSheetHeader(
            titleLeading = stringResource(R.string.sign_message_request_title_leading),
            titleAccent = signMessageDAppDomain(event?.dAppInfo?.url, request.dAppName),
            titleTrailing = stringResource(R.string.sign_message_request_title_trailing),
            subtitle = stringResource(R.string.sign_message_request_subtitle),
            onClose = onReject,
            dAppIconUrl = event?.dAppInfo?.iconUrl,
            modifier = Modifier.testTag(TestTags.SIGN_MESSAGE_REQUEST_TITLE),
            closeButtonModifier = Modifier.testTag(TestTags.SIGN_MESSAGE_REJECT_BUTTON),
        )

        wallet?.let {
            TonConnectWalletPicker(wallets = listOf(it), selected = it, onSelect = {})
        }

        if (txRequest != null) {
            TonConnectSheetSection(label = stringResource(R.string.sign_message_request_section)) {
                TransactionEntries(txRequest)
            }
        }

        if (moneyFlow != null && preview.result == TONResult.success && shouldShowMoneyFlow(moneyFlow)) {
            TonConnectSheetSection(label = stringResource(R.string.transaction_request_money_flow)) {
                MoneyFlowContent(moneyFlow)
            }
        }

        if (preview?.result == TONResult.failure) {
            TransactionPreviewError(preview.error?.message ?: stringResource(R.string.wallet_error_unknown))
        }
    }
}

/** dApp host for the title accent; falls back to the raw url then the dApp name. */
private fun signMessageDAppDomain(url: String?, fallbackName: String?): String {
    val host = url?.let { runCatching { Uri.parse(it).host }.getOrNull() }
    return host ?: url ?: fallbackName ?: ""
}
