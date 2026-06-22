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
package io.ton.walletkit.model

import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.api.generated.TONPreparedSignData
import io.ton.walletkit.api.generated.TONProofMessage
import io.ton.walletkit.api.generated.TONTransactionRequest
import io.ton.walletkit.client.TONAPIClient
import io.ton.walletkit.config.TONWalletKitConfiguration

/**
 * The account-level signing contract for a wallet. Implemented by ITONWallet, or directly to back a
 * custom/hardware wallet for `ITONWalletKit.addWallet`. Suspend members may throw
 * WalletKitBridgeException. On the `signed*` members, `fakeSignature = true` produces a placeholder
 * signature for emulation.
 */
interface ITONWalletAdapter {
    /** Stable wallet/adapter identifier (also the wallet id). */
    fun identifier(): String

    /** The wallet's Ed25519 public key. */
    suspend fun publicKey(): TONHex

    /** The network this adapter operates on. */
    fun network(): TONNetwork

    /** The API client bound to this adapter's network. */
    fun client(): TONAPIClient

    /** The wallet's user-friendly address ([testnet] selects the testnet encoding). */
    fun address(testnet: Boolean = false): TONUserFriendlyAddress

    /** State init (base64 BOC) for first-use contract deployment. */
    suspend fun stateInit(): TONBase64

    /** Sign a transaction into a broadcastable external-message BoC. */
    suspend fun signedSendTransaction(
        input: TONTransactionRequest,
        fakeSignature: Boolean? = null,
    ): TONBase64

    /** Sign a transaction as an internal sign-message BoC (gasless relay flows). */
    suspend fun signedSignMessage(
        input: TONTransactionRequest,
        fakeSignature: Boolean? = null,
    ): TONBase64

    /** Sign prepared data (TON Connect signData); returns hex. */
    suspend fun signedSignData(
        input: TONPreparedSignData,
        fakeSignature: Boolean? = null,
    ): TONHex

    /** Sign a TON Proof challenge; returns hex. */
    suspend fun signedTonProof(
        input: TONProofMessage,
        fakeSignature: Boolean? = null,
    ): TONHex

    /** The wallet features this adapter supports, or null for SDK defaults. */
    fun supportedFeatures(): List<TONWalletKitConfiguration.Feature>? = null
}
