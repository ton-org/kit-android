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
@file:Suppress(
    "ArrayInDataClass",
    "EnumEntryName",
    "RemoveRedundantQualifierName",
    "UnusedImport",
)

package io.ton.walletkit.api.generated

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Verification status of a Jetton.
 *
 * @param verified Indicates whether the jetton has been verified
 * @param source Source of the verification
 * @param warnings Warnings associated with the jetton
 */
@Serializable
data class TONJettonVerification(

    /* Indicates whether the jetton has been verified */
    @SerialName(value = "verified")
    var verified: kotlin.Boolean,

    /* Source of the verification */
    @SerialName(value = "source")
    var source: TONJettonVerification.Source? = null,

    /* Warnings associated with the jetton */
    @SerialName(value = "warnings")
    var warnings: kotlin.collections.List<kotlin.String>? = null,

) {

    companion object

    /**
     * Source of the verification
     *
     * Values: toncenter,community,manual
     */
    @Serializable
    enum class Source(val value: kotlin.String) {
        @SerialName(value = "toncenter")
        toncenter("toncenter"),

        @SerialName(value = "community")
        community("community"),

        @SerialName(value = "manual")
        manual("manual"),
    }
}
