package com.blockchain.home.presentation.dashboard.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Divider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.blockchain.coincore.NullFiatAccount
import com.blockchain.componentlib.R
import com.blockchain.componentlib.basic.Image
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.system.ShimmerLoadingCard
import com.blockchain.componentlib.tablerow.BalanceChangeTableRow
import com.blockchain.componentlib.tablerow.ValueChange
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.theme.Grey700
import com.blockchain.componentlib.utils.clickableNoEffect
import com.blockchain.componentlib.utils.collectAsStateLifecycleAware
import com.blockchain.data.DataResource
import com.blockchain.data.map
import com.blockchain.home.presentation.SectionSize
import com.blockchain.home.presentation.allassets.AssetsIntent
import com.blockchain.home.presentation.allassets.AssetsViewModel
import com.blockchain.home.presentation.allassets.AssetsViewState
import com.blockchain.home.presentation.allassets.CustodialAssetState
import com.blockchain.home.presentation.allassets.FiatAssetState
import com.blockchain.home.presentation.allassets.HomeAsset
import com.blockchain.home.presentation.allassets.HomeCryptoAsset
import com.blockchain.home.presentation.allassets.composable.CryptoAssetsList
import com.blockchain.home.presentation.navigation.AssetActionsNavigation
import com.blockchain.koin.payloadScope
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.CryptoCurrency
import info.blockchain.balance.FiatCurrency.Companion.Dollars
import info.blockchain.balance.Money
import org.koin.androidx.compose.getViewModel

@Composable
fun HomeAssets(
    viewModel: AssetsViewModel = getViewModel(scope = payloadScope),
    assetActionsNavigation: AssetActionsNavigation,
    openAllAssets: () -> Unit,
    openFiatActionDetail: (String) -> Unit
) {
    val lifecycleOwner = LocalLifecycleOwner.current

    val viewState: AssetsViewState by viewModel.viewState.collectAsStateLifecycleAware()

    DisposableEffect(key1 = viewModel) {
        viewModel.onIntent(AssetsIntent.LoadAccounts(SectionSize.Limited()))
        onDispose { }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onIntent(AssetsIntent.LoadFilters)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    HomeAssetsScreen(
        assets = viewState.assets,
        onSeeAllCryptoAssetsClick = openAllAssets,
        onAssetClick = { asset ->
            assetActionsNavigation.coinview(asset)
        },
        openFiatActionDetail = openFiatActionDetail
    )
}

@Composable
fun HomeAssetsScreen(
    assets: DataResource<List<HomeAsset>>,
    onSeeAllCryptoAssetsClick: () -> Unit,
    onAssetClick: (AssetInfo) -> Unit,
    openFiatActionDetail: (String) -> Unit
) {
    when (assets) {
        DataResource.Loading -> AssetsLoading()
        is DataResource.Data -> HomeAssetsList(
            assets = assets.data,
            onSeeAllCryptoAssetsClick = onSeeAllCryptoAssetsClick,
            onAssetClick = onAssetClick,
            openFiatActionDetail = openFiatActionDetail
        )
        is DataResource.Error -> {
            TODO("Render error")
        }
    }
}

@Composable
private fun AssetsLoading() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.dimensions.smallSpacing)
    ) {
        ShimmerLoadingCard()
    }
}

@Composable
private fun HomeAssetsList(
    assets: List<HomeAsset>,
    onSeeAllCryptoAssetsClick: () -> Unit,
    onAssetClick: (AssetInfo) -> Unit,
    openFiatActionDetail: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(AppTheme.dimensions.smallSpacing)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            if (assets.filterIsInstance<HomeCryptoAsset>().isNotEmpty()) {
                Text(
                    text = stringResource(R.string.ma_home_assets_title),
                    style = AppTheme.typography.body2,
                    color = Grey700
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    modifier = Modifier.clickableNoEffect(onSeeAllCryptoAssetsClick),
                    text = stringResource(R.string.see_all),
                    style = AppTheme.typography.paragraph2,
                    color = AppTheme.colors.primary,
                )
            }
            Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))
        }

        Spacer(modifier = Modifier.size(AppTheme.dimensions.tinySpacing))

        CryptoAssetsList(
            cryptoAssets = assets.filterIsInstance<HomeCryptoAsset>(),
            onAssetClick = onAssetClick
        )

        val fiats = assets.filterIsInstance<FiatAssetState>()
        if (fiats.isNotEmpty())
            FiatAssetsStateList(
                assets = fiats,
                openFiatActionDetail = openFiatActionDetail
            )
    }
}

