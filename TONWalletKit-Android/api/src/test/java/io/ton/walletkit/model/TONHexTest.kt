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

import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Test

class TONHexTest {

    @Test
    fun `value is preserved verbatim`() {
        assertEquals("0x1234", TONHex("0x1234").value)
        assertEquals("abcd", TONHex("abcd").value)
    }

    @Test
    fun `rawValue strips a 0x or 0X prefix`() {
        assertEquals("1234abcd", TONHex("0x1234abcd").rawValue)
        assertEquals("1234abcd", TONHex("0X1234abcd").rawValue)
        assertEquals("1234abcd", TONHex("1234abcd").rawValue)
    }

    @Test
    fun `withPrefix adds 0x only when absent`() {
        assertEquals("0xabcd", TONHex("abcd").withPrefix)
        assertEquals("0xabcd", TONHex("0xabcd").withPrefix)
        assertEquals("0Xabcd", TONHex("0Xabcd").withPrefix)
    }

    @Test
    fun `data decodes valid hex with and without prefix`() {
        assertArrayEquals(byteArrayOf(0x12, 0x34), TONHex("0x1234").data)
        assertArrayEquals(byteArrayOf(0x12, 0x34), TONHex("1234").data)
    }

    @Test
    fun `data decodes high bytes to negative byte values`() {
        assertArrayEquals(byteArrayOf(0xFF.toByte(), 0xAB.toByte()), TONHex("0xffab").data)
    }

    @Test
    fun `data of an empty hex is an empty array`() {
        assertArrayEquals(ByteArray(0), TONHex("0x").data)
        assertArrayEquals(ByteArray(0), TONHex("").data)
    }

    @Test
    fun `data is null for odd-length hex`() {
        assertNull(TONHex("0x123").data)
    }

    @Test
    fun `data is null for non-hex characters`() {
        assertNull(TONHex("0xzz").data)
    }

    @Test
    fun `fromData encodes bytes with a 0x prefix by default`() {
        assertEquals("0x1234ff", TONHex.fromData(byteArrayOf(0x12, 0x34, 0xFF.toByte())).value)
    }

    @Test
    fun `fromData can omit the prefix`() {
        assertEquals("1234ff", TONHex.fromData(byteArrayOf(0x12, 0x34, 0xFF.toByte()), withPrefix = false).value)
    }

    @Test
    fun `fromString encodes UTF-8 bytes`() {
        // "ab" -> 0x61 0x62
        assertEquals("0x6162", TONHex.fromString("ab").value)
    }

    @Test
    fun `fromData and data round-trip arbitrary bytes`() {
        val bytes = byteArrayOf(0x00, 0x12, 0x7F, 0xFF.toByte(), 0xAB.toByte(), 0x80.toByte())
        assertArrayEquals(bytes, TONHex.fromData(bytes).data)
    }

    @Test
    fun `equals and hashCode are value-based`() {
        assertEquals(TONHex("0x1234"), TONHex("0x1234"))
        assertEquals(TONHex("0x1234").hashCode(), TONHex("0x1234").hashCode())
        assertNotEquals(TONHex("0x1234"), TONHex("0x5678"))
        // Equality is on the raw stored string, so prefixed vs unprefixed are distinct values.
        assertNotEquals(TONHex("0x1234"), TONHex("1234"))
    }

    @Test
    fun `toString returns the stored value`() {
        assertEquals("0x1234", TONHex("0x1234").toString())
    }
}
