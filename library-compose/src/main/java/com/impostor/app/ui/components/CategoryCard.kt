package com.impostor.app.ui.components

import androidx.annotation.RawRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.MarkerYellow

@Composable
fun CategoryCard(@RawRes image: Int, label: String, selected: Boolean,
    onClick: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    val shape = RoundedCornerShape(18.dp)
    Image(rememberSvgPainter(image), null, contentScale = ContentScale.FillBounds,
        modifier = modifier.shadow(if (selected) 10.dp else 0.dp, shape,
            ambientColor = MarkerYellow, spotColor = MarkerYellow)
            .clip(shape).border(if (selected) 4.dp else 2.dp, if (selected) MarkerYellow else MainPurple, shape)
            .selectable(selected = selected, enabled = enabled, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = label })
}
