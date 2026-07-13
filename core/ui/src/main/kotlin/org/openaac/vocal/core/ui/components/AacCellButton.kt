package org.openaac.vocal.core.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.openaac.vocal.core.domain.model.BoardThemePreset
import org.openaac.vocal.core.ui.accessibility.AacMinTouchTarget
import org.openaac.vocal.core.ui.theme.VocalTheme
import org.openaac.vocal.core.ui.theme.boardColors

private val BoardCellCornerRadius = 4.dp
private val LargeCellBreakpoint = 144.dp
private val MediumCellBreakpoint = 96.dp
private val CellIconSize = 40.dp

@Composable
fun AacCellButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = label,
    iconResId: Int? = null,
    iconBitmap: ImageBitmap? = null,
) {
    val colors = boardColors()
    Surface(
        onClick = onClick,
        modifier = modifier
            .fillMaxSize()
            .semantics { this.contentDescription = contentDescription },
        shape = RoundedCornerShape(BoardCellCornerRadius),
        color = colors.boardCellBackground,
        contentColor = colors.boardCellContent,
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val labelStyle = aacCellLabelStyle(minOf(maxWidth, maxHeight))
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                when {
                    iconBitmap != null -> {
                        Image(
                            bitmap = iconBitmap,
                            contentDescription = null,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(CellIconSize),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    iconResId != null -> {
                        Icon(
                            painter = painterResource(iconResId),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(CellIconSize),
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                }

                Text(
                    text = label,
                    style = labelStyle,
                    color = colors.boardCellContent,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun aacCellLabelStyle(cellSize: Dp): TextStyle = when {
    cellSize >= LargeCellBreakpoint -> MaterialTheme.typography.headlineMedium
    cellSize >= MediumCellBreakpoint -> MaterialTheme.typography.titleLarge
    else -> MaterialTheme.typography.titleMedium
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
private fun AacCellButtonDefaultBlueLightPreview() {
    AacCellButtonPreviewContent(
        boardThemePreset = BoardThemePreset.DefaultBlue,
        darkTheme = false,
    )
}

@Preview(showBackground = true)
@Composable
private fun AacCellButtonDefaultBlueDarkPreview() {
    AacCellButtonPreviewContent(
        boardThemePreset = BoardThemePreset.DefaultBlue,
        darkTheme = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun AacCellButtonHighContrastLightPreview() {
    AacCellButtonPreviewContent(
        boardThemePreset = BoardThemePreset.HighContrast,
        darkTheme = false,
    )
}

@Preview(showBackground = true)
@Composable
private fun AacCellButtonHighContrastDarkPreview() {
    AacCellButtonPreviewContent(
        boardThemePreset = BoardThemePreset.HighContrast,
        darkTheme = true,
    )
}

@Preview(showBackground = true)
@Composable
private fun AacCellButtonSoftPastelLightPreview() {
    AacCellButtonPreviewContent(
        boardThemePreset = BoardThemePreset.SoftPastel,
        darkTheme = false,
    )
}

@Preview(showBackground = true)
@Composable
private fun AacCellButtonSoftPastelDarkPreview() {
    AacCellButtonPreviewContent(
        boardThemePreset = BoardThemePreset.SoftPastel,
        darkTheme = true,
    )
}

@Composable
private fun AacCellButtonPreviewContent(
    boardThemePreset: BoardThemePreset,
    darkTheme: Boolean,
) {
    VocalTheme(boardThemePreset = boardThemePreset, darkTheme = darkTheme) {
        val colors = boardColors()
        Surface(
            modifier = Modifier.size(160.dp),
            color = colors.boardGridBackground,
        ) {
            Box(modifier = Modifier.padding(4.dp)) {
                AacCellButton(label = "I need help", onClick = {})
            }
        }
    }
}
