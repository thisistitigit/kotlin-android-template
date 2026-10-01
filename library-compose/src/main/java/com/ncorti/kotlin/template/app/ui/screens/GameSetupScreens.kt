@file:Suppress("MagicNumber")

package com.ncorti.kotlin.template.app.ui.screens

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.ncorti.kotlin.template.app.ui.components.AppButton
import com.ncorti.kotlin.template.app.ui.components.AppButtonStyle
import com.ncorti.kotlin.template.app.ui.components.AppCard
import com.ncorti.kotlin.template.app.ui.components.BackButton
import com.ncorti.kotlin.template.app.ui.components.NumberSelector
import com.ncorti.kotlin.template.app.ui.components.NumberSummaryPosition
import com.ncorti.kotlin.template.app.ui.components.RoleOption
import com.ncorti.kotlin.template.app.ui.components.RoleSelector
import com.ncorti.kotlin.template.app.ui.components.PlayerAvatar
import com.ncorti.kotlin.template.app.ui.components.PlayerAvatarDecoration
import com.ncorti.kotlin.template.app.ui.theme.AppBackground
import com.ncorti.kotlin.template.app.ui.theme.AppTextStyles
import com.ncorti.kotlin.template.app.ui.theme.AppWhite
import com.ncorti.kotlin.template.app.ui.theme.DeepPurple
import com.ncorti.kotlin.template.app.ui.theme.MarkerYellow
import com.ncorti.kotlin.template.app.ui.theme.MainPurple
import com.ncorti.kotlin.template.app.ui.theme.Spacing
import com.ncorti.kotlin.template.app.ui.theme.TemplateTheme
import com.ncorti.kotlin.template.library.compose.R
import com.ncorti.kotlin.template.library.domain.enums.GameModeType
import com.ncorti.kotlin.template.library.domain.enums.RoleType

@Composable
fun PlayerCountScreen(
    playerCount: Int,
    onPlayerCountChange: (Int) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    SetupScreen(
        title = stringResource(R.string.players_question),
        onBack = onBack,
        onContinue = onContinue,
        modifier = modifier
    ) {
        Spacer(Modifier.weight(1f))
        NumberSelector(
            values = MIN_PLAYERS..MAX_PLAYERS,
            selectedValue = playerCount,
            onValueChange = onPlayerCountChange,
            summaryPosition = NumberSummaryPosition.ABOVE,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(1.2f))
    }
}

@Suppress("LongParameterList")
@Composable
fun ImpostorCountScreen(
    mode: GameModeType,
    playerCount: Int,
    impostorCount: Int,
    mrWhiteCount: Int,
    selectedRole: RoleType,
    onImpostorCountChange: (Int) -> Unit,
    onMrWhiteCountChange: (Int) -> Unit,
    onRoleSelected: (RoleType) -> Unit,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    val supportsMrWhite = mode == GameModeType.CLASSIC
    val role = if (supportsMrWhite) selectedRole else RoleType.IMPOSTOR
    val maxImpostors = (playerCount - mrWhiteCount - 1).coerceIn(1, MAX_ADVERSARIES)
    val maxMrWhites = (playerCount - impostorCount - 1).coerceIn(0, MAX_ADVERSARIES)
    val selectedCount = if (role == RoleType.MR_WHITE) mrWhiteCount else impostorCount
    val range = if (role == RoleType.MR_WHITE) 0..maxMrWhites else 1..maxImpostors
    val summary = if (role == RoleType.MR_WHITE) {
        pluralStringResource(R.plurals.mr_white_count, selectedCount)
    } else {
        pluralStringResource(R.plurals.impostor_count, selectedCount)
    }
    val impostorLabel = stringResource(R.string.impostor)
    val roleOptions = if (supportsMrWhite) {
        listOf(
            RoleOption(RoleType.MR_WHITE, stringResource(R.string.mr_white)),
            RoleOption(RoleType.IMPOSTOR, impostorLabel)
        )
    } else {
        listOf(RoleOption(RoleType.IMPOSTOR, impostorLabel))
    }

    SetupScreen(
        title = stringResource(R.string.impostors_question),
        onBack = onBack,
        onContinue = onContinue,
        modifier = modifier,
        continueEnabled = impostorCount + mrWhiteCount < playerCount
    ) {
        Spacer(Modifier.height(Spacing.extraLarge))
        RoleSelector(
            options = roleOptions,
            selectedRole = role,
            onRoleSelected = onRoleSelected,
            modifier = Modifier.padding(horizontal = Spacing.large)
        )
        Spacer(Modifier.weight(1f))
        NumberSelector(
            values = range,
            selectedValue = selectedCount,
            onValueChange = {
                if (role == RoleType.MR_WHITE) onMrWhiteCountChange(it) else onImpostorCountChange(it)
            },
            summaryPosition = NumberSummaryPosition.BELOW,
            summaryLabel = summary,
            formatValue = { it.toString().padStart(2, '0') },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.weight(0.8f))
    }
}

data class TurnPlayerUi(
    val name: String,
    @DrawableRes val avatarRes: Int,
    val photo: Bitmap? = null
)

data class PlayerTurnUiState(
    val currentPlayer: TurnPlayerUi,
    val viewedPlayers: List<TurnPlayerUi>,
    @DrawableRes val catRes: Int,
    val actionLabel: String
)

