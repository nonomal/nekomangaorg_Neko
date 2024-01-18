package org.nekomanga.presentation.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import eu.kanade.tachiyomi.R
import jp.wasabeef.gap.Gap
import org.nekomanga.presentation.screens.ThemeColorState
import org.nekomanga.presentation.theme.Size

/**
 * Simple Dialog to add a new category
 */
@Composable
fun DeleteAllHistoryDialog(themeColorState: ThemeColorState, onDismiss: () -> Unit, onConfirm: () -> Unit) {

    CompositionLocalProvider(LocalRippleTheme provides themeColorState.rippleTheme, LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
        AlertDialog(
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.clear_history_confirmation_1),
                        style = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                    )
                    Gap(Size.medium)
                    Text(
                        text = stringResource(R.string.clear_history_confirmation_2),
                        style = MaterialTheme.typography.bodyLarge.copy(MaterialTheme.colorScheme.onSurface),
                    )
                }
            },
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        onConfirm()
                        onDismiss()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = themeColorState.buttonColor),
                ) {
                    Text(text = stringResource(id = R.string.clear))
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss, colors = ButtonDefaults.textButtonColors(contentColor = themeColorState.buttonColor)) {
                    Text(text = stringResource(id = R.string.cancel))
                }
            },
        )
    }
}