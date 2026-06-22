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

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for [io.ton.walletkit.engine.operations] gasless functions — the bridge contract for
 * gasless provider creation, registration, and the provider registry. These pin the
 * `METHOD_*_GASLESS_*` constants against the JS bridge handler keys (a mismatch fails silently
 * at runtime). Mirrors [SwapOperationsTest] at the bridge protocol layer.
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE, sdk = [28])
class GaslessOperationsTest : OperationsTestBase() {

    companion object {
        const val PROVIDER_ID = "tonapi"
    }

    // --- createTonApiGaslessProvider ---

    @Test
    fun createTonApiGaslessProvider_returnsProviderId() = runBlocking {
        givenBridgeReturns(buildJsonObject { put("providerId", PROVIDER_ID) })

        val id = rpcClient.createTonApiGaslessProvider(null)

        assertEquals(PROVIDER_ID, id)
        assertEquals("createTonApiGaslessProvider", capturedMethod)
    }

    // --- register / remove / setDefault ---

    @Test
    fun registerGaslessProvider_sendsProviderId() = runBlocking {
        rpcClient.registerGaslessProvider(PROVIDER_ID)

        assertEquals("registerGaslessProvider", capturedMethod)
        val encoded = encodeCapturedParams() as JsonObject
        assertEquals(PROVIDER_ID, (encoded["providerId"] as JsonPrimitive).content)
    }

    @Test
    fun removeGaslessProvider_sendsProviderId() = runBlocking {
        rpcClient.removeGaslessProvider(PROVIDER_ID)

        assertEquals("removeGaslessProvider", capturedMethod)
        val encoded = encodeCapturedParams() as JsonObject
        assertEquals(PROVIDER_ID, (encoded["providerId"] as JsonPrimitive).content)
    }

    @Test
    fun setDefaultGaslessProvider_sendsProviderId() = runBlocking {
        rpcClient.setDefaultGaslessProvider(PROVIDER_ID)

        assertEquals("setDefaultGaslessProvider", capturedMethod)
        val encoded = encodeCapturedParams() as JsonObject
        assertEquals(PROVIDER_ID, (encoded["providerId"] as JsonPrimitive).content)
    }

    // --- getRegisteredGaslessProviders ---

    @Test
    fun getRegisteredGaslessProviders_returnsProviderIds() = runBlocking {
        givenBridgeReturns(
            buildJsonObject {
                put(
                    "providerIds",
                    buildJsonArray {
                        add("tonapi")
                        add("custom")
                    },
                )
            },
        )

        val ids = rpcClient.getRegisteredGaslessProviders()

        assertEquals(listOf("tonapi", "custom"), ids)
        assertEquals("getRegisteredGaslessProviders", capturedMethod)
    }

    @Test
    fun getRegisteredGaslessProviders_emptyProviders() = runBlocking {
        givenBridgeReturns(buildJsonObject { put("providerIds", JsonArray(emptyList())) })

        assertTrue(rpcClient.getRegisteredGaslessProviders().isEmpty())
    }

    // --- hasGaslessProvider ---

    @Test
    fun hasGaslessProvider_returnsTrueWhenResultTrue() = runBlocking {
        givenBridgeReturns(buildJsonObject { put("result", true) })

        assertTrue(rpcClient.hasGaslessProvider(PROVIDER_ID))
        assertEquals("hasGaslessProvider", capturedMethod)
    }

    @Test
    fun hasGaslessProvider_returnsFalseWhenResultFalse() = runBlocking {
        givenBridgeReturns(buildJsonObject { put("result", false) })

        assertFalse(rpcClient.hasGaslessProvider(PROVIDER_ID))
    }

    // --- getGaslessProviderSupportedNetworks ---

    @Test
    fun getGaslessProviderSupportedNetworks_decodesNetworks() = runBlocking {
        givenBridgeReturns(
            buildJsonObject {
                put(
                    "networks",
                    buildJsonArray {
                        add(buildJsonObject { put("chainId", "-239") })
                        add(buildJsonObject { put("chainId", "-3") })
                    },
                )
            },
        )

        val networks = rpcClient.getGaslessProviderSupportedNetworks(PROVIDER_ID)

        assertEquals(2, networks.size)
        assertEquals("-239", networks[0].chainId)
        assertEquals("-3", networks[1].chainId)
        assertEquals("getGaslessProviderSupportedNetworks", capturedMethod)
    }
}
