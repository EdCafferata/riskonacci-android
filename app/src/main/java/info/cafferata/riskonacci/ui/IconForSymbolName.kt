package info.cafferata.riskonacci.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.QuestionMark
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.outlined.Checkroom
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * Maps a `PokerCard`/`Deck` symbol key (a plain string, same role as an
 * SF Symbol name on iOS) to a Material icon. Kept as a lookup at the UI
 * layer rather than in the model, mirroring how the iOS app resolves
 * `symbolName` to an SF Symbol only at the `Image(systemName:)` call site.
 */
fun iconForSymbolName(name: String?): ImageVector? = when {
    name == null -> null
    name == "tag" -> Icons.Filled.Tag
    name == "textformat_123" -> Icons.Filled.Dialpad
    name == "tshirt" -> Icons.Outlined.Checkroom
    name == "warning" -> Icons.Filled.Warning
    name == "check_circle" -> Icons.Filled.CheckCircle
    name == "flame" -> Icons.Filled.LocalFireDepartment
    name == "question_mark" -> Icons.Filled.QuestionMark
    name == "coffee" -> Icons.Filled.Coffee
    name == "group" -> Icons.Filled.Group
    name.startsWith("circle_") || name.startsWith("likelihood-") || name.startsWith("impact-") -> Icons.Filled.Circle
    else -> null
}
