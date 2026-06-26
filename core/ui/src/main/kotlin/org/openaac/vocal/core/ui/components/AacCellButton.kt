package org.openaac.vocal.core.ui.components

import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.openaac.vocal.core.ui.accessibility.AacMinTouchTarget
import org.openaac.vocal.core.ui.theme.VocalTheme

@Composable
fun AacCellButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = label,
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .defaultMinSize(minWidth = AacMinTouchTarget, minHeight = AacMinTouchTarget)
            .semantics { this.contentDescription = contentDescription },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        ),
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
        )
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
        Surface {
            AacCellButton(label = "I need help", onClick = {})
        }
    }
}
