package piuk.blockchain.android.ui.interest.domain.model

import com.blockchain.domain.eligibility.model.EarnRewardsEligibility
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.Money

data class InterestAsset(
    val assetInfo: AssetInfo,
    val interestDetail: AssetInterestDetail?
)

data class AssetInterestDetail(
    val totalInterest: Money,
    val totalBalance: Money,
    val rate: Double,
    val eligibility: EarnRewardsEligibility,
    val totalBalanceFiat: Money
)
