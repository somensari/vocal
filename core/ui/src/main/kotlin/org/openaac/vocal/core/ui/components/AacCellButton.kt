package org.openaac.vocal.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openaac.vocal.core.ui.accessibility.AacMinTouchTarget
import org.openaac.vocal.core.ui.theme.ProvideBoardColors
import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.core.ui.theme.boardColors

private val BoardCellCornerRadius = 4.dp

@Composable
fun AacCellButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = label,
    iconResId: Int? = null,
) {
    val colors = boardColors()
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxSize()
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(BoardCellCornerRadius),
        color = colors.cellBackground,
        contentColor = colors.cellContent,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
        ) {
            if (iconResId != null) {
                Icon(
                    painter = painterResource(iconResId),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(40.dp),
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium,
                color = colors.cellContent,
                textAlign = TextAlign.Center,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun AacSecondaryButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = label,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = AacMinTouchTarget, minHeight = AacMinTouchTarget)
            .semantics { this.contentDescription = contentDescription },
    ) {
        Text(text = label)
    }
}

@Preview(showBackground = true)
@Composable
private fun AacCellButtonPreview() {
    VocalTheme {
        ProvideBoardColors(
            boardThemePreset = org.openaac.vocal.core.domain.model.BoardThemePreset.Default,
            darkTheme = false,
        ) {
            Surface(modifier = Modifier.size(160.dp)) {
                AacCellButton(label = "I need help", onClick = {})
            }
        }
    }
}
