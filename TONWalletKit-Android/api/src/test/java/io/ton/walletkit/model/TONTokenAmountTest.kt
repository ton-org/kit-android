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

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail
import org.junit.Test
import java.math.BigInteger

class TONTokenAmountTest {

    @Test
    fun `string and long constructors agree with BigInteger`() {
        val fromBig = TONTokenAmount(BigInteger.valueOf(100))
        val fromString = TONTokenAmount("100")
        val fromLong = TONTokenAmount(100L)

        assertEquals(fromBig, fromString)
        assertEquals(fromBig, fromLong)
        assertEquals("100", fromBig.value)
    }

    @Test
    fun `value and toString render the nano units`() {
        val amount = TONTokenAmount("1000000000")
        assertEquals("1000000000", amount.value)
        assertEquals("1000000000", amount.toString())
    }

    @Test
    fun `ZERO is zero`() {
        assertEquals(TONTokenAmount("0"), TONTokenAmount.ZERO)
        assertEquals("0", TONTokenAmount.ZERO.value)
    }

    @Test
    fun `amounts beyond Long range are preserved`() {
        val huge = "123456789012345678901234567890"
        assertEquals(huge, TONTokenAmount(huge).value)
    }

    @Test
    fun `negative amounts are allowed`() {
        assertEquals("-5", TONTokenAmount(-5L).value)
    }

    @Test
    fun `parseOrNull returns the amount for a valid string`() {
        assertEquals(TONTokenAmount("42"), TONTokenAmount.parseOrNull("42"))
    }

    @Test
    fun `parseOrNull returns null for an invalid string`() {
        assertNull(TONTokenAmount.parseOrNull("not-a-number"))
        assertNull(TONTokenAmount.parseOrNull(""))
    }

    @Test
    fun `string constructor throws on an invalid number`() {
        try {
            TONTokenAmount("not-a-number")
            fail("expected NumberFormatException")
        } catch (e: NumberFormatException) {
            // expected
        }
    }

    @Test
    fun `equals and hashCode are value-based`() {
        assertEquals(TONTokenAmount("100"), TONTokenAmount("100"))
        assertEquals(TONTokenAmount("100").hashCode(), TONTokenAmount("100").hashCode())
        assertNotEquals(TONTokenAmount("100"), TONTokenAmount("101"))
    }
}
