package piuk.blockchain.android.ui.dashboard.sheets

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blockchain.commonarch.presentation.base.SlidingModalBottomDialog
import com.blockchain.componentlib.navigation.NavigationBarButton
import com.blockchain.componentlib.viewextensions.gone
import com.blockchain.componentlib.viewextensions.px
import com.blockchain.core.eligibility.models.TransactionsLimit
import com.blockchain.koin.scopedInject
import com.blockchain.nabu.Tier
import com.blockchain.nabu.UserIdentity
import com.google.android.material.shape.MaterialShapeDrawable
import com.google.android.material.shape.RoundedCornerTreatment
import com.google.android.material.shape.ShapeAppearanceModel
import com.google.android.material.tabs.TabLayoutMediator
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import io.reactivex.rxjava3.kotlin.subscribeBy
import piuk.blockchain.android.R
import piuk.blockchain.android.databinding.DialogSheetKycUpgradeNowBinding
import piuk.blockchain.android.ui.dashboard.assetdetails.AssetDetailsModel
import piuk.blockchain.android.ui.dashboard.assetdetails.ClearSheetDataIntent
import piuk.blockchain.android.ui.dashboard.assetdetails.NavigateToKyc

class KycUpgradeNowSheet : SlidingModalBottomDialog<DialogSheetKycUpgradeNowBinding>() {

    interface Host : SlidingModalBottomDialog.Host {
        fun startKycClicked()
    }

    private val disposables = CompositeDisposable()

    private val userIdentity: UserIdentity by scopedInject()

    private val initialTab: ViewPagerTab = ViewPagerTab.VERIFIED
    private lateinit var tabLayoutMediator: TabLayoutMediator

    private val transactionsLimit: TransactionsLimit by lazy {
        arguments?.getSerializable(ARG_TRANSACTIONS_LIMIT) as TransactionsLimit
    }

    private val isHostAssetDetailsFlow: Boolean by lazy {
        arguments?.getBoolean(ARG_IS_HOST_ASSET_DETAILS_FLOW, false) ?: false
    }

    // Only used when isHostAssetDetailsFlow == true, unfortunately we have to manipulate AssetDetailsModel when
    // being used from AssetDetailsFlow, because the host is Portfolio or Prices and not AssetDetailsFlow
    // so we have to communicate via the Model rather than relying on the host
    private val assetDetailsModel: AssetDetailsModel by scopedInject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // This is because we need to show this as a regular fragment as well as a BottomSheet
        if (!showsDialog) {
            val binding = initBinding(inflater, container)
            initControls(binding)
            return binding.root
        }
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun initBinding(inflater: LayoutInflater, container: ViewGroup?): DialogSheetKycUpgradeNowBinding =
        DialogSheetKycUpgradeNowBinding.inflate(inflater, container, false)

    override fun initControls(binding: DialogSheetKycUpgradeNowBinding): Unit = with(binding) {
        if (!showsDialog) {
            sheetIndicator.gone()
            toolbar.gone()
        } else {
            toolbar.apply {
                title = getString(R.string.upgrade_now)
                startNavigationBarButton = null
                endNavigationBarButtons = listOf(
                    NavigationBarButton.Icon(
                        drawable = R.drawable.ic_close_circle_v2,
                        color = null,
                        onIconClick = {
                            if (isHostAssetDetailsFlow) {
                                assetDetailsModel.process(ClearSheetDataIntent)
                                dismiss()
                            } else if (showsDialog) dismiss()
                        }
                    )
                )
            }
        }

        val indicatorDrawable = MaterialShapeDrawable(
            ShapeAppearanceModel()
                .toBuilder()
                .setAllCorners(RoundedCornerTreatment())
                .setAllCornerSizes(8.px.toFloat())
                .build()
        ).apply {
            initializeElevationOverlay(requireContext())
            elevation = 8f
            shadowCompatibilityMode = MaterialShapeDrawable.SHADOW_COMPAT_MODE_ALWAYS
        }

        tabLayout.setSelectedTabIndicator(indicatorDrawable)

        val viewPagerAdapter = KycCtaViewPagerAdapter(
            basicClicked = {
                startKycClicked()
            },
            verifyClicked = {
                startKycClicked()
            }
        ).apply {
            val initialItems = ViewPagerTab.values().toList().toItems(isBasicApproved = false)
            submitList(initialItems)
        }

        viewPager.adapter = viewPagerAdapter
        tabLayoutMediator = TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = when (ViewPagerTab.values()[position]) {
                ViewPagerTab.BASIC -> getString(R.string.kyc_upgrade_now_tab_basic)
                ViewPagerTab.VERIFIED -> getString(R.string.kyc_upgrade_now_tab_verified)
            }
        }
        tabLayoutMediator.attach()
        viewPager.setCurrentItem(ViewPagerTab.values().indexOf(initialTab), false)

        disposables += userIdentity.getHighestApprovedKycTier()
            .subscribeBy(
                onSuccess = {
                    val isAtleastSilver = it != Tier.BRONZE
                    val items = ViewPagerTab.values().toList().toItems(isBasicApproved = isAtleastSilver)
                    viewPagerAdapter.submitList(items)
                },
                onError = {}
            )
    }

    private fun startKycClicked() {
        if (isHostAssetDetailsFlow) {
            assetDetailsModel.process(NavigateToKyc)
            assetDetailsModel.process(ClearSheetDataIntent)
            dismiss()
        } else {
            (host as Host).startKycClicked()
            if (showsDialog) dismiss()
        }
    }

    override fun onCancel(dialog: DialogInterface) {
        if (isHostAssetDetailsFlow) assetDetailsModel.process(ClearSheetDataIntent)
        else super.onCancel(dialog)
    }

    override fun onDestroyView() {
        disposables.dispose()
        tabLayoutMediator.detach()
        super.onDestroyView()
    }

    enum class ViewPagerTab {
        BASIC,
        VERIFIED
    }

    private fun List<ViewPagerTab>.toItems(
        isBasicApproved: Boolean
    ): List<ViewPagerItem> = map {
        when (it) {
            ViewPagerTab.BASIC -> ViewPagerItem.Basic(isBasicApproved, transactionsLimit)
            ViewPagerTab.VERIFIED -> ViewPagerItem.Verified
        }
    }

    companion object {
        private const val ARG_TRANSACTIONS_LIMIT = "ARG_TRANSACTIONS_LIMIT"
        private const val ARG_IS_HOST_ASSET_DETAILS_FLOW = "ARG_IS_HOST_ASSET_DETAILS_FLOW"

        fun newInstance(
            transactionsLimit: TransactionsLimit = TransactionsLimit.Unlimited,
            isHostAssetDetailsFlow: Boolean = false
        ): KycUpgradeNowSheet = KycUpgradeNowSheet().apply {
            arguments = Bundle().apply {
                putSerializable(ARG_TRANSACTIONS_LIMIT, transactionsLimit)
                putBoolean(ARG_IS_HOST_ASSET_DETAILS_FLOW, isHostAssetDetailsFlow)
            }
        }
    }
}