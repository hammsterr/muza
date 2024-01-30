package it.hamy.muza.enums

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

enum class ThumbnailRoundness {
    Отключено,
    Слабое,
    Среднее,
    Сильное,
    Максимальное;

    fun shape(): Shape {
        return when (this) {
            Отключено -> RectangleShape
            Слабое -> RoundedCornerShape(2.dp)
            Среднее -> RoundedCornerShape(4.dp)
            Сильное -> RoundedCornerShape(8.dp)
            Максимальное -> RoundedCornerShape(14.dp)
        }
    }
}
