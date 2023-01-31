package com.blockchain.componentlib.chrome

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.blockchain.componentlib.R
import com.blockchain.componentlib.basic.Image
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.icons.Icons
import com.blockchain.componentlib.icons.Viewfinder
import com.blockchain.componentlib.theme.AppTheme

const val BALANCE_OFFSET_TARGET = 120
const val BALANCE_OFFSET_ANIM_DURATION = 200

@Composable
fun MenuOptionsScreen(
    modifier: Modifier = Modifier,
    walletBalance: String = "",
    openSettings: () -> Unit,
    launchQrScanner: () -> Unit,
    showBackground: Boolean = false,
    showBalance: Boolean = false
) {
    val balanceOffset by animateIntAsState(
        targetValue = if (showBalance) 0 else BALANCE_OFFSET_TARGET,
        animationSpec = tween(
            durationMillis = BALANCE_OFFSET_ANIM_DURATION
        )
    )

    Box(modifier = Modifier.fillMaxWidth()) {
        // to hide what is scrolled passed this view
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(AppTheme.dimensions.xHugeSpacing)
                .background(
                    AppTheme.colors.backgroundMuted.copy(alpha = 0.9F),
                )
        )

        Box(
            modifier = modifier
                .padding(vertical = AppTheme.dimensions.tinySpacing)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                modifier = Modifier.matchParentSize(),
                visible = showBackground,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                Surface(
                    modifier = Modifier
                        .padding(vertical = AppTheme.dimensions.smallestSpacing, horizontal = 1.dp)
                        .matchParentSize()
                        .background(AppTheme.colors.background, AppTheme.shapes.large)
                        .clickable { },
                    shape = AppTheme.shapes.large,
                    elevation = 3.dp
                ) {
                    Box(modifier = Modifier.matchParentSize()) {
                        Text(
                            modifier = Modifier
                                .clipToBounds()
                                .align(Alignment.Center)
                                .offset {
                                    IntOffset(
                                        x = 0,
                                        y = balanceOffset
                                    )
                                }.graphicsLayer {
                                    alpha = BALANCE_OFFSET_TARGET - balanceOffset / BALANCE_OFFSET_TARGET.toFloat()
                                },
                            text = walletBalance,
                            style = AppTheme.typography.title3,
                            color = AppTheme.colors.title
                        )
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.dimensions.tinySpacing),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    imageResource = ImageResource.Local(R.drawable.ic_user_settings),
                    modifier = Modifier
                        .padding(AppTheme.dimensions.tinySpacing)
                        .clickable {
                            openSettings()
                        }
                )

                Spacer(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                Spacer(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth()
                )

                Image(
                    imageResource = Icons.Viewfinder,
                    modifier = Modifier
                        .padding(AppTheme.dimensions.tinySpacing)
                        .clickable {
                            launchQrScanner()
                        }
                )
            }
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0XFF1234F2)
@Composable
fun PreviewMenuOptionsScreen() {
    MenuOptionsScreen(
        walletBalance = "123.456",
        openSettings = {}, launchQrScanner = {},
        showBackground = true,
        showBalance = true
    )
}
