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

import io.ton.walletkit.api.generated.TONBalanceUpdate
import io.ton.walletkit.api.generated.TONJettonUpdate
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.api.generated.TONStreamingUpdate
import io.ton.walletkit.api.generated.TONStreamingWatchType
import io.ton.walletkit.api.generated.TONTransactionsUpdate
import kotlinx.coroutines.flow.Flow

/** Real-time subscriptions (balance, transactions, jettons) backed by registered [ITONStreamingProvider]s, delivered as cold [Flow]s. */
interface ITONStreamingManager {
    /** Whether a streaming provider is registered for [network]. */
    suspend fun hasProvider(network: TONNetwork): Boolean

    /** Register a streaming provider; its [ITONStreamingProvider.network] selects the network. */
    suspend fun registerProvider(provider: ITONStreamingProvider)

    /** Open the streaming connection(s). */
    suspend fun connect()

    /** Close the streaming connection(s). */
    suspend fun disconnect()

    /** Connection-state changes for [network] (true = connected). */
    fun connectionChange(network: TONNetwork): Flow<Boolean>

    /** Watch an account's balance on [network]. */
    fun balance(network: TONNetwork, address: String): Flow<TONBalanceUpdate>

    /** Watch an account's transactions on [network]. */
    fun transactions(network: TONNetwork, address: String): Flow<TONTransactionsUpdate>

    /** Watch an account's jetton balances on [network]. */
    fun jettons(network: TONNetwork, address: String): Flow<TONJettonUpdate>

    /** Watch the given [types] for an account on [network] in one stream. */
    fun updates(network: TONNetwork, address: String, types: List<TONStreamingWatchType>): Flow<TONStreamingUpdate>
}
