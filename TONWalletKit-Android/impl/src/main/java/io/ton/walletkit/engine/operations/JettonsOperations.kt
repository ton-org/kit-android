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
package io.ton.walletkit.engine.operations

import io.ton.walletkit.api.generated.TONJetton
import io.ton.walletkit.api.generated.TONJettonInfo
import io.ton.walletkit.api.generated.TONNetwork
import io.ton.walletkit.engine.infrastructure.BridgeRpcClient
import io.ton.walletkit.engine.infrastructure.callTyped
import io.ton.walletkit.engine.infrastructure.callTypedOrNull
import io.ton.walletkit.engine.operations.requests.GetAddressJettonsRequest
import io.ton.walletkit.engine.operations.requests.GetJettonInfoRequest
import io.ton.walletkit.engine.operations.requests.ValidateJettonAddressRequest
import io.ton.walletkit.engine.operations.responses.ValidateJettonAddressResponse
import io.ton.walletkit.internal.constants.BridgeMethodConstants

internal suspend fun BridgeRpcClient.getJettonInfo(address: String, network: TONNetwork): TONJettonInfo? =
    callTypedOrNull(
        BridgeMethodConstants.METHOD_GET_JETTON_INFO,
        GetJettonInfoRequest(address = address, network = network),
    )

internal suspend fun BridgeRpcClient.getAddressJettons(
    userAddress: String,
    network: TONNetwork,
    offset: Int,
    limit: Int,
): List<TONJetton> = callTyped(
    BridgeMethodConstants.METHOD_GET_ADDRESS_JETTONS,
    GetAddressJettonsRequest(userAddress = userAddress, network = network, offset = offset, limit = limit),
)

internal suspend fun BridgeRpcClient.validateJettonAddress(address: String): Boolean =
    callTyped<ValidateJettonAddressResponse>(
        BridgeMethodConstants.METHOD_VALIDATE_JETTON_ADDRESS,
        ValidateJettonAddressRequest(address = address),
    ).valid
