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
package io.ton.walletkit.client

import io.ton.walletkit.api.generated.TONAccountState
import io.ton.walletkit.api.generated.TONEmulationResult
import io.ton.walletkit.api.generated.TONGetMethodResult
import io.ton.walletkit.api.generated.TONMasterchainInfo
import io.ton.walletkit.api.generated.TONNFTsRequest
import io.ton.walletkit.api.generated.TONNFTsResponse
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.api.generated.TONRawStackItem
import io.ton.walletkit.api.generated.TONUserNFTsRequest
import io.ton.walletkit.model.TONBase64
import io.ton.walletkit.model.TONTokenAmount
import io.ton.walletkit.model.TONUserFriendlyAddress

/**
 * Interface for custom TON API client implementations: send signed BoCs, run contract get-methods,
 * read chain state. Network identity is set at registration via
 * [io.ton.walletkit.config.TONWalletKitConfiguration.NetworkConfiguration], not here. Suspend
 * methods may throw WalletKitBridgeException when the SDK-backed client is used.
 */
interface TONAPIClient {
    /** The network this client talks to. */
    fun network(): TONNetwork

    /** Broadcast a signed external message (base64 BoC); returns its hash. */
    suspend fun sendBoc(boc: TONBase64): String

    /** Run a contract get-method and return its result stack. */
    suspend fun runGetMethod(
        address: TONUserFriendlyAddress,
        method: String,
        stack: List<TONRawStackItem>? = null,
        seqno: UInt? = null,
    ): TONGetMethodResult

    /** Get an account's balance, in nano-TON. */
    suspend fun getBalance(
        address: TONUserFriendlyAddress,
        seqno: UInt? = null,
    ): TONTokenAmount

    /** Get the latest masterchain info. */
    suspend fun getMasterchainInfo(): TONMasterchainInfo

    /** List NFT items by their contract addresses. */
    suspend fun nftItemsByAddress(request: TONNFTsRequest): TONNFTsResponse

    /** List NFT items owned by an account. */
    suspend fun nftItemsByOwner(request: TONUserNFTsRequest): TONNFTsResponse

    /** Emulate an external message and return its predicted result. */
    suspend fun fetchEmulation(
        messageBoc: TONBase64,
        ignoreSignature: Boolean = false,
    ): TONEmulationResult

    /** Get an account's on-chain state. */
    suspend fun accountState(
        address: TONUserFriendlyAddress,
        seqno: UInt? = null,
    ): TONAccountState

    /** Batch [accountState] for several accounts. */
    suspend fun accountStates(
        addresses: List<TONUserFriendlyAddress>,
    ): Map<TONUserFriendlyAddress, TONAccountState>

    /** Resolve a TON DNS domain to its wallet address, or null. */
    suspend fun resolveDnsWallet(domain: String): String?

    /** Reverse-resolve a wallet address to its TON DNS domain, or null. */
    suspend fun backResolveDnsWallet(address: TONUserFriendlyAddress): String?
}
