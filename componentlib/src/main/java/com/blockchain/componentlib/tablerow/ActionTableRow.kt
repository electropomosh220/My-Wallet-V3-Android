package com.blockchain.componentlib.tablerow

import android.graphics.drawable.ColorDrawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import com.blockchain.componentlib.R
import com.blockchain.componentlib.tag.TagViewState
import com.blockchain.componentlib.tag.TagsRow
import com.blockchain.componentlib.theme.AppSurface
import com.blockchain.componentlib.theme.AppTheme

@Composable
fun ActionTableRow(
    startIconUrl: String,
    primaryText: String,
    onClick: () -> Unit,
    secondaryText: String? = null,
    paragraphText: String? = null,
    tags: List<TagViewState>? = null,
) {
    TableRow(
        contentStart = {
            Image(
                painter = rememberImagePainter(
                    data = startIconUrl,
                    builder = {
                        crossfade(true)
                        placeholder(ColorDrawable(AppTheme.colors.light.toArgb()))
                    }
                ),
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(end = 16.dp)
                    .size(24.dp)
            )
        },
        content = {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 8.dp)
            ) {
                Text(
                    text = primaryText,
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.title,
                )
                if (secondaryText != null) {
                    Text(
                        text = secondaryText,
                        style = AppTheme.typography.paragraph1,
                        color = AppTheme.colors.body,
                    )
                }
            }
        },
        contentEnd = {
            Image(
                painter = painterResource(id = R.drawable.ic_chevron_end),
                contentDescription = null
            )
        },
        onContentClicked = onClick,
        contentBottom = {
            Column {
                if (paragraphText != null) {
                    Text(
                        text = paragraphText,
                        style = AppTheme.typography.caption1,
                        color = AppTheme.colors.body,
                        modifier = Modifier
                            .padding(
                                top = 4.dp,
                                bottom = if (tags.isNullOrEmpty()) 0.dp else 8.dp,
                            )
                    )
                }
                if (!tags.isNullOrEmpty()) {
                    TagsRow(
                        tags = tags,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    )
}

@Preview
@Composable
fun ActionTableRow_Basic() {
    AppTheme {
        AppSurface {
            ActionTableRow(
                startIconUrl = "",
                primaryText = "Back Up Your Wallet",
                secondaryText = "Step 1",
                onClick = { }
            )
        }
    }
}