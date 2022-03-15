package com.blockchain.deeplinking.navigation

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

sealed class Destination : Parcelable {
    @Parcelize
    data class AssetViewDestination(val networkTicker: String) : Destination()

    @Parcelize
    data class AssetBuyDestination(val code: String, val amount: String) : Destination()

    @Parcelize
    data class ActivityDestination(val filter: String? = null) : Destination()
}
