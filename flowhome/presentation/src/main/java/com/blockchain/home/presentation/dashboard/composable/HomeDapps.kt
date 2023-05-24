package com.blockchain.home.presentation.dashboard.composable

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blockchain.componentlib.lazylist.paddedItem
import com.blockchain.componentlib.lazylist.paddedRoundedCornersItems
import com.blockchain.componentlib.tablerow.TableRowHeader
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.home.presentation.dapps.HomeDappsViewState
import com.blockchain.home.presentation.dapps.composable.WalletConnectDashboardCTA
import com.blockchain.walletconnect.ui.composable.common.DappSessionUiElement
import com.blockchain.walletconnect.ui.composable.common.WalletConnectDappTableRow
import org.koin.androidx.compose.get
import timber.log.Timber

internal fun LazyListScope.homeDapps(
    homeDappsState: HomeDappsViewState,
    onWalletConnectSeeAllSessionsClicked: () -> Unit,
    onDappSessionClicked: (DappSessionUiElement) -> Unit,
    openQrCodeScanner: () -> Unit
) {

    if (homeDappsState !is HomeDappsViewState.Loading) {
        paddedItem(
            paddingValues = PaddingValues(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.size(AppTheme.dimensions.largeSpacing))
            TableRowHeader(
                title = "Connected Apps",
                actionOnClick = onWalletConnectSeeAllSessionsClicked,
                actionTitle = if (homeDappsState is HomeDappsViewState.HomeDappsSessions) {
                    "See All"
                } else {
                    null
                },
            )
            Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))
        }
    }

    if (homeDappsState is HomeDappsViewState.NoSessions) {
        paddedItem(paddingValues = PaddingValues(horizontal = 16.dp)) {
            WalletConnectDashboardCTA(openQrCodeScanner)
        }
    } else if (homeDappsState is HomeDappsViewState.HomeDappsSessions) {
        paddedRoundedCornersItems(
            items = homeDappsState.connectedSessions,
            paddingValues = PaddingValues(horizontal = 16.dp)
        ) { session ->
            WalletConnectDappTableRow(
                session = session,
                onSessionClicked = {
                    Timber.d("Session clicked: $session")
                    onDappSessionClicked(session)
                }
            )
        }
    }
}
