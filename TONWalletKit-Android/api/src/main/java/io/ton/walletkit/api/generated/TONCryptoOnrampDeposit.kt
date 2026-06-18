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
 * Deposit details returned by a crypto onramp provider.  The user must send `amount` of `sourceCurrency` to `address` to complete the onramp; the provider then delivers the target crypto to the user's TON address.
 *
 * @param depositId Deposit id
 * @param address Deposit address on the source chain
 * @param amount Exact amount of source crypto the user must send (in base units of `sourceCurrency.decimals`).
 * @param sourceCurrency
 * @param providerId Identifier of the provider that issued this deposit
 * @param memo Optional memo / tag required by some chains (e.g. XRP, TON comment)
 * @param expiresAt Unix timestamp (ms) after which the deposit offer is no longer valid
 */
@Serializable
data class TONCryptoOnrampDeposit(

    /* Deposit id */
    @SerialName(value = "depositId")
    var depositId: kotlin.String,

    /* Deposit address on the source chain */
    @SerialName(value = "address")
    var address: kotlin.String,

    /* Exact amount of source crypto the user must send (in base units of `sourceCurrency.decimals`). */
    @SerialName(value = "amount")
    var amount: kotlin.String,

    @SerialName(value = "sourceCurrency")
    var sourceCurrency: TONCryptoOnrampSourceCurrency,

    /* Identifier of the provider that issued this deposit */
    @SerialName(value = "providerId")
    var providerId: kotlin.String,

    /* Optional memo / tag required by some chains (e.g. XRP, TON comment) */
    @SerialName(value = "memo")
    var memo: kotlin.String? = null,

    /* Unix timestamp (ms) after which the deposit offer is no longer valid */
    @SerialName(value = "expiresAt")
    var expiresAt: kotlin.Double? = null,

) {

    companion object
}
