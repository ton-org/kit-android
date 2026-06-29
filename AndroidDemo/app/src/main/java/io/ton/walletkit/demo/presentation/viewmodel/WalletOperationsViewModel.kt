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
package io.ton.walletkit.demo.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.ton.walletkit.ITONWallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for wallet-context operations such as switching the active wallet.
 * Sending tokens is handled by [SendTokensViewModel].
 */
class WalletOperationsViewModel(
    private val getWalletById: (String) -> ITONWallet?,
    private val onWalletSwitched: (String) -> Unit = {},
) : ViewModel() {

    private val _state = MutableStateFlow(WalletOperationsState())
    val state: StateFlow<WalletOperationsState> = _state.asStateFlow()

    data class WalletOperationsState(
        val activeWalletId: String? = null,
        val isSendingTransaction: Boolean = false,
        val error: String? = null,
        val successMessage: String? = null,
    )

    /**
     * Switch to a different wallet.
     */
    fun switchWallet(walletId: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(error = null)

            val wallet = getWalletById(walletId)
            if (wallet == null) {
                _state.value = _state.value.copy(error = "Wallet not found")
                return@launch
            }

            _state.value = _state.value.copy(activeWalletId = walletId)
            onWalletSwitched(walletId)

            Log.d(TAG, "Switched to wallet: $walletId")
        }
    }

    /**
     * Clear error or success message.
     */
    fun clearMessage() {
        _state.value = _state.value.copy(error = null, successMessage = null)
    }

    companion object {
        private const val TAG = "WalletOperationsVM"
    }
}
