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
 * Static metadata for a crypto-onramp provider.
 *
 * @param name Human-readable provider name (e.g. 'Decent')
 * @param logo URL to the provider's logo image
 * @param url URL to the provider's website
 * @param refundAddressMode Refund-address collection mode for this provider: - `'off'` (default): no refund address — the UI skips the address modal entirely. - `'optional'`: the UI shows the address modal with a \"Skip\" button — users may   enter an address or proceed without one. - `'required'`: the UI shows the address modal and blocks submission until a   non-empty address is entered.
 * @param isReversedAmountSupported Whether this provider supports reversed (target-amount) quotes. When false, the UI should hide the direction toggle and only allow source-amount input.
 */
@Serializable
data class TONCryptoOnrampProviderMetadata(

    /* Human-readable provider name (e.g. 'Decent') */
    @SerialName(value = "name")
    var name: kotlin.String,

    /* URL to the provider's logo image */
    @SerialName(value = "logo")
    var logo: kotlin.String? = null,

    /* URL to the provider's website */
    @SerialName(value = "url")
    var url: kotlin.String? = null,

    /* Refund-address collection mode for this provider: - `'off'` (default): no refund address — the UI skips the address modal entirely. - `'optional'`: the UI shows the address modal with a \"Skip\" button — users may   enter an address or proceed without one. - `'required'`: the UI shows the address modal and blocks submission until a   non-empty address is entered. */
    @SerialName(value = "refundAddressMode")
    var refundAddressMode: TONCryptoOnrampProviderMetadata.RefundAddressMode? = null,

    /* Whether this provider supports reversed (target-amount) quotes. When false, the UI should hide the direction toggle and only allow source-amount input. */
    @SerialName(value = "isReversedAmountSupported")
    var isReversedAmountSupported: kotlin.Boolean? = null,

) {

    companion object

    /**
     * Refund-address collection mode for this provider: - `'off'` (default): no refund address — the UI skips the address modal entirely. - `'optional'`: the UI shows the address modal with a \"Skip\" button — users may   enter an address or proceed without one. - `'required'`: the UI shows the address modal and blocks submission until a   non-empty address is entered.
     *
     * Values: off,optional,required
     */
    @Serializable
    enum class RefundAddressMode(val value: kotlin.String) {
        @SerialName(value = "off")
        off("off"),

        @SerialName(value = "optional")
        optional("optional"),

        @SerialName(value = "required")
        required("required"),
    }
}
