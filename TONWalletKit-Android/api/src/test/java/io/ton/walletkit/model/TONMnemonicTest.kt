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
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TONMnemonicTest {

    private fun words(count: Int): List<String> = (1..count).map { "word$it" }

    @Test
    fun `empty mnemonic has max capacity and no filled words`() {
        val mnemonic = TONMnemonic.empty()

        assertEquals(TONMnemonicLength.MAX.wordCount, mnemonic.value.size)
        assertEquals(0, mnemonic.filledWordCount)
        assertFalse(mnemonic.isFilled)
    }

    @Test
    fun `fromWords pads to max capacity`() {
        val mnemonic = TONMnemonic.fromWords(words(12))

        assertEquals(TONMnemonicLength.MAX.wordCount, mnemonic.value.size)
        assertEquals(12, mnemonic.filledWordCount)
    }

    @Test
    fun `a 12-word mnemonic is filled`() {
        assertTrue(TONMnemonic.fromWords(words(12)).isFilled)
    }

    @Test
    fun `a 24-word mnemonic is filled`() {
        val mnemonic = TONMnemonic.fromWords(words(24))
        assertEquals(24, mnemonic.filledWordCount)
        assertTrue(mnemonic.isFilled)
    }

    @Test
    fun `an invalid word count is not filled`() {
        assertFalse(TONMnemonic.fromWords(words(5)).isFilled)
    }

    @Test
    fun `fromPhrase splits on spaces`() {
        val mnemonic = TONMnemonic.fromPhrase("alpha bravo charlie")

        assertEquals(3, mnemonic.filledWordCount)
        assertEquals("alpha", mnemonic.getWord(0))
        assertEquals("charlie", mnemonic.getWord(2))
        assertFalse(mnemonic.isFilled)
    }

    @Test
    fun `updateWord changes a word in range and ignores out-of-range`() {
        val mnemonic = TONMnemonic.empty()

        mnemonic.updateWord(0, "first")
        assertEquals("first", mnemonic.getWord(0))

        // Out of range is a no-op, not a crash.
        mnemonic.updateWord(999, "ignored")
        assertEquals(1, mnemonic.filledWordCount)
    }

    @Test
    fun `getWord returns empty string out of bounds`() {
        assertEquals("", TONMnemonic.empty().getWord(999))
    }

    @Test
    fun `toPhrase joins only the filled words`() {
        assertEquals("alpha bravo charlie", TONMnemonic.fromPhrase("alpha bravo charlie").toPhrase())
        assertEquals("first", TONMnemonic.empty().also { it.updateWord(0, "first") }.toPhrase())
    }

    @Test
    fun `equals and hashCode are content-based`() {
        val a = TONMnemonic.fromWords(words(12))
        val b = TONMnemonic.fromWords(words(12))

        assertEquals(a, b)
        assertEquals(a.hashCode(), b.hashCode())
        assertNotEquals(a, TONMnemonic.fromWords(words(24)))
    }

    @Test
    fun `TONMnemonicLength maps word counts`() {
        assertEquals(TONMnemonicLength.BITS_128, TONMnemonicLength.fromWordCount(12))
        assertEquals(TONMnemonicLength.BITS_256, TONMnemonicLength.fromWordCount(24))
        assertNull(TONMnemonicLength.fromWordCount(13))
        assertEquals(TONMnemonicLength.BITS_256, TONMnemonicLength.MAX)
    }
}
