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

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Copyright (c) TonTech.  This source code is licensed under the MIT license found in the LICENSE file in the root directory of this source tree.
 *
 * Values: unknownError,badRequestError,manifestNotFoundError,manifestContentError,unknownAppError,userRejectsError,methodNotSupported
 */
@Serializable(with = TONConnectEventErrorCodesSerializer::class)
enum class TONConnectEventErrorCodes(val value: kotlin.Double) {

    unknownError(0.0),

    badRequestError(1.0),

    manifestNotFoundError(2.0),

    manifestContentError(3.0),

    unknownAppError(100.0),

    userRejectsError(300.0),

    methodNotSupported(400.0),
    ;

    /**
     * Override [toString()] to avoid using the enum variable name as the value, and instead use
     * the actual value defined in the API spec file.
     *
     * This solves a problem when the variable name and its value are different, and ensures that
     * the client sends the correct enum values to the server always.
     */
    override fun toString(): kotlin.String = value.toString()

    companion object {
        /**
         * Converts the provided [data] to a [String] on success, null otherwise.
         */
        fun encode(data: kotlin.Any?): kotlin.String? = if (data is TONConnectEventErrorCodes) "$data" else null

        /**
         * Returns a valid [TONConnectEventErrorCodes] for [data], null otherwise.
         */
        fun decode(data: kotlin.Any?): TONConnectEventErrorCodes? = data?.let {
            val normalizedData = "$it".lowercase()
            values().firstOrNull { value ->
                it == value || normalizedData == "$value".lowercase()
            }
        }
    }
}

internal object TONConnectEventErrorCodesSerializer : KSerializer<TONConnectEventErrorCodes> {
    override val descriptor = kotlin.Double.serializer().descriptor

    override fun deserialize(decoder: Decoder): TONConnectEventErrorCodes {
        val value = decoder.decodeSerializableValue(kotlin.Double.serializer())
        return TONConnectEventErrorCodes.values().firstOrNull { it.value == value }
            ?: throw IllegalArgumentException("Unknown enum value: $value")
    }

    override fun serialize(encoder: Encoder, value: TONConnectEventErrorCodes) {
        encoder.encodeSerializableValue(kotlin.Double.serializer(), value.value)
    }
}
