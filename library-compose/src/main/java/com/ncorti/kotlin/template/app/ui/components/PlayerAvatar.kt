package com.ncorti.kotlin.template.app.ui.components

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.ncorti.kotlin.template.app.ui.theme.LightPurple

@Composable
fun PlayerAvatar(
    @DrawableRes avatarRes: Int,
    modifier: Modifier = Modifier,
    photo: Bitmap? = null,
    decoration: PlayerAvatarDecoration = PlayerAvatarDecoration(),
    overlay: (@Composable BoxScope.() -> Unit)? = null
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(LightPurple)
                .border(decoration.borderWidth, decoration.borderColor, CircleShape)
        ) {
            if (photo == null) {
                Image(
                    painter = painterResource(avatarRes),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    bitmap = photo.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
        overlay?.invoke(this)
    }
}
