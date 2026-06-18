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
@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.ton.walletkit.api.generated

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 *
 *
 * @param amount Amount to onramp (either source or target crypto, depending on isSourceAmount)
 * @param sourceCurrency
 * @param targetCurrency
 * @param recipientAddress TON address that will receive the target crypto
 * @param refundAddress Refund address for the source crypto
 * @param isSourceAmount If true, `amount` is the source amount to spend. If false, `amount` is the target amount to receive. Defaults to true when omitted.
 * @param providerOptions Provider-specific options
 */
@Serializable
data class TONCryptoOnrampQuoteParams<TProviderOptions>(
    @SerialName("amount")
    var amount: kotlin.String,
    @SerialName("sourceCurrency")
    var sourceCurrency: TONCryptoOnrampSourceCurrency,
    @SerialName("targetCurrency")
    var targetCurrency: TONCryptoOnrampDestinationCurrency,
    @SerialName("recipientAddress")
    var recipientAddress: kotlin.String,
    @SerialName("refundAddress")
    var refundAddress: kotlin.String? = null,
    @SerialName("isSourceAmount")
    var isSourceAmount: kotlin.Boolean? = null,
    @SerialName("providerOptions")
    var providerOptions: TProviderOptions? = null,
) {
    companion object
}
