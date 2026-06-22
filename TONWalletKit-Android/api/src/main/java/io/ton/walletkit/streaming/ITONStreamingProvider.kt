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
package io.ton.walletkit.streaming

import io.ton.walletkit.TONProviderType
import io.ton.walletkit.api.generated.TONBalanceUpdate
import io.ton.walletkit.api.generated.TONJettonUpdate
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.api.generated.TONTransactionsUpdate
import kotlinx.coroutines.flow.Flow

/** A streaming data source for one [network]; register with [ITONStreamingManager.registerProvider]. Built-ins come from `ITONWalletKit.createStreamingProvider`. */
interface ITONStreamingProvider {
    /** Stable provider identifier. */
    val identifier: String

    /** Always [TONProviderType.Streaming]. */
    val type: TONProviderType get() = TONProviderType.Streaming

    /** The network this provider streams from. */
    val network: TONNetwork

    /** Open the streaming connection. */
    suspend fun connect()

    /** Close the streaming connection. */
    suspend fun disconnect()

    /** A [Flow] of connection-state changes (`true` = connected). */
    fun connectionChange(): Flow<Boolean>

    /** Watch an account's balance. */
    fun balance(address: String): Flow<TONBalanceUpdate>

    /** Watch an account's transactions. */
    fun transactions(address: String): Flow<TONTransactionsUpdate>

    /** Watch an account's jetton balances. */
    fun jettons(address: String): Flow<TONJettonUpdate>
}
