package piuk.blockchain.android.ui.launcher

import android.app.Application
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import com.blockchain.coincore.AssetAction
import com.blockchain.deeplinking.navigation.DeeplinkRedirector
import com.blockchain.deeplinking.navigation.Destination
import com.blockchain.deeplinking.processor.DeepLinkResult
import com.blockchain.extensions.exhaustive
import com.blockchain.notifications.NotificationsUtil
import com.blockchain.notifications.analytics.Analytics
import com.blockchain.remoteconfig.IntegratedFeatureFlag
import com.blockchain.walletconnect.domain.WalletConnectServiceAPI
import com.blockchain.walletconnect.domain.WalletConnectUserEvent
import info.blockchain.balance.AssetCatalogue
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.plusAssign
import piuk.blockchain.android.R
import piuk.blockchain.android.simplebuy.SimpleBuyActivity
import piuk.blockchain.android.ui.dashboard.coinview.CoinViewActivity
import piuk.blockchain.android.ui.home.MainActivity
import piuk.blockchain.android.ui.transactionflow.flow.TransactionFlowActivity
import timber.log.Timber

class GlobalEventHandler(
    private val application: Application,
    private val walletConnectServiceAPI: WalletConnectServiceAPI,
    private val wcFeatureFlag: IntegratedFeatureFlag,
    private val deeplinkFeatureFlag: IntegratedFeatureFlag,
    private val deeplinkRedirector: DeeplinkRedirector,
    private val assetCatalogue: AssetCatalogue,
    private val notificationManager: NotificationManager,
    private val analytics: Analytics
) {
    private val compositeDisposable = CompositeDisposable()

    fun init() {
        compositeDisposable.clear()
        compositeDisposable += wcFeatureFlag.enabled.flatMapObservable { enabled ->
            if (enabled) walletConnectServiceAPI.userEvents
            else Observable.empty()
        }.subscribe { event ->
            startTransactionFlowForSigning(event)
        }

        compositeDisposable += deeplinkRedirector.deeplinkEvents.subscribe { deeplinkResult ->
            navigateToDeeplinkDestination(deeplinkResult)
        }
    }

    private fun navigateToDeeplinkDestination(deeplinkResult: DeepLinkResult.DeepLinkResultSuccess) {
        var intent: Intent? = null

        val destination = deeplinkResult.destination
        when (destination) {
            is Destination.AssetViewDestination -> {
                val assetInfo = assetCatalogue.assetInfoFromNetworkTicker(destination.networkTicker)
                if (assetInfo != null) {
                    intent =  CoinViewActivity.newIntent(
                            context = application,
                            asset = assetInfo
                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                } else {
                    Timber.e("Unable to start CoinViewActivity from deeplink. AssetInfo is null")
                }
            }

            is Destination.AssetBuyDestination -> {
                val assetInfo = assetCatalogue.assetInfoFromNetworkTicker(destination.code)
                if (assetInfo != null) {
                    intent = SimpleBuyActivity.newIntent(
                            context = application,
                            asset = assetInfo
                    )
                } else {
                    Timber.e("Unable to start SimpleBuyActivity from deeplink. AssetInfo is null")
                }
            }

            is Destination.ActivityDestination -> {
                intent =
                    MainActivity.newIntent(
                        context = application,
                        pendingDestination = destination
                    )

            }
        }.exhaustive

        if (intent != null) {
            val pendingIntent = PendingIntent.getActivity(
                application,
                0,
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                PendingIntent.FLAG_UPDATE_CURRENT
            )

            if (pendingIntent != null) {
                NotificationsUtil(
                    context = application,
                    notificationManager = notificationManager,
                    analytics = analytics
                ).triggerNotification(
                    title = deeplinkResult.notificationPayload?.title ?: "",
                    marquee = deeplinkResult.notificationPayload?.title ?: "",
                    text = deeplinkResult.notificationPayload?.body ?: "",
                    pendingIntent = pendingIntent,
                    id = NotificationsUtil.ID_BACKGROUND_NOTIFICATION,
                    appName = R.string.app_name,
                    colorRes = R.color.primary_navy_medium
                )
            }
        }
    }

    private fun startTransactionFlowForSigning(event: WalletConnectUserEvent) {
        val intent = TransactionFlowActivity.newInstance(
            application,
            sourceAccount = event.source,
            target = event.target,
            action = AssetAction.Sign
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        application.startActivity(intent)
    }
}
