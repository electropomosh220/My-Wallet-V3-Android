package com.blockchain.home.introduction.composable

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import com.blockchain.componentlib.theme.Blue600
import com.blockchain.componentlib.theme.Purple0000
import com.blockchain.home.presentation.R
import com.blockchain.walletmode.WalletMode

data class IntroductionScreenContent(
    @DrawableRes val image: Int,
    val isLogo: Boolean,
    @StringRes val title: Int,
    @StringRes val description: Int,
    val tag: Pair<Int, Color>? = null,
    val forWalletMode: WalletMode? = null
)

fun introductionsScreens(isNewUser: Boolean): List<IntroductionScreenContent> {
    return listOfNotNull(
        IntroductionScreenContent(
            image = R.drawable.ic_blockchain,
            isLogo = true,
            title = R.string.educational_wallet_mode_intro_title,
            description = R.string.educational_wallet_mode_intro_description,
        ).takeIf { isNewUser },
        IntroductionScreenContent(
            image = R.drawable.ic_educational_wallet_menu,
            isLogo = false,
            title = R.string.educational_wallet_mode_menu_title,
            description = R.string.educational_wallet_mode_menu_description,
        ).takeIf { isNewUser.not() },
        IntroductionScreenContent(
            image = R.drawable.ic_educational_wallet_menu,
            isLogo = false,
            title = R.string.educational_wallet_mode_trading_title,
            description = R.string.educational_wallet_mode_trading_description,
            tag = Pair(R.string.educational_wallet_mode_trading_secure_tag, Blue600),
            forWalletMode = WalletMode.CUSTODIAL_ONLY
        ),
        IntroductionScreenContent(
            image = R.drawable.ic_educational_wallet_menu,
            isLogo = false,
            title = R.string.educational_wallet_mode_defi_title,
            description = R.string.educational_wallet_mode_defi_description,
            tag = Pair(R.string.educational_wallet_mode_defi_secure_tag, Purple0000),
            forWalletMode = WalletMode.NON_CUSTODIAL_ONLY
        )
    )
}