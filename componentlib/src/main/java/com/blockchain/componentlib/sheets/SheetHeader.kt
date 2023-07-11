package com.blockchain.componentlib.sheets

import android.content.res.Configuration
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.blockchain.componentlib.R
import com.blockchain.componentlib.basic.AppDivider
import com.blockchain.componentlib.basic.CloseIcon
import com.blockchain.componentlib.basic.ImageResource
import com.blockchain.componentlib.icon.CustomStackedIcon
import com.blockchain.componentlib.tablerow.custom.StackedIcon
import com.blockchain.componentlib.theme.AppColors
import com.blockchain.componentlib.theme.AppTheme
import com.blockchain.componentlib.theme.Dark200
import com.blockchain.componentlib.theme.Grey700
import com.blockchain.componentlib.theme.topOnly

@Composable
fun SheetHeader(
    modifier: Modifier = Modifier,
    title: String? = null,
    byline: String? = null,
    startImage: StackedIcon? = null,
    onClosePress: () -> Unit,
    shouldShowDivider: Boolean = true,
    backgroundSecondary: Boolean = true
) {

    Surface(
        color = if (backgroundSecondary) AppColors.backgroundSecondary else AppColors.background,
        shape = AppTheme.shapes.large.topOnly()
    ) {
        Box {
            SheetNub(
                Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 8.dp)
            )
            Column {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Spacer(Modifier.width(AppTheme.dimensions.smallSpacing))

                    startImage?.let {
                        Box(modifier = Modifier.padding(top = 27.dp)) {
                            CustomStackedIcon(
                                icon = it,
                                iconBackground = AppColors.backgroundSecondary,
                                borderColor = AppColors.backgroundSecondary,
                            )
                        }
                        Spacer(Modifier.width(AppTheme.dimensions.tinySpacing))
                    }

                    SheetHeaderTitle(
                        title = title,
                        byline = byline,
                        modifier = Modifier
                            .weight(1f)
                            .padding(
                                top = dimensionResource(R.dimen.standard_spacing),
                                bottom = if (byline.isNullOrBlank()) 10.dp else 5.dp
                            )
                    )

                    CloseIcon(
                        modifier = Modifier.padding(
                            top = AppTheme.dimensions.mediumSpacing,
                            end = AppTheme.dimensions.smallSpacing
                        ),
                        isScreenBackgroundSecondary = backgroundSecondary,
                        onClick = onClosePress
                    )
                }
                if (shouldShowDivider) {
                    AppDivider(
                        if(backgroundSecondary) {
                            AppColors.background
                        } else {
                            AppColors.medium
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SheetHeaderTitle(
    modifier: Modifier = Modifier,
    title: String?,
    byline: String? = null,
    isDarkMode: Boolean = isSystemInDarkTheme()
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.Start
    ) {
        title?.let {
            Text(
                text = title,
                style = AppTheme.typography.title3,
                color = AppTheme.colors.title,
                textAlign = TextAlign.Center
            )
        }

        if (!byline.isNullOrBlank()) {
            Text(
                text = byline,
                style = AppTheme.typography.paragraph1,
                color = if (isDarkMode) Dark200 else Grey700,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Preview
@Composable
private fun SheetHeaderPreview() {
    SheetHeader(
        title = "Title",
        onClosePress = { /* no-op */ }
    )
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun PreviewSheetHeaderPreviewDark() {
    SheetHeaderPreview()
}

@Preview
@Composable
private fun SheetHeaderNoTitle() {
    SheetHeader(
        onClosePress = { /* no-op */ }
    )
}

@Preview
@Composable
private fun SheetHeaderBylinePreview() {
    SheetHeader(
        title = "Title",
        byline = "Byline",
        onClosePress = { /* no-op */ }
    )
}

@Preview
@Composable
private fun SheetHeaderWithStartIconPreview() {
    SheetHeader(
        title = "Title",
        onClosePress = { /* no-op */ },
        startImage = StackedIcon.SingleIcon(
            ImageResource.Local(
                id = R.drawable.ic_qr_code,
                contentDescription = null
            )
        )
    )
}

@Preview
@Composable
private fun SheetHeaderBylineWithStartIconPreview() {
    SheetHeader(
        title = "Title",
        byline = "Byline",
        onClosePress = { /* no-op */ },
        startImage = StackedIcon.SingleIcon(
            ImageResource.Local(
                id = R.drawable.ic_qr_code,
                contentDescription = null
            )
        )
    )
}
