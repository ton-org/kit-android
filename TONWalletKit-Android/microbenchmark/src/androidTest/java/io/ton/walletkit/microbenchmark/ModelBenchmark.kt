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
package io.ton.walletkit.microbenchmark

import androidx.benchmark.junit4.BenchmarkRule
import androidx.benchmark.junit4.measureRepeated
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.ton.walletkit.model.TONBase64
import io.ton.walletkit.model.TONHex
import io.ton.walletkit.model.TONRawAddress
import io.ton.walletkit.model.TONTokenAmount
import io.ton.walletkit.model.TONTokenAmountFormatter
import io.ton.walletkit.model.TONUserFriendlyAddress
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Per-method cost of the SDK's deterministic public value types. Run with
 * `./gradlew :microbenchmark:connectedReleaseAndroidTest`. Bridge/network-bound SDK methods are not
 * benchmarkable here (they measure latency, not code) — profile those with Perfetto instead.
 */
@RunWith(AndroidJUnit4::class)
class ModelBenchmark {

    @get:Rule
    val benchmarkRule = BenchmarkRule()

    private val sampleAddress = TONUserFriendlyAddress(
        TONRawAddress(workchain = 0, hash = ByteArray(32) { (it + 1).toByte() }),
    )
    private val addressValue = sampleAddress.value
    private val bytes32 = ByteArray(32) { it.toByte() }
    private val hexString = TONHex.fromData(bytes32, withPrefix = false).value
    private val base64String = TONBase64.fromData(bytes32).value
    private val formatter = TONTokenAmountFormatter()
    private val tokenAmount = TONTokenAmount("123456789012")

    @Test
    fun parseUserFriendlyAddress() = benchmarkRule.measureRepeated {
        TONUserFriendlyAddress.parseUserFriendly(addressValue)
    }

    @Test
    fun addressToBounceableForm() = benchmarkRule.measureRepeated {
        sampleAddress.toString(isBounceable = false)
    }

    @Test
    fun addressToRaw() = benchmarkRule.measureRepeated {
        sampleAddress.raw
    }

    @Test
    fun hexEncode() = benchmarkRule.measureRepeated {
        TONHex.fromData(bytes32, withPrefix = false)
    }

    @Test
    fun hexDecode() = benchmarkRule.measureRepeated {
        TONHex(hexString).data
    }

    @Test
    fun base64Encode() = benchmarkRule.measureRepeated {
        TONBase64.fromData(bytes32)
    }

    @Test
    fun base64Decode() = benchmarkRule.measureRepeated {
        TONBase64(base64String).data
    }

    @Test
    fun tokenAmountParse() = benchmarkRule.measureRepeated {
        formatter.amount("123.456789012")
    }

    @Test
    fun tokenAmountFormat() = benchmarkRule.measureRepeated {
        formatter.string(tokenAmount)
    }
}
