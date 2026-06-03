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

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.tooling.preview.Preview
import io.ton.walletkit.demo.R
import io.ton.walletkit.demo.designsystem.components.button.TonHoldToSignButton
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.model.SignDataRequestUi
import io.ton.walletkit.demo.presentation.model.WalletSummary
import io.ton.walletkit.demo.presentation.ui.preview.PreviewData
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetDisclaimer
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetHeader
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetScaffold
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectSheetSection
import io.ton.walletkit.demo.presentation.ui.sheet.components.TonConnectWalletPicker
import io.ton.walletkit.demo.presentation.util.TestTags

@Composable
fun SignDataSheet(
    request: SignDataRequestUi,
    onApprove: () -> Unit,
    onReject: () -> Unit,
    wallet: WalletSummary? = null,
) {
    val clipboardManager = LocalClipboardManager.current
    val displayContent = request.preview?.takeIf { it.isNotBlank() } ?: request.payloadContent

    TonConnectSheetScaffold(
        testTag = TestTags.SIGN_DATA_REQUEST_SHEET,
        footer = {
            TonConnectSheetDisclaimer(text = stringResource(R.string.sign_request_disclaimer))
            TonHoldToSignButton(
                text = stringResource(R.string.sign_request_action_hold),
                onComplete = onApprove,
                modifier = Modifier.testTag(TestTags.SIGN_DATA_APPROVE_BUTTON),
            )
        },
    ) {
        TonConnectSheetHeader(
            titleLeading = stringResource(R.string.sign_request_title_leading),
            titleAccent = request.dAppName ?: "",
            titleTrailing = stringResource(R.string.sign_request_title_trailing),
            subtitle = stringResource(R.string.sign_request_subtitle),
            onClose = onReject,
            modifier = Modifier.testTag(TestTags.SIGN_DATA_REQUEST_TITLE),
            closeButtonModifier = Modifier.testTag(TestTags.SIGN_DATA_REJECT_BUTTON),
        )

        wallet?.let {
            TonConnectWalletPicker(
                wallets = listOf(it),
                selected = it,
                onSelect = {},
            )
        }

        TonConnectSheetSection(
            label = stringResource(R.string.sign_request_section_data),
            accent = true,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        clipboardManager.setText(AnnotatedString(request.payloadContent))
                    },
            ) {
                TonText(
                    text = request.payloadType.replaceFirstChar { it.titlecase() },
                    style = TonTheme.typography.bodySemibold,
                    color = TonTheme.colors.textPrimary,
                )
                TonText(
                    text = displayContent,
                    style = TonTheme.typography.subheadline2,
                    color = TonTheme.colors.textSecondary,
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SignDataSheetPreview() {
    TonTheme {
        SignDataSheet(
            request = PreviewData.signDataRequest,
            onApprove = {},
            onReject = {},
        )
    }
}
