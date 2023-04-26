package com.blockchain.transactions.swap.selecttarget

import com.blockchain.commonarch.presentation.mvi_v2.Intent

sealed interface SelectTargetIntent : Intent<SelectTargetModelState> {
    object LoadData : SelectTargetIntent
    data class AssetSelected(val ticker: String) : SelectTargetIntent
}
