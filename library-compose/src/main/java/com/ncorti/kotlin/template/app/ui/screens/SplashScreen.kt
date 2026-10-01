package com.ncorti.kotlin.template.app.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import com.ncorti.kotlin.template.app.ui.theme.Spacing
import com.ncorti.kotlin.template.app.ui.theme.TemplateTheme
import com.ncorti.kotlin.template.library.compose.R

@Composable
fun SplashScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.fillMaxSize().background(MaterialTheme.colors.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(R.drawable.logo),
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large)
        )
    }
}

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
internal fun SplashPreview() = TemplateTheme { SplashScreen() }
