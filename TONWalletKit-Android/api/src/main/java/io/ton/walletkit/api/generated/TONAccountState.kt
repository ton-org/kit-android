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

import io.ton.walletkit.model.TONUserFriendlyAddress
import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Blockchain state of an account at a given point in time.  The `status` field distinguishes four cases: - `active` — contract deployed, `code` and `data` present - `uninitialized` — has balance/history but no contract deployed; `code`/`data` omitted - `frozen` — frozen due to storage debt; `frozenHash` points at the pre-freeze state - `non-existing` — no on-chain record at all; balance is `'0'` and other fields omitted
 *
 * @param address
 * @param status
 * @param rawBalance
 * @param balance Balance formatted in TON (10^9 nanotons = 1 TON).
 * @param extraCurrencies Map of extra currency IDs to their amounts. Extra currencies are additional tokens that can be attached to TON messages.
 * @param code Base64-encoded contract code BOC. Omitted if the contract is not deployed.
 * @param `data` Base64-encoded contract data BOC. Omitted if the contract is not deployed.
 * @param lastTransaction
 * @param frozenHash
 */
@Serializable
data class TONAccountState(

    @SerialName(value = "address")
    val address: io.ton.walletkit.model.TONUserFriendlyAddress,

    @Contextual @SerialName(value = "status")
    val status: TONAccountStatus,

    @SerialName(value = "rawBalance")
    val rawBalance: kotlin.String,

    /* Balance formatted in TON (10^9 nanotons = 1 TON). */
    @SerialName(value = "balance")
    val balance: kotlin.String,

    /* Map of extra currency IDs to their amounts. Extra currencies are additional tokens that can be attached to TON messages. */
    @SerialName(value = "extraCurrencies")
    val extraCurrencies: kotlin.collections.Map<kotlin.String, kotlin.String>,

    /* Base64-encoded contract code BOC. Omitted if the contract is not deployed. */
    @SerialName(value = "code")
    val code: kotlin.String? = null,

    /* Base64-encoded contract data BOC. Omitted if the contract is not deployed. */
    @SerialName(value = "data")
    val `data`: kotlin.String? = null,

    @SerialName(value = "lastTransaction")
    val lastTransaction: TONTransactionId? = null,

    @Contextual @SerialName(value = "frozenHash")
    val frozenHash: io.ton.walletkit.model.TONHex? = null,

) {

    companion object
}
