package piuk.blockchain.android.ui.coinview.presentation.composable

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.blockchain.analytics.Analytics
import com.blockchain.analytics.events.LaunchOrigin
import com.blockchain.charts.ChartEntry
import com.blockchain.charts.ChartView
import com.blockchain.componentlib.alert.AlertType
import com.blockchain.componentlib.alert.CardAlert
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.charts.Balance
import com.blockchain.componentlib.charts.PercentageChangeData
import com.blockchain.componentlib.charts.SparkLineHistoricalRate
import com.blockchain.componentlib.control.TabLayoutLive
import com.blockchain.componentlib.system.LoadingChart
import com.blockchain.componentlib.system.ShimmerLoadingTableRow
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.core.price.HistoricalTimeSpan
import com.blockchain.core.price.impl.toDatePattern
import com.github.mikephil.charting.data.Entry
import kotlin.random.Random
import org.koin.androidx.compose.get
import piuk.blockchain.android.R
import piuk.blockchain.android.ui.coinview.presentation.CoinviewPriceState
import piuk.blockchain.android.ui.dashboard.coinview.CoinViewAnalytics

@Composable
fun AssetPrice(
    data: CoinviewPriceState,
    assetTicker: String,
    onChartEntryHighlighted: (Entry) -> Unit,
    resetPriceInformation: () -> Unit,
    onNewTimeSpanSelected: (HistoricalTimeSpan) -> Unit
) {
    when (data) {
        CoinviewPriceState.Loading -> {
            AssetPriceInfoLoading()
        }

        CoinviewPriceState.Error -> {
            AssetPriceError()
        }

        is CoinviewPriceState.Data -> {
            AssetPriceInfoData(
                data = data,
                assetTicker = assetTicker,
                onChartEntryHighlighted = onChartEntryHighlighted,
                resetPriceInformation = resetPriceInformation,
                onNewTimeSpanSelected = onNewTimeSpanSelected
            )
        }
    }
}

@Composable
fun AssetPriceInfoLoading() {
    Column(modifier = Modifier.fillMaxWidth()) {
        ShimmerLoadingTableRow(showIconLoader = false)

        LoadingChart()
    }
}

@Composable
fun AssetPriceInfoData(
    analytics: Analytics = get(),
    data: CoinviewPriceState.Data,
    assetTicker: String,
    onChartEntryHighlighted: (Entry) -> Unit,
    resetPriceInformation: () -> Unit,
    onNewTimeSpanSelected: (HistoricalTimeSpan) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Balance(
            modifier = Modifier.fillMaxWidth(),
            price = data.price,
            percentageChangeData = PercentageChangeData(
                priceChange = data.priceChange,
                percentChange = data.percentChange,
                interval = stringResource(data.intervalName)
            )
        )

        when (data.chartData) {
            CoinviewPriceState.Data.CoinviewChartState.Loading -> {
                LoadingChart()
            }

            is CoinviewPriceState.Data.CoinviewChartState.Data -> {
                ContentChart(
                    analytics = analytics,
                    assetTicker = assetTicker,
                    fiatSymbol = data.fiatSymbol,
                    chartData = data.chartData.chartData,
                    selectedTimeSpan = data.selectedTimeSpan,
                    onChartEntryHighlighted = onChartEntryHighlighted,
                    resetPriceInformation = resetPriceInformation
                )
            }
        }

        TabLayoutLive(
            items = HistoricalTimeSpan.values().map { stringResource(it.toSimpleName()) },
            onItemSelected = { index ->
                HistoricalTimeSpan.fromValue(index).let { selectedTimeSpan ->
                    analytics.logEvent(
                        CoinViewAnalytics.ChartTimeIntervalSelected(
                            origin = LaunchOrigin.COIN_VIEW,
                            currency = assetTicker,
                            timeInterval = selectedTimeSpan.toTimeInterval()
                        )
                    )

                    onNewTimeSpanSelected(selectedTimeSpan)
                }
            },
            selectedItemIndex = data.selectedTimeSpan.value,
            showLiveIndicator = false
        )
    }
}

