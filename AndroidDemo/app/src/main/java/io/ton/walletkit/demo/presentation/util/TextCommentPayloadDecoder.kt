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
package io.ton.walletkit.demo.presentation.util

import android.util.Base64

/**
 * Minimal BoC parser that decodes a TonConnect text-comment payload to its string: parse the root
 * cell, require a 32-bit op == 0, then read the UTF-8 tail (chained through the first ref for long
 * comments). Returns null when the payload isn't a plain text comment.
 *
 * Only the BoC features a TonConnect text comment uses are supported (standard magic,
 * non-exotic cells, root index 0, optional ref chain carrying the comment tail).
 */
object TextCommentPayloadDecoder {

    fun decode(base64: String): String? {
        val data = runCatching { Base64.decode(base64, Base64.DEFAULT) }.getOrNull() ?: return null
        if (data.size < 6) return null
        val bytes = IntArray(data.size) { data[it].toInt() and 0xFF }

        if (bytes[0] != 0xB5 || bytes[1] != 0xEE || bytes[2] != 0x9C || bytes[3] != 0x72) return null

        val flags = bytes[4]
        val hasIdx = (flags and 0x80) != 0
        val sizeBytes = flags and 0x07
        val offBytes = bytes[5]
        if (sizeBytes < 1 || sizeBytes > 4 || offBytes < 1 || offBytes > 8) return null

        var cursor = 6
        if (bytes.size < cursor + 3 * sizeBytes + offBytes) return null
        val cells = readUInt(bytes, cursor, sizeBytes)
        cursor += sizeBytes
        val roots = readUInt(bytes, cursor, sizeBytes)
        cursor += sizeBytes
        cursor += sizeBytes // absent
        cursor += offBytes // tot_cells_size

        if (cells <= 0 || roots <= 0) return null
        if (bytes.size < cursor + roots * sizeBytes) return null
        val rootIdx = readUInt(bytes, cursor, sizeBytes)
        cursor += roots * sizeBytes
        if (rootIdx != 0) return null

        if (hasIdx) {
            if (bytes.size < cursor + cells * offBytes) return null
            cursor += cells * offBytes
        }

        // Walk sequentially from the root to record each cell's offset.
        val cellOffsets = IntArray(cells)
        var walking = cursor
        for (i in 0 until cells) {
            cellOffsets[i] = walking
            val size = cellSize(bytes, walking, sizeBytes) ?: return null
            walking += size
        }

        return readText(bytes, cellOffsets, cellIndex = 0, sizeBytes = sizeBytes, isRoot = true)
    }

    private fun readText(
        bytes: IntArray,
        cellOffsets: IntArray,
        cellIndex: Int,
        sizeBytes: Int,
        isRoot: Boolean,
    ): String? {
        if (cellIndex < 0 || cellIndex >= cellOffsets.size) return null
        val offset = cellOffsets[cellIndex]
        if (offset + 2 > bytes.size) return null
        val d1 = bytes[offset]
        val d2 = bytes[offset + 1]

        val refsCount = d1 and 0x07
        val isExotic = (d1 and 0x08) != 0
        if (isExotic) return null

        val dataNibbles = d2
        val dataBytes = (dataNibbles + 1) / 2
        val hasPartialLastByte = (dataNibbles and 0x01) != 0

        if (offset + 2 + dataBytes + refsCount * sizeBytes > bytes.size) return null
        var data = bytes.copyOfRange(offset + 2, offset + 2 + dataBytes)

        // Partial-bit cells encode a 1-bit + zero padding in the last byte. TonConnect text
        // comments are byte-aligned; drop the padding byte conservatively if it's present.
        if (hasPartialLastByte && data.isNotEmpty()) {
            data = data.copyOfRange(0, data.size - 1)
        }

        val textBytes: IntArray
        if (isRoot) {
            if (data.size < 4) return null
            val op = (data[0] shl 24) or (data[1] shl 16) or (data[2] shl 8) or data[3]
            if (op != 0) return null
            textBytes = data.copyOfRange(4, data.size)
        } else {
            textBytes = data
        }

        var text = runCatching {
            String(ByteArray(textBytes.size) { textBytes[it].toByte() }, Charsets.UTF_8)
        }.getOrDefault("")

        if (refsCount > 0) {
            val refStart = offset + 2 + dataBytes
            val nextIdx = readUInt(bytes, refStart, sizeBytes)
            readText(bytes, cellOffsets, nextIdx, sizeBytes, isRoot = false)?.let { text += it }
        }

        return text.ifEmpty { null }
    }

    private fun cellSize(bytes: IntArray, start: Int, sizeBytes: Int): Int? {
        if (start + 2 > bytes.size) return null
        val d1 = bytes[start]
        val d2 = bytes[start + 1]
        val refsCount = d1 and 0x07
        val dataBytes = (d2 + 1) / 2
        return 2 + dataBytes + refsCount * sizeBytes
    }

    private fun readUInt(bytes: IntArray, offset: Int, length: Int): Int {
        var result = 0
        for (i in 0 until length) {
            result = (result shl 8) or bytes[offset + i]
        }
        return result
    }
}
