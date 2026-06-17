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

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Detailed information about a Jetton master contract.
 *
 * @param address The Jetton master contract address
 * @param name The token name
 * @param symbol The token symbol
 * @param description The token description
 * @param decimals The number of decimal places used by the token
 * @param totalSupply Total supply in the token's smallest units
 * @param image URL of the token image
 * @param imageData Inline base64-encoded image data
 * @param uri URI pointing to the token metadata
 * @param verification
 * @param metadata Additional arbitrary metadata related to the jetton
 */
@Serializable
data class TONJettonInfo(

    /* The Jetton master contract address */
    @SerialName(value = "address")
    var address: kotlin.String,

    /* The token name */
    @SerialName(value = "name")
    var name: kotlin.String,

    /* The token symbol */
    @SerialName(value = "symbol")
    var symbol: kotlin.String,

    /* The token description */
    @SerialName(value = "description")
    var description: kotlin.String,

    /* The number of decimal places used by the token */
    @SerialName(value = "decimals")
    var decimals: kotlin.Int? = null,

    /* Total supply in the token's smallest units */
    @SerialName(value = "totalSupply")
    var totalSupply: kotlin.String? = null,

    /* URL of the token image */
    @SerialName(value = "image")
    var image: kotlin.String? = null,

    /* Inline base64-encoded image data */
    @SerialName(value = "image_data")
    var imageData: kotlin.String? = null,

    /* URI pointing to the token metadata */
    @SerialName(value = "uri")
    var uri: kotlin.String? = null,

    @SerialName(value = "verification")
    var verification: TONJettonVerification? = null,

    /* Additional arbitrary metadata related to the jetton */
    @Contextual @SerialName(value = "metadata")
    var metadata: kotlin.collections.Map<kotlin.String, kotlinx.serialization.json.JsonElement>? = null,

) {

    companion object
}
