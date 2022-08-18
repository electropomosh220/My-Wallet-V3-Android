package piuk.blockchain.android.ui.coinview.presentation

import com.blockchain.commonarch.presentation.mvi_v2.Intent

sealed interface CoinviewIntents : Intent<CoinviewModelState> {
    /**
     * Triggers loading:
     * * asset price / chart values
     * * todo
     */
    object LoadData : CoinviewIntents
}
