package com.blockchain.transactions.swap.enteramount

import com.blockchain.commonarch.presentation.mvi_v2.Intent
import com.blockchain.transactions.swap.CryptoAccountWithBalance

sealed interface EnterAmountIntent : Intent<EnterAmountModelState> {
    object LoadData : EnterAmountIntent

    object FlipInputs : EnterAmountIntent

    data class FiatInputChanged(
        val amount: String
    ) : EnterAmountIntent

    data class CryptoInputChanged(
        val amount: String
    ) : EnterAmountIntent

    data class FromAccountChanged(
        val account: CryptoAccountWithBalance
    ) : EnterAmountIntent

    object MaxSelected : EnterAmountIntent

    object PreviewClicked : EnterAmountIntent
}
