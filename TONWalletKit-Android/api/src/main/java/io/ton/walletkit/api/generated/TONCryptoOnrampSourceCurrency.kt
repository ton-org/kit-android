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
 * A source currency the user can spend to onramp into a TON asset. Always lives on a non-TON chain (identified by CAIP-2).
 *
 * @param chain CAIP-2 source chain identifier, e.g. `'eip155:42161'`.
 * @param address Token contract address on the source chain, or `'native'` for the chain's native gas coin.
 * @param symbol Token symbol, e.g. `'USDT0'`, `'ETH'`.
 * @param decimals Decimals used to convert between display and base units.
 * @param name Full token name, e.g. `'Tether USD0'`. Optional.
 * @param logo Logo URL.
 */
@Serializable
data class TONCryptoOnrampSourceCurrency(

    /* CAIP-2 source chain identifier, e.g. `'eip155:42161'`. */
    @SerialName(value = "chain")
    var chain: kotlin.String,

    /* Token contract address on the source chain, or `'native'` for the chain's native gas coin. */
    @SerialName(value = "address")
    var address: kotlin.String,

    /* Token symbol, e.g. `'USDT0'`, `'ETH'`. */
    @SerialName(value = "symbol")
    var symbol: kotlin.String,

    /* Decimals used to convert between display and base units. */
    @SerialName(value = "decimals")
    var decimals: kotlin.Int,

    /* Full token name, e.g. `'Tether USD0'`. Optional. */
    @SerialName(value = "name")
    var name: kotlin.String? = null,

    /* Logo URL. */
    @SerialName(value = "logo")
    var logo: kotlin.String? = null,

) {

    companion object
}