@Composable
fun PlayerTurnScreen(
    state: PlayerTurnUiState,
    onBack: () -> Unit,
    onSeeContent: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = Spacing.small),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BackButton(
            contentDescription = stringResource(R.string.back),
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(Modifier.height(44.dp))
        Row(horizontalArrangement = Arrangement.Center) {
            Text(text = state.currentPlayer.name.uppercase(), style = AppTextStyles.title, color = AppWhite)
            Text(text = stringResource(R.string.turn_suffix), style = AppTextStyles.title, color = MainPurple)
        }
        Text(
            text = stringResource(R.string.pass_phone_to, state.currentPlayer.name),
            style = androidx.compose.material.MaterialTheme.typography.body1,
            color = AppWhite.copy(alpha = 0.82f)
        )
        Image(
            painter = painterResource(state.catRes),
            contentDescription = null,
            contentScale = ContentScale.Fit,
            modifier = Modifier.size(292.dp).padding(top = Spacing.medium)
        )
        Spacer(Modifier.weight(1f))
        ViewedPlayersCard(
            viewedPlayers = state.viewedPlayers,
            currentPlayer = state.currentPlayer,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(30.dp))
        AppButton(
            text = state.actionLabel,
            onClick = onSeeContent,
            style = AppButtonStyle.SECONDARY,
            modifier = Modifier.width(172.dp)
        )
        Spacer(Modifier.height(Spacing.extraLarge))
    }
}

@Composable
private fun ViewedPlayersCard(
    viewedPlayers: List<TurnPlayerUi>,
    currentPlayer: TurnPlayerUi,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.height(192.dp),
        backgroundColor = DeepPurple
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(modifier = Modifier.width(142.dp), verticalAlignment = Alignment.CenterVertically) {
                viewedPlayers.take(MAX_VISIBLE_VIEWED_PLAYERS).forEachIndexed { index, player ->
                    Box(modifier = Modifier.offset(x = (-12 * index).dp)) {
                        PlayerAvatar(
                            avatarRes = player.avatarRes,
                            photo = player.photo,
                            decoration = PlayerAvatarDecoration(AppWhite, 2.dp),
                            modifier = Modifier.size(58.dp)
                        )
                        Text(
                            text = "✓",
                            color = AppBackground,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.align(Alignment.BottomEnd).size(18.dp)
                                .clip(CircleShape).background(MarkerYellow)
                        )
                    }
                }
            }
            Text(text = "→", style = AppTextStyles.scrollNumber, color = MainPurple)
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PlayerAvatar(
                    avatarRes = currentPlayer.avatarRes,
                    photo = currentPlayer.photo,
                    decoration = PlayerAvatarDecoration(MarkerYellow, 2.dp),
                    modifier = Modifier.size(64.dp)
                )
                Text(text = currentPlayer.name, style = AppTextStyles.labelsScroll, color = AppWhite)
            }
        }
    }
}


@Suppress("LongParameterList")
@Composable
private fun SetupScreen(
    title: String,
    onBack: () -> Unit,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    continueEnabled: Boolean = true,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally) {
        BackButton(
            contentDescription = stringResource(R.string.back),
            onClick = onBack,
            modifier = Modifier.align(Alignment.Start).padding(start = Spacing.small, top = Spacing.small)
        )
        Spacer(Modifier.height(Spacing.large))
        Text(
            text = stringResource(R.string.how_many),
            style = AppTextStyles.title,
            color = AppWhite,
            textAlign = TextAlign.Center
        )
        Text(text = title, style = AppTextStyles.title, color = MainPurple, textAlign = TextAlign.Center)
        content()
        AppButton(
            text = stringResource(R.string.continue_label),
            onClick = onContinue,
            enabled = continueEnabled,
            style = AppButtonStyle.OUTLINED,
            showShadow = true,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 72.dp)
        )
        Spacer(Modifier.height(Spacing.extraLarge))
    }
}

private const val MIN_PLAYERS = 3
private const val MAX_PLAYERS = 10
private const val MAX_ADVERSARIES = 4
private const val MAX_VISIBLE_VIEWED_PLAYERS = 3

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
internal fun PlayerCountPreview() = TemplateTheme {
    PlayerCountScreen(8, {}, {}, {})
}

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
internal fun ClassicImpostorCountPreview() = TemplateTheme {
    ImpostorCountScreen(GameModeType.CLASSIC, 8, 2, 1, RoleType.MR_WHITE, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
internal fun PlayerTurnPreview() = TemplateTheme {
    PlayerTurnScreen(
        state = PlayerTurnUiState(
            currentPlayer = TurnPlayerUi("Tiago", R.drawable.vibrent_4),
            viewedPlayers = listOf(
                TurnPlayerUi("Ana", R.drawable.vibrent_1),
                TurnPlayerUi("Luís", R.drawable.vibrent_2),
                TurnPlayerUi("Mia", R.drawable.vibrent_3)
            ),
            catRes = R.drawable.cat_turn_01,
            actionLabel = "See Word"
        ),
        onBack = {},
        onSeeContent = {}
    )
}

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
private fun QuestionImpostorCountPreview() = TemplateTheme {
    ImpostorCountScreen(GameModeType.QUESTION, 8, 2, 0, RoleType.IMPOSTOR, {}, {}, {}, {}, {})
}
