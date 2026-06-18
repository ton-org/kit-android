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
 * A destination currency the user receives on TON. Chain is implicit (always TON).
 *
 * @param address Jetton master address (e.g. `'EQCx...'`), or `'ton'` for native Toncoin.
 * @param symbol Token symbol, e.g. `'TON'`, `'USDT'`.
 * @param decimals Decimals
 * @param name Full token name.
 * @param logo Logo URL.
 */
@Serializable
data class TONCryptoOnrampDestinationCurrency(

    /* Jetton master address (e.g. `'EQCx...'`), or `'ton'` for native Toncoin. */
    @SerialName(value = "address")
    var address: kotlin.String,

    /* Token symbol, e.g. `'TON'`, `'USDT'`. */
    @SerialName(value = "symbol")
    var symbol: kotlin.String,

    /* Decimals */
    @SerialName(value = "decimals")
    var decimals: kotlin.Int,

    /* Full token name. */
    @SerialName(value = "name")
    var name: kotlin.String? = null,

    /* Logo URL. */
    @SerialName(value = "logo")
    var logo: kotlin.String? = null,

) {

    companion object
}
