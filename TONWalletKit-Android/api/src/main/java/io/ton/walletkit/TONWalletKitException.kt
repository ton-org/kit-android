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

/**
 * Strongly-typed errors raised by [TONWalletKit] for SDK lifecycle / usage failures.
 *
 * These are distinct from [WalletKitBridgeException], which represents failures of the
 * JavaScript bridge itself. A [TONWalletKitException] signals that the SDK was used
 * incorrectly (e.g. before initialization or after destruction).
 */
sealed class TONWalletKitException(message: String, cause: Throwable? = null) : Exception(message, cause) {

    /** The SDK was used before [TONWalletKit.initialize] was called. */
    class NotInitialized : TONWalletKitException(
        "TONWalletKit.initialize() must be called before using the SDK.",
    )

    /**
     * The SDK instance has already been destroyed. A new instance must be created.
     *
     * @property method The method that was invoked on the destroyed instance, when known.
     */
    class Destroyed(val method: String? = null) : TONWalletKitException(
        method?.let { "Cannot call method '$it' - SDK has been destroyed" }
            ?: "TONWalletKit instance has been destroyed. Create a new instance.",
    )

    /** Lazy auto-initialization of the SDK failed. */
    class AutoInitializationFailed(cause: Throwable) : TONWalletKitException(
        "Failed to auto-initialize WalletKit: ${cause.message}",
        cause,
    )
}
