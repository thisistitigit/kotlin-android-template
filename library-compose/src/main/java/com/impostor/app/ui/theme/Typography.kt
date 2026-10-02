package com.impostor.app.ui.theme

import androidx.compose.material.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.impostor.library.compose.R

val Poppins = FontFamily(
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_black, FontWeight.Black)
)

object AppTextStyles {
    val title = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Black, fontSize = 35.sp)
    val labelsScroll = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 20.sp)
    val scrollNumber = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 40.sp)
    val chosenNumber = TextStyle(fontFamily = Poppins, fontWeight = FontWeight.Bold, fontSize = 64.sp)
    val nickname = TextStyle(
        brush = NicknameGradient,
        fontFamily = Poppins,
        fontWeight = FontWeight.Black,
        fontSize = 54.sp,
        lineHeight = 81.sp
    )
}

val AppTypography = Typography(
    defaultFontFamily = Poppins,
    h3 = AppTextStyles.title,
    h4 = TextStyle(fontFamily = Poppins, fontSize = 30.sp, fontWeight = FontWeight.Black),
    h5 = TextStyle(fontFamily = Poppins, fontSize = 24.sp, fontWeight = FontWeight.Bold),
    button = TextStyle(fontFamily = Poppins, fontSize = 18.sp, fontWeight = FontWeight.Bold),
    body1 = TextStyle(fontFamily = Poppins, fontSize = 18.sp, fontWeight = FontWeight.Bold)
)
