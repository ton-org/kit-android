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
package io.ton.walletkit.demo.presentation.ui.components.wallet.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import io.ton.walletkit.demo.designsystem.components.text.TonText
import io.ton.walletkit.demo.designsystem.icons.TonIcon
import io.ton.walletkit.demo.designsystem.icons.TonIconImage
import io.ton.walletkit.demo.designsystem.theme.TonTheme
import io.ton.walletkit.demo.presentation.dev.devToggleTaps
import io.ton.walletkit.demo.presentation.util.TestTags

@Composable
fun WalletHomeBalance(
    totalBalance: Double,
    balanceSuffix: String,
    maxFractionDigits: Int,
    truncatedAddress: String,
    onCopyAddress: () -> Unit,
    modifier: Modifier = Modifier,
    onSecretTap: (() -> Unit)? = null,
) {
    val animated = rememberCountUp(totalBalance)
    val formatted = formatCountUp(animated, maxFractionDigits)
    val dotIndex = formatted.indexOf('.')
    val integerPart = if (dotIndex < 0) formatted else formatted.substring(0, dotIndex)
    val fractionPart = if (dotIndex < 0) "" else formatted.substring(dotIndex)

    val gestureModifier = if (onSecretTap != null) {
        Modifier.devToggleTaps(onTrigger = onSecretTap)
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .then(gestureModifier)
            .testTag(TestTags.WALLET_BALANCE),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row {
            TonText(
                text = integerPart,
                style = TonTheme.typography.price64,
                color = TonTheme.colors.textPrimary,
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
            if (fractionPart.isNotEmpty()) {
                TonText(
                    text = fractionPart,
                    style = TonTheme.typography.price40,
                    color = TonTheme.colors.textSecondary,
                    maxLines = 1,
                    modifier = Modifier.alignByBaseline(),
                )
            }
            TonText(
                text = balanceSuffix,
                style = TonTheme.typography.price40,
                color = TonTheme.colors.textSecondary,
                maxLines = 1,
                modifier = Modifier.alignByBaseline(),
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            TonIconImage(icon = TonIcon.Gram, size = 16.dp)
            TonText(
                text = truncatedAddress,
                style = TonTheme.typography.subheadline2,
                color = TonTheme.colors.textSecondary,
            )
            TonIconImage(
                icon = TonIcon.Copy,
                size = 16.dp,
                tint = TonTheme.colors.textSecondary,
                modifier = Modifier.clickable(onClick = onCopyAddress),
            )
        }
    }
}
