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
package io.ton.walletkit

import io.ton.walletkit.api.generated.*
import io.ton.walletkit.model.ITONWalletAdapter
import io.ton.walletkit.model.TONBalance
import io.ton.walletkit.model.TONUserFriendlyAddress

/**
 * A TON wallet, obtained from [ITONWalletKit.addWallet]. Extends [ITONWalletAdapter]; suspend
 * members may throw [WalletKitBridgeException].
 */
interface ITONWallet : ITONWalletAdapter {
    /** Get the wallet balance, in nano-TON. */
    suspend fun balance(): TONBalance

    /** Create a TON transfer transaction, ready for [send] or [preview]. */
    suspend fun transferTONTransaction(request: TONTransferRequest): TONTransactionRequest

    /** Create a multi-recipient TON transfer transaction. */
    suspend fun transferTONTransaction(requests: List<TONTransferRequest>): TONTransactionRequest

    /** Emulate a transaction to preview its outcome and estimated fees. */
    suspend fun preview(
        transactionRequest: TONTransactionRequest,
        options: TONTransactionPreviewOptions? = null,
    ): TONTransactionEmulatedPreview

    /** Sign and broadcast a transaction. */
    suspend fun send(transactionRequest: TONTransactionRequest): TONSendTransactionResponse

    /** Create an NFT transfer transaction. */
    suspend fun transferNFTTransaction(request: TONNFTTransferRequest): TONTransactionRequest

    /** Create an NFT transfer transaction from raw parameters. */
    suspend fun transferNFTTransaction(request: TONNFTRawTransferRequest): TONTransactionRequest

    /** Get NFTs owned by this wallet. */
    suspend fun nfts(request: TONNFTsRequest): TONNFTsResponse

    /** Get a single NFT by address, or null if none. */
    suspend fun nft(address: TONUserFriendlyAddress): TONNFT?

    /** Get this wallet's balance of a specific jetton. */
    suspend fun jettonBalance(jettonAddress: TONUserFriendlyAddress): TONBalance

    /** Resolve this wallet's jetton-wallet address for a jetton. */
    suspend fun jettonWalletAddress(jettonAddress: TONUserFriendlyAddress): TONUserFriendlyAddress

    /** Create a jetton transfer transaction. */
    suspend fun transferJettonTransaction(request: TONJettonsTransferRequest): TONTransactionRequest

    /** Get jettons owned by this wallet. */
    suspend fun jettons(request: TONJettonsRequest? = null): TONJettonsResponse
}

/** Fetch the first [limit] NFTs owned by this wallet. */
suspend fun ITONWallet.nfts(limit: Int): TONNFTsResponse =
    nfts(TONNFTsRequest(pagination = TONPagination(limit = limit)))

/** Fetch the first [limit] jettons owned by this wallet. */
suspend fun ITONWallet.jettons(limit: Int): TONJettonsResponse =
    jettons(TONJettonsRequest(pagination = TONPagination(limit = limit)))
