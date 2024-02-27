package it.hamy.muza.enums

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import it.hamy.muza.roundedShape

enum class ThumbnailRoundness(val dp: Dp) {
    None(0.dp),
    Light(2.dp),
    Medium(8.dp),
    Heavy(12.dp),
    Heavier(16.dp),
    Heaviest(16.dp);

    val shape get() = dp.roundedShape
}
