package piuk.blockchain.android.ui.coinview.presentation

import androidx.annotation.StringRes
import com.blockchain.charts.ChartEntry
import com.blockchain.commonarch.presentation.mvi_v2.ViewState
import com.blockchain.core.price.HistoricalTimeSpan

data class CoinviewViewState(
    val assetName: String,
    val assetPrice: CoinviewPriceState,
    val totalBalance: CoinviewTotalBalanceState,
    val accounts: CoinviewAccountsState
) : ViewState

// Price
sealed interface CoinviewPriceState {
    object Loading : CoinviewPriceState
    object Error : CoinviewPriceState
    data class Data(
        val assetName: String,
        val assetLogo: String,
        val fiatSymbol: String,
        val price: String,
        val priceChange: String,
        val percentChange: Double,
        @StringRes val intervalName: Int,
        val chartData: CoinviewChart,
        val selectedTimeSpan: HistoricalTimeSpan
    ) : CoinviewPriceState {
        sealed interface CoinviewChart {
            object Loading : CoinviewChart
            data class Data(val chartData: List<ChartEntry>) : CoinviewChart
        }
    }
}

// Total balance
sealed interface CoinviewTotalBalanceState {
    object NotSupported : CoinviewTotalBalanceState
    object Loading : CoinviewTotalBalanceState
    data class Data(
        val assetName: String,
        val totalFiatBalance: String,
        val totalCryptoBalance: String
    ) : CoinviewTotalBalanceState
}

// Accounts
sealed interface CoinviewAccountsState {
    object Loading : CoinviewAccountsState
    data class Data(
        val style: CoinviewAccountsStyle,
        val header: CoinviewAccountsHeaderState,
        val accounts: List<CoinviewAccountState>
    ) : CoinviewAccountsState {
        sealed interface CoinviewAccountState {
            data class Available(
                val title: String,
                val subtitle: SimpleValue,
                val cryptoBalance: String,
                val fiatBalance: String,
                val logo: String
            ) : CoinviewAccountState

            data class Unavailable(
                val title: String,
                val subtitle: SimpleValue
            ) : CoinviewAccountState
        }

        sealed interface CoinviewAccountsHeaderState {
            data class ShowHeader(val text: SimpleValue) : CoinviewAccountsHeaderState
            object NoHeader : CoinviewAccountsHeaderState
        }
    }
}

enum class CoinviewAccountsStyle {
    Simple, Boxed
}

sealed interface SimpleValue {
    data class StringValue(val value: String) : SimpleValue
    data class IntResValue(
        @StringRes val value: Int,
        val args: List<String> = emptyList()
    ) : SimpleValue
}