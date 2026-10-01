package com.ncorti.kotlin.template.app.ui.screens

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ncorti.kotlin.template.app.ui.components.AppButton
import com.ncorti.kotlin.template.app.ui.components.AppButtonStyle
import com.ncorti.kotlin.template.app.ui.components.BackButton
import com.ncorti.kotlin.template.app.ui.components.PlayerAvatar
import com.ncorti.kotlin.template.app.ui.components.TitleBanner
import com.ncorti.kotlin.template.app.ui.theme.AppTextStyles
import com.ncorti.kotlin.template.app.ui.theme.AppWhite
import com.ncorti.kotlin.template.app.ui.theme.MainPurple
import com.ncorti.kotlin.template.app.ui.theme.MarkerYellow
import com.ncorti.kotlin.template.app.ui.theme.Spacing
import com.ncorti.kotlin.template.app.ui.theme.TemplateTheme
import com.ncorti.kotlin.template.library.compose.R

data class PlayerNameUiState(
    val playerIndex: Int,
    val nickname: String,
    @DrawableRes val avatarRes: Int,
    val photo: Bitmap? = null
)

data class PlayerNameCallbacks(
    val onNicknameChange: (String) -> Unit,
    val onBack: () -> Unit,
    val onContinue: () -> Unit
)

/** Android integration boundary. The stateless screen remains safe for Preview and screenshot tests. */
@Composable
fun PlayerNameRoute(
    state: PlayerNameUiState,
    callbacks: PlayerNameCallbacks,
    onPhotoTaken: (Bitmap) -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview(),
        onResult = { result -> result?.let(onPhotoTaken) }
    )
    PlayerNameScreen(
        state = state,
        callbacks = callbacks,
        onTakePhoto = { cameraLauncher.launch(null) },
        modifier = modifier
    )
}

@Composable
fun PlayerNameScreen(
    state: PlayerNameUiState,
    callbacks: PlayerNameCallbacks,
    onTakePhoto: () -> Unit,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    Column(
        modifier = modifier.fillMaxSize().imePadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(
            contentDescription = stringResource(R.string.back),
            onClick = callbacks.onBack,
            modifier = Modifier.align(Alignment.Start).padding(start = Spacing.small, top = Spacing.small)
        )
        Spacer(Modifier.height(42.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            Text(text = stringResource(R.string.player_label), style = AppTextStyles.title, color = AppWhite)
            Text(text = " ${state.playerIndex + 1}", style = AppTextStyles.title, color = MainPurple)
        }
        Spacer(Modifier.height(54.dp))
        Box(
            modifier = Modifier.fillMaxWidth().height(194.dp),
            contentAlignment = Alignment.Center
        ) {
            TitleBanner(text = "", modifier = Modifier.fillMaxWidth())
            PlayerAvatar(
                avatarRes = state.avatarRes,
                photo = state.photo,
                modifier = Modifier.size(126.dp)
            ) {
                CameraButton(onClick = onTakePhoto, modifier = Modifier.align(Alignment.BottomEnd))
            }
        }
        Spacer(Modifier.height(20.dp))
        NicknameField(
            value = state.nickname,
            onValueChange = callbacks.onNicknameChange,
            onDone = {
                focusManager.moveFocus(FocusDirection.Down)
                if (state.nickname.isNotBlank()) callbacks.onContinue()
            },
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spacing.large)
        )
        Spacer(Modifier.weight(1f))
        AppButton(
            text = stringResource(R.string.next),
            onClick = callbacks.onContinue,
            enabled = state.nickname.isNotBlank(),
            style = AppButtonStyle.SECONDARY,
            modifier = Modifier.width(172.dp)
        )
        Spacer(Modifier.height(Spacing.extraLarge))
    }
}

@Composable
private fun NicknameField(
    value: String,
    onValueChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    BasicTextField(
        value = value,
        onValueChange = { onValueChange(it.take(MAX_NICKNAME_LENGTH)) },
        modifier = modifier.height(76.dp),
        singleLine = true,
        textStyle = AppTextStyles.chosenNumber.copy(fontSize = 52.sp, color = AppWhite, textAlign = TextAlign.Center),
        cursorBrush = SolidColor(MainPurple),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = { onDone() }),
        decorationBox = { content ->
            Box(contentAlignment = Alignment.Center) {
                if (value.isEmpty()) {
                    Text(
                        text = stringResource(R.string.nickname),
                        style = AppTextStyles.chosenNumber.copy(fontSize = 52.sp),
                        color = AppWhite.copy(alpha = 0.72f),
                        textAlign = TextAlign.Center
                    )
                }
                content()
            }
        }
    )
}

@Composable
private fun CameraButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    val description = stringResource(R.string.take_photo)
    Box(
        modifier = modifier
            .size(42.dp)
            .clip(CircleShape)
            .background(MarkerYellow)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics {
                contentDescription = description
                role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_camera),
            contentDescription = null,
            modifier = Modifier.size(22.dp)
        )
    }
}

private const val MAX_NICKNAME_LENGTH = 18

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
internal fun PlayerNamePreview() = TemplateTheme {
    PlayerNameScreen(PlayerNameUiState(0, "", R.drawable.vibrent_1), PlayerNameCallbacks({}, {}, {}), {})
}
