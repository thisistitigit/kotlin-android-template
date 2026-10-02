package com.impostor.app.ui

import android.graphics.Bitmap
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.impostor.app.ui.screens.HomeScreen
import com.impostor.app.ui.screens.ImpostorCountScreen
import com.impostor.app.ui.screens.OnboardingScreen
import com.impostor.app.ui.screens.PLAYER_AVATARS
import com.impostor.app.ui.screens.PlayerNameRoute
import com.impostor.app.ui.screens.PlayerNameCallbacks
import com.impostor.app.ui.screens.PlayerNameUiState
import com.impostor.app.ui.screens.PlayerCountScreen
import com.impostor.app.ui.screens.SplashScreen
import com.impostor.app.ui.screens.TurnPlayerUi
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.RoleType
import kotlinx.coroutines.delay

private enum class AppDestination {
    SPLASH,
    ONBOARDING,
    HOME,
    PLAYER_COUNT,
    PLAYER_NAME,
    IMPOSTOR_COUNT,
    PLAYER_TURN
}

@Composable
@Suppress("LongMethod", "CyclomaticComplexMethod")
fun ImpostorApp() {
    var destination by rememberSaveable { mutableStateOf(AppDestination.SPLASH) }
    var onboardingPage by rememberSaveable { mutableIntStateOf(0) }
    var selectedMode by rememberSaveable { mutableStateOf<GameModeType?>(null) }
    var playerCount by rememberSaveable { mutableIntStateOf(DEFAULT_PLAYER_COUNT) }
    var impostorCount by rememberSaveable { mutableIntStateOf(DEFAULT_IMPOSTOR_COUNT) }
    var mrWhiteCount by rememberSaveable { mutableIntStateOf(0) }
    var selectedRole by rememberSaveable { mutableStateOf(RoleType.IMPOSTOR) }
    var currentPlayerIndex by rememberSaveable { mutableIntStateOf(0) }
    var playerNicknames by rememberSaveable { mutableStateOf(emptyList<String>()) }
    var playerAvatarIndices by rememberSaveable { mutableStateOf(emptyList<Int>()) }
    var gameSessionKey by rememberSaveable { mutableStateOf(java.util.UUID.randomUUID().toString()) }
    val playerPhotos = remember { mutableStateMapOf<Int, Bitmap>() }

    LaunchedEffect(destination) {
        if (destination == AppDestination.SPLASH) {
            delay(SPLASH_DURATION_MILLIS)
            destination = AppDestination.ONBOARDING
        }
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
        when (destination) {
            AppDestination.SPLASH -> SplashScreen()
            AppDestination.ONBOARDING -> OnboardingScreen(
                pageIndex = onboardingPage,
                onNext = {
                    if (onboardingPage < LAST_ONBOARDING_PAGE) onboardingPage++ else destination = AppDestination.HOME
                },
                onSkip = { destination = AppDestination.HOME }
            )
            AppDestination.HOME -> HomeScreen(
                selectedMode = selectedMode,
                onModeSelected = {
                    selectedMode = it
                    if (it != GameModeType.CLASSIC) mrWhiteCount = 0
                },
                onContinue = { destination = AppDestination.PLAYER_COUNT },
                modifier = Modifier.safeDrawingPadding()
            )
            AppDestination.PLAYER_COUNT -> PlayerCountScreen(
                playerCount = playerCount,
                onPlayerCountChange = { playerCount = it },
                onBack = { destination = AppDestination.HOME },
                onContinue = {
                    playerNicknames = List(playerCount) { playerNicknames.getOrNull(it).orEmpty() }
                    playerAvatarIndices = randomizedAvatars(playerCount, playerAvatarIndices)
                    currentPlayerIndex = 0
                    destination = AppDestination.PLAYER_NAME
                },
                modifier = Modifier
            )
            AppDestination.PLAYER_NAME -> PlayerNameRoute(
                state = PlayerNameUiState(
                    playerIndex = currentPlayerIndex,
                    nickname = playerNicknames.getOrElse(currentPlayerIndex) { "" },
                    avatarRes = PLAYER_AVATARS[playerAvatarIndices.getOrElse(currentPlayerIndex) { 0 }],
                    photo = playerPhotos[currentPlayerIndex]
                ),
                callbacks = PlayerNameCallbacks(
                    onNicknameChange = { nickname ->
                        playerNicknames = playerNicknames.toMutableList().also {
                            while (it.size < playerCount) it.add("")
                            it[currentPlayerIndex] = nickname
                        }
                    },
                    onBack = {
                        if (currentPlayerIndex == 0) {
                            destination = AppDestination.PLAYER_COUNT
                        } else {
                            currentPlayerIndex--
                        }
                    },
                    onContinue = {
                        if (currentPlayerIndex < playerCount - 1) {
                            currentPlayerIndex++
                        } else {
                            destination = AppDestination.IMPOSTOR_COUNT
                        }
                    }
                ),
                onPhotoTaken = { playerPhotos[currentPlayerIndex] = it },
                modifier = Modifier
            )
            AppDestination.IMPOSTOR_COUNT -> ImpostorCountScreen(
                mode = requireNotNull(selectedMode),
                playerCount = playerCount,
                impostorCount = impostorCount,
                mrWhiteCount = mrWhiteCount,
                selectedRole = selectedRole,
                onImpostorCountChange = { impostorCount = it },
                onMrWhiteCountChange = { mrWhiteCount = it },
                onRoleSelected = { selectedRole = it },
                onBack = {
                    currentPlayerIndex = playerCount - 1
                    destination = AppDestination.PLAYER_NAME
                },
                onContinue = {
                    currentPlayerIndex = 0
                    gameSessionKey = java.util.UUID.randomUUID().toString()
                    destination = AppDestination.PLAYER_TURN
                },
                modifier = Modifier
            )
            AppDestination.PLAYER_TURN -> {
                val players = playerNicknames.mapIndexed { index, nickname ->
                    TurnPlayerUi(
                        name = nickname,
                        avatarRes = PLAYER_AVATARS[playerAvatarIndices[index]],
                        photo = playerPhotos[index]
                    )
                }
                com.impostor.app.ui.screens.GameFlowRoute(
                    setup = com.impostor.app.ui.screens.GameFlowSetup(requireNotNull(selectedMode), impostorCount, mrWhiteCount, players),
                    sessionKey = gameSessionKey,
                    onExit = { destination = AppDestination.HOME }
                )
            }
        }
    }
}

private fun randomizedAvatars(playerCount: Int, previous: List<Int>): List<Int> {
    val validPrevious = previous.filter { it in PLAYER_AVATARS.indices }.take(playerCount)
    val unused = PLAYER_AVATARS.indices.filterNot(validPrevious::contains).shuffled()
    return (validPrevious + unused).take(playerCount)
}

private const val SPLASH_DURATION_MILLIS = 1_200L
private const val LAST_ONBOARDING_PAGE = 3
private const val DEFAULT_PLAYER_COUNT = 8
private const val DEFAULT_IMPOSTOR_COUNT = 2