@StringRes private fun HistoricalTimeSpan.toSimpleName(): Int {
    return when (this) {
        HistoricalTimeSpan.DAY -> R.string.coinview_chart_tab_day
        HistoricalTimeSpan.WEEK -> R.string.coinview_chart_tab_week
        HistoricalTimeSpan.MONTH -> R.string.coinview_chart_tab_month
        HistoricalTimeSpan.YEAR -> R.string.coinview_chart_tab_year
        HistoricalTimeSpan.ALL_TIME -> R.string.coinview_chart_tab_all
    }
}

@Composable
fun AssetPriceError() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .padding(AppTheme.dimensions.standardSpacing),
        contentAlignment = Alignment.Center
    ) {
        CardAlert(
            title = stringResource(R.string.coinview_chart_load_error_title),
            subtitle = stringResource(R.string.coinview_chart_load_error_subtitle),
            alertType = AlertType.Warning,
            isBordered = true,
            isDismissable = false
        )
    }
}

@Composable
fun LoadingChart() {
    LoadingChart(
        historicalRates = List(20) {
            object : SparkLineHistoricalRate {
                override val timestamp: Long = it.toLong()
                override val rate: Double = Random.nextDouble(50.0, 150.0)
            }
        },
        loadingText = stringResource(R.string.coinview_chart_loading)
    )
}

@Composable
fun ContentChart(
    analytics: Analytics = get(),
    assetTicker: String,
    fiatSymbol: String,
    chartData: List<ChartEntry>,
    selectedTimeSpan: HistoricalTimeSpan,
    onChartEntryHighlighted: (Entry) -> Unit,
    resetPriceInformation: () -> Unit,
) {
    var isInteractingWithChart by remember { mutableStateOf(false) }

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp),
        factory = { context ->
            ChartView(context).apply {
                isChartLive = false
                onEntryHighlighted = { entry ->
                    onChartEntryHighlighted(entry)
                }
                onActionPressDown = {
                    isInteractingWithChart = true
                    analytics.logEvent(
                        CoinViewAnalytics.ChartEngaged(
                            origin = LaunchOrigin.COIN_VIEW,
                            currency = assetTicker,
                            timeInterval = selectedTimeSpan.toTimeInterval()
                        )
                    )
                }
                onScrubRelease = {
                    isInteractingWithChart = false
                    analytics.logEvent(
                        CoinViewAnalytics.ChartDisengaged(
                            origin = LaunchOrigin.COIN_VIEW,
                            currency = assetTicker,
                            timeInterval = selectedTimeSpan.toTimeInterval()
                        )
                    )
                    resetPriceInformation()
                }

                datePattern = selectedTimeSpan.toDatePattern()
                this.fiatSymbol = fiatSymbol
                setData(chartData)
            }
        },
        update = {
            if (isInteractingWithChart.not()) it.setData(chartData)
        }
    )
}

private fun HistoricalTimeSpan.toTimeInterval(): CoinViewAnalytics.Companion.TimeInterval =
    when (this) {
        HistoricalTimeSpan.DAY -> CoinViewAnalytics.Companion.TimeInterval.DAY
        HistoricalTimeSpan.WEEK -> CoinViewAnalytics.Companion.TimeInterval.WEEK
        HistoricalTimeSpan.MONTH -> CoinViewAnalytics.Companion.TimeInterval.MONTH
        HistoricalTimeSpan.YEAR -> CoinViewAnalytics.Companion.TimeInterval.YEAR
        HistoricalTimeSpan.ALL_TIME -> CoinViewAnalytics.Companion.TimeInterval.ALL_TIME
        else -> CoinViewAnalytics.Companion.TimeInterval.LIVE
    }

@Preview
@Composable
fun PreviewAssetPrice_Loading() {
    AssetPrice(CoinviewPriceState.Loading, assetTicker = "ETH", {}, {}, {})
}

@Preview
@Composable
fun PreviewAssetPrice_Data() {
    AssetPrice(
        CoinviewPriceState.Data(
            assetName = "Ethereum",
            assetLogo = "logo//",
            fiatSymbol = "€",
            price = "$4,570.27",
            priceChange = "$969.25",
            percentChange = 5.58,
            intervalName = R.string.coinview_price_day,
            chartData = CoinviewPriceState.Data.CoinviewChartState.Loading,
            selectedTimeSpan = HistoricalTimeSpan.DAY
        ),
        assetTicker = "ETH",
        {},
        {},
        {}
    )
}

@Preview
@Composable
fun PreviewAssetPriceError() {
    AssetPriceError()
}
