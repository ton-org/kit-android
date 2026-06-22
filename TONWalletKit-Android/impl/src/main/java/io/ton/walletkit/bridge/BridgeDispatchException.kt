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
package io.ton.walletkit.bridge

import io.ton.walletkit.WalletKitBridgeException

internal sealed class BridgeDispatchException(message: String) : WalletKitBridgeException(message) {
    class UnknownMethod(method: String) :
        BridgeDispatchException("Unknown bridge method: $method")

    class UnknownReverseRpcMethod(method: String) :
        BridgeDispatchException("Unknown reverse-RPC method: $method")

    class MissingParameter(method: String, parameter: String) :
        BridgeDispatchException("$method: missing $parameter")

    class AdapterNotFound(adapterId: String) :
        BridgeDispatchException("Adapter not found: $adapterId")

    class SignerNotFound(signerId: String) :
        BridgeDispatchException("Custom signer not found: $signerId")

    class SessionManagerNotConfigured :
        BridgeDispatchException("Session manager not configured")

    class ApiClientNotConfigured(chainId: String) :
        BridgeDispatchException("No API client configured for chainId=$chainId")

    class ProviderNotRegistered(providerId: String) :
        BridgeDispatchException("No Kotlin provider registered for id=$providerId")

    class WrappedFunctionNotRegistered(reference: String) :
        BridgeDispatchException("No wrapped function registered for reference: $reference")
}
