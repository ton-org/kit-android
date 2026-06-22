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
package io.ton.walletkit.demo.presentation.ui.screen.iframesec

import androidx.compose.runtime.mutableStateListOf
import java.util.UUID

/**
 * One diagnostic log row. [actualOrigin] is the platform-reported origin of the frame that posted
 * (the ground truth — JS cannot forge it). [claimedOrigin] is whatever the frame's JS claimed.
 * [isNative] marks rows that come from the native SDK event stream rather than a JS bridge message.
 */
data class IframeSecLogEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val frameLabel: String,
    val action: String,
    val claimedOrigin: String,
    val actualOrigin: String,
    val isMainFrame: Boolean,
    val payload: String = "",
    val isNative: Boolean = false,
)

/** Compose-observable log shared by the synthetic and real-bridge iframe-security screens. */
class IframeSecLog {
    val entries = mutableStateListOf<IframeSecLogEntry>()

    fun add(entry: IframeSecLogEntry) {
        entries.add(entry)
    }

    /**
     * Append an entry describing an event the native SDK actually received and surfaced — proof
     * the bridge accepted the request, plus the [domain] the SDK attributed to it.
     */
    fun addNative(action: String, domain: String, payload: String = "") {
        entries.add(
            IframeSecLogEntry(
                frameLabel = "SDK EVENT",
                action = action,
                claimedOrigin = domain,
                actualOrigin = domain,
                isMainFrame = true,
                payload = payload,
                isNative = true,
            ),
        )
    }

    fun clear() {
        entries.clear()
    }
}