@Composable
private fun FiatAssetsStateList(
    assets: List<FiatAssetState>,
    openFiatActionDetail: (String) -> Unit
) {
    Spacer(modifier = Modifier.size(AppTheme.dimensions.smallSpacing))
    Card(
        backgroundColor = AppTheme.colors.background,
        shape = RoundedCornerShape(AppTheme.dimensions.mediumSpacing),
        elevation = 0.dp
    ) {
        Column {
            assets.forEachIndexed { index, fiatAsset ->
                BalanceChangeTableRow(
                    name = fiatAsset.name,
                    value = fiatAsset.balance.map {
                        it.toStringWithSymbol()
                    },
                    contentStart = {
                        Image(
                            imageResource = ImageResource.Remote(fiatAsset.icon[0]),
                            modifier = Modifier
                                .align(Alignment.CenterVertically)
                                .size(dimensionResource(R.dimen.standard_spacing)),
                            defaultShape = CircleShape
                        )
                    },
                    onClick = {
                        openFiatActionDetail(fiatAsset.account.currency.networkTicker)
                    }
                )

                if (index < assets.lastIndex) {
                    Divider(color = Color(0XFFF1F2F7))
                }
            }
        }
    }
}

@Preview(backgroundColor = 0xFF272727)
@Composable
fun PreviewHomeAccounts() {
    HomeAssetsScreen(
        assets = DataResource.Data(
            listOf(
                CustodialAssetState(
                    icon = listOf(""),
                    name = "Ethereum",
                    balance = DataResource.Data(Money.fromMajor(Dollars, 128.toBigDecimal())),
                    change = DataResource.Data(ValueChange.Up(3.94)),
                    fiatBalance = DataResource.Data(Money.fromMajor(Dollars, 112328.toBigDecimal())),
                    asset = CryptoCurrency.ETHER
                ),
                CustodialAssetState(
                    icon = listOf(""),
                    name = "Bitcoin",
                    balance = DataResource.Loading,
                    change = DataResource.Loading,
                    fiatBalance = DataResource.Loading,
                    asset = CryptoCurrency.ETHER
                ),
                CustodialAssetState(
                    icon = listOf(""),
                    name = "Solana",
                    balance = DataResource.Data(Money.fromMajor(Dollars, 555.28.toBigDecimal())),
                    change = DataResource.Data(ValueChange.Down(2.32)),
                    fiatBalance = DataResource.Data(Money.fromMajor(Dollars, 1.28.toBigDecimal())),
                    asset = CryptoCurrency.ETHER
                )
            ) +

                listOf(
                    FiatAssetState(
                        icon = listOf(""),
                        name = "US Dollar",
                        balance = DataResource.Data(Money.fromMajor(Dollars, 123.28.toBigDecimal())),
                        fiatBalance = DataResource.Data(Money.fromMajor(Dollars, 123.28.toBigDecimal())),
                        account = NullFiatAccount
                    ),
                    FiatAssetState(
                        icon = listOf(""),
                        name = "Euro",
                        balance = DataResource.Loading,
                        fiatBalance = DataResource.Loading,
                        account = NullFiatAccount
                    )
                )
        ),
        onSeeAllCryptoAssetsClick = {},
        onAssetClick = {},
        openFiatActionDetail = {}
    )
}

@Preview(backgroundColor = 0xFF272727)
@Composable
fun PreviewHomeAccounts_Loading() {
    HomeAssetsScreen(
        assets = DataResource.Loading,
        onSeeAllCryptoAssetsClick = {},
        onAssetClick = {},
        openFiatActionDetail = {}
    )
}
