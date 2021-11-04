package com.blockchain.componentlib.tablerow

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.blockchain.componentlib.theme.AppTheme

@Composable
fun ToggleTableRow(
    onCheckedChange: (isChecked: Boolean) -> Unit,
    title: String,
    body: String? = null,
    isChecked: Boolean = false,
) {
    TableRow(
        content = {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = AppTheme.typography.body2,
                    color = AppTheme.colors.title
                )
                if (body != null) {
                    Text(
                        text = body,
                        style = AppTheme.typography.paragraph1,
                        color = AppTheme.colors.body
                    )
                }
            }
        },
        contentEnd = {
            Switch(
                checked = isChecked,
                onCheckedChange = { onCheckedChange(it) },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = AppTheme.colors.primary,
                    uncheckedThumbColor = AppTheme.colors.dark,
                    uncheckedTrackColor = AppTheme.colors.dark,
                )
            )
        }
    )
}

@Preview
@Composable
fun ToggleTableRow_SingleLine_NotChecked() {
    AppTheme {
        Surface {
            ToggleTableRow(
                onCheckedChange = {},
                title = "Enable this ?"
            )
        }
    }
}

@Preview
@Composable
fun ToggleTableRow_NotChecked() {
    AppTheme {
        Surface {
            ToggleTableRow(
                onCheckedChange = {},
                title = "Enable this ?",
                body = "Some additional info"
            )
        }
    }
}

@Preview
@Composable
fun ToggleTableRow_Checked() {
    AppTheme {
        Surface {
            ToggleTableRow(
                onCheckedChange = {},
                title = "Enable this ?",
                body = "Some additional info",
                isChecked = true
            )
        }
    }
}

@Preview
@Composable
fun ToggleTableRow_SingleLine_NotChecked_Dark() {
    AppTheme(darkTheme = true) {
        Surface {
            ToggleTableRow(
                onCheckedChange = {},
                title = "Enable this ?"
            )
        }
    }
}

@Preview
@Composable
fun ToggleTableRow_NotChecked_Dark() {
    AppTheme(darkTheme = true) {
        Surface {
            ToggleTableRow(
                onCheckedChange = {},
                title = "Enable this ?",
                body = "Some additional info"
            )
        }
    }
}

@Preview
@Composable
fun ToggleTableRow_Checked_Dark() {
    AppTheme(darkTheme = true) {
        Surface {
            ToggleTableRow(
                onCheckedChange = {},
                title = "Enable this ?",
                body = "Some additional info",
                isChecked = true
            )
        }
    }
}