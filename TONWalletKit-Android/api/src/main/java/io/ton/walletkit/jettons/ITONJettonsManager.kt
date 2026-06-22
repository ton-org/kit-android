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
package io.ton.walletkit.jettons

import io.ton.walletkit.api.generated.TONJetton
import io.ton.walletkit.api.generated.TONJettonInfo
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.model.TONUserFriendlyAddress

/**
 * Manager for resolving jetton-master metadata and a user's jetton holdings.
 */
interface ITONJettonsManager {
    /**
     * Resolve jetton-master metadata by address. Returns `null` when the master is unknown.
     */
    suspend fun jettonInfo(
        address: TONUserFriendlyAddress,
        network: TONNetwork,
    ): TONJettonInfo?

    /**
     * All jettons held by a user address, paginated.
     */
    suspend fun addressJettons(
        userAddress: TONUserFriendlyAddress,
        network: TONNetwork,
        offset: Int = 0,
        limit: Int = 20,
    ): List<TONJetton>

    /**
     * Validate that a string is a well-formed jetton address.
     */
    suspend fun validateJettonAddress(address: String): Boolean
}
