package com.blockchain.prices.prices

import androidx.lifecycle.viewModelScope
import com.blockchain.coincore.Coincore
import com.blockchain.commonarch.presentation.mvi_v2.ModelConfigArgs
import com.blockchain.commonarch.presentation.mvi_v2.MviViewModel
import com.blockchain.commonarch.presentation.mvi_v2.NavigationEvent
import com.blockchain.componentlib.tablerow.ValueChange
import com.blockchain.core.buy.domain.SimpleBuyService
import com.blockchain.core.price.ExchangeRatesDataManager
import com.blockchain.data.DataResource
import com.blockchain.data.FreshnessStrategy
import com.blockchain.data.dataOrElse
import com.blockchain.data.filter
import com.blockchain.data.map
import com.blockchain.data.mapList
import com.blockchain.data.updateDataWith
import com.blockchain.nabu.datamanagers.CustodialWalletManager
import com.blockchain.preferences.CurrencyPrefs
import com.blockchain.preferences.PricesPrefs
import com.blockchain.store.flatMapData
import com.blockchain.walletmode.WalletMode
import com.blockchain.walletmode.WalletModeService
import info.blockchain.balance.AssetInfo
import info.blockchain.balance.Currency
import info.blockchain.balance.Money
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PricesViewModel(
    private val walletModeService: WalletModeService,
    private val currencyPrefs: CurrencyPrefs,
    private val coincore: Coincore,
    private val pricesPrefs: PricesPrefs,
    private val exchangeRatesDataManager: ExchangeRatesDataManager,
    private val custodialWalletManager: CustodialWalletManager,
    private val simpleBuyService: SimpleBuyService
) : MviViewModel<PricesIntents,
    PricesViewState,
    PricesModelState,
    NavigationEvent,
    ModelConfigArgs.NoArgs>(
    PricesModelState()
) {

    override fun viewCreated(args: ModelConfigArgs.NoArgs) {}

    override fun reduce(state: PricesModelState): PricesViewState {
        return PricesViewState(
            availableFilters = state.filters,
            selectedFilter = state.filterBy,
            data = state.data
                .filter {  assetPriceInfo ->
                    state.filterTerm.isEmpty() ||
                        assetPriceInfo.assetInfo.displayTicker.contains(state.filterTerm, ignoreCase = true) ||
                        assetPriceInfo.assetInfo.name.contains(state.filterTerm, ignoreCase = true)
                }
                .filter { assetPriceInfo ->
                    state.tradableCurrencies.map {
                        it.contains(assetPriceInfo.assetInfo.networkTicker)
                    }.dataOrElse(false)
                }.map {
                    it.sortedWith(
                        compareByDescending<AssetPriceInfo> { assetPriceInfo ->
                            state.tradableCurrencies.map {
                                it.contains(assetPriceInfo.assetInfo.networkTicker)
                            }.dataOrElse(false)
                        }.thenByDescending {
                            it.price.map { it.marketCap }.dataOrElse(null)
                        }.thenBy {
                            it.assetInfo.name
                        }
                    )
                }.mapList {
                    it.toPriceItemViewModel()
                }
        )
    }

    private fun AssetPriceInfo.toPriceItemViewModel(): PriceItemViewState {
        return PriceItemViewState(
            name = assetInfo.name,
            ticker = assetInfo.networkTicker,
            logo = assetInfo.logo,
            delta = price.map { ValueChange.fromValue(it.delta24h) },
            currentPrice = price.map {
                it.currentRate.price.format(currencyPrefs.selectedFiatCurrency)
            }
        )
    }

    override suspend fun handleIntent(modelState: PricesModelState, intent: PricesIntents) {
        when (intent) {
            is PricesIntents.LoadAssetsAvailable -> {
                loadAvailableAssets()
            }

            is PricesIntents.FilterSearch -> {
                updateState {
                    it.copy(filterTerm = intent.term)
                }
            }

            is PricesIntents.PricesItemClicked -> {
                handlePriceItemClicked(
                    cryptoCurrency = intent.cryptoCurrency
                )
            }
            is PricesIntents.Filter -> {
                updateState {
                    it.copy(filterBy = intent.filter)
                }
            }
        }
    }

    private fun loadAvailableAssets() {
        viewModelScope.launch {
            simpleBuyService.getSupportedBuySellCryptoCurrencies()
                .onEach { dataResource ->
                    updateState {
                        modelState.copy(tradableCurrencies = dataResource.mapList { it.source.networkTicker })
                    }
                }
                .flatMapLatest {
                    loadAssetsAndPrices()
                }
                .onEach { prices ->
                    updateState {
                        it.copy(data = it.data.updateDataWith(prices))
                    }
                }
                .collect()
        }

        //        viewModelScope.launch {
        //
        //            loadAssetsAndPrices()
        //                .onEach { prices ->
        //                    updateState {
        //                        it.copy(data = it.data.updateDataWith(prices))
        //                    }
        //                }.collect()
        //
        //            //            walletModeService.walletMode.collectLatest {
        //            //                updateState { state ->
        //            //                    if (it != WalletMode.UNIVERSAL) {
        //            //                        state.copy(
        //            //                            filters = listOf(
        //            //                                PricesFilter.All, PricesFilter.Tradable
        //            //                            ),
        //            //                            filterBy = initialSelectedFilter(pricesPrefs.latestPricesMode, it)
        //            //                        )
        //            //                    } else {
        //            //                        state.copy(
        //            //                            filters = emptyList()
        //            //                        )
        //            //                    }
        //            //                }
        //            //            }
        //        }
    }

    private fun initialSelectedFilter(latestPricesMode: String?, walletMode: WalletMode): PricesFilter {
        if (latestPricesMode == null) {
            return when (walletMode) {
                WalletMode.CUSTODIAL_ONLY -> PricesFilter.Tradable
                WalletMode.NON_CUSTODIAL_ONLY -> PricesFilter.All
                else -> throw IllegalArgumentException("Price filtering is not supported for $walletMode")
            }
        }
        return try {
            PricesFilter.valueOf(latestPricesMode)
        } catch (e: IllegalArgumentException) {
            PricesFilter.All
        }
    }

    private fun loadAssetsAndPrices(): Flow<DataResource<List<AssetPriceInfo>>> {
        return coincore.availableCryptoAssetsFlow().flatMapData {
            val assetPriceInfoList = it.map { assetInfo ->
                exchangeRatesDataManager
                    .getPricesWith24hDelta(
                        assetInfo,
                        FreshnessStrategy.Cached(forceRefresh = false)
                    )
                    .map { priceDataResource ->
                        AssetPriceInfo(
                            price = priceDataResource,
                            assetInfo = assetInfo
                        )
                    }
            }

            combine(assetPriceInfoList) {
                it.toList()
            }.map { DataResource.Data(it) }
        }
        //        val supportedBuyCurrencies = custodialWalletManager.getSupportedBuySellCryptoCurrencies()
        //        return supportedBuyCurrencies.doOnSuccess { tradableCurrencies ->
        //            updateState {
        //                modelState.copy(tradableCurrencies = tradableCurrencies.map { it.source.networkTicker })
        //            }
        //        }.flatMap {
        //            coincore.availableCryptoAssets().doOnSuccess {
        //                updateState {
        //                    modelState.copy(
        //                        isLoadingData = true,
        //                        queryBy = "",
        //                        data = emptyList()
        //                    )
        //                }
        //            }.flatMap { assets ->
        //                Single.concat(
        //                    assets.map { fetchAssetPrice(it) }
        //                ).toList()
        //            }
        //        }
    }

    //    private fun fetchAssetPrice(assetInfo: AssetInfo): Single<AssetPriceInfo> {
    //
    //        return exchangeRatesDataManager.getPricesWith24hDeltaLegacy(assetInfo).firstOrError()
    //            .map { prices24HrWithDelta ->
    //                AssetPriceInfo(
    //                    price = prices24HrWithDelta,
    //                    assetInfo = assetInfo
    //                )
    //            }.subscribeOn(Schedulers.io()).onErrorReturn {
    //                AssetPriceInfo(
    //                    assetInfo = assetInfo
    //                )
    //            }
    //    }

    private fun handlePriceItemClicked(cryptoCurrency: AssetInfo) {
        //        navigate(PricesNavigationEvent.CoinView(cryptoCurrency))
    }
}

//private fun PricesModelState.updateAssets(assetPriceInfo: List<AssetPriceInfo>): PricesModelState {
//    require(this.fiatCurrency != null)
//    return copy(
//        data = assetPriceInfo.map { priceInfo ->
//            PricesItem(
//                isTradingAccount = tradableCurrencies.contains(priceInfo.assetInfo.networkTicker),
//                assetInfo = priceInfo.assetInfo,
//                hasError = priceInfo.price == null,
//                currency = this.fiatCurrency,
//                priceWithDelta = priceInfo.price
//            )
//        }
//    )
//}

enum class PricesFilter {
    All, Tradable
}

fun Money?.format(cryptoCurrency: Currency) =
    this?.toStringWithSymbol()
        ?: Money.zero(cryptoCurrency).toStringWithSymbol()