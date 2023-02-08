package com.blockchain.home.presentation.navigation

import androidx.compose.runtime.Stable
import com.blockchain.coincore.AssetAction
import com.blockchain.coincore.CryptoAccount
import com.blockchain.domain.onboarding.CompletableDashboardOnboardingStep
import com.blockchain.domain.paymentmethods.model.FundsLocks
import com.blockchain.navigation.ActivityResultNavigation
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.Currency

@Stable
interface AssetActionsNavigation : ActivityResultNavigation {
    fun navigate(assetAction: AssetAction)
    fun receive(currency: String)
    fun buyCrypto(
        currency: AssetInfo,
        amount: String? = null,
        preselectedFiatTicker: String? = null,
        launchLinkCard: Boolean = false,
        launchNewPaymentMethodSelection: Boolean = false,
    )

    fun buyWithPreselectedMethod(paymentMethodId: String?)

    fun earnRewards()
    fun settings()
    fun fundsLocksDetail(fundsLocks: FundsLocks)
    fun coinview(asset: AssetInfo)
    fun coinview(asset: AssetInfo, recurringBuyId: String?, originScreen: String)
    fun onBoardingNavigation(initialSteps: List<CompletableDashboardOnboardingStep>)
    fun interestSummary(account: CryptoAccount)
    fun stakingSummary(currency: Currency)
    fun startKyc()
}
