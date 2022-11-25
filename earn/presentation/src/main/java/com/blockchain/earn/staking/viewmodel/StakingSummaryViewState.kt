package com.blockchain.earn.staking.viewmodel

import com.blockchain.commonarch.presentation.mvi_v2.ViewState
import com.blockchain.earn.domain.models.EarnRewardsFrequency
import info.blockchain.balance.Currency
import info.blockchain.balance.Money

data class StakingSummaryViewState(
    val currency: Currency?,
    val errorState: StakingError,
    val isLoading: Boolean,
    val balanceCrypto: Money?,
    val balanceFiat: Money?,
    val stakedCrypto: Money?,
    val stakedFiat: Money?,
    val bondingCrypto: Money?,
    val bondingFiat: Money?,
    val earnedCrypto: Money?,
    val earnedFiat: Money?,
    val stakingRate: Double,
    val isWithdrawable: Boolean,
    val rewardsFrequency: EarnRewardsFrequency
) : ViewState

enum class StakingError {
    UnknownAsset,
    Other,
    None
}
