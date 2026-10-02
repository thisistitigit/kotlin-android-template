@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.material.Icon
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
import androidx.compose.ui.zIndex
import com.impostor.app.ui.components.AppButton
import com.impostor.app.ui.components.AppButtonStyle
import com.impostor.app.ui.components.AppCard
import com.impostor.app.ui.components.AppHeader
import com.impostor.app.ui.components.NumberSelector
import com.impostor.app.ui.components.NumberSummaryPosition
import com.impostor.app.ui.components.RoleOption
import com.impostor.app.ui.components.RoleSelector
import com.impostor.app.ui.components.PlayerAvatar
import com.impostor.app.ui.components.PlayerAvatarDecoration
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.DeepPurple
import com.impostor.app.ui.theme.MarkerYellow
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.Spacing
import com.impostor.app.ui.theme.ImpostorTheme
import com.impostor.library.compose.R
import com.composables.icons.lucide.R as LucideR
import com.impostor.library.domain.enums.GameModeType
import com.impostor.library.domain.enums.RoleType

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
    BoxWithConstraints(modifier = modifier.fillMaxSize().background(AppBackground)) {
        val compact = maxHeight < COMPACT_HEIGHT
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = Spacing.medium),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppHeader(onBack = onBack)
            Spacer(Modifier.height(if (compact) Spacing.small else Spacing.large))
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
                modifier = Modifier.size(if (compact) 210.dp else 292.dp)
            )
            Spacer(Modifier.weight(1f))
            ViewedPlayersCard(
                viewedPlayers = state.viewedPlayers,
                currentPlayer = state.currentPlayer,
                compact = compact,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(if (compact) Spacing.medium else 30.dp))
            AppButton(
                text = state.actionLabel,
                onClick = onSeeContent,
                style = AppButtonStyle.SECONDARY,
                modifier = Modifier.width(172.dp)
            )
            Spacer(Modifier.height(Spacing.medium))
        }
    }
}

@Composable
private fun ViewedPlayersCard(
    viewedPlayers: List<TurnPlayerUi>,
    currentPlayer: TurnPlayerUi,
    compact: Boolean,
    modifier: Modifier = Modifier
) {
    AppCard(
        modifier = modifier.height(if (compact) 150.dp else 182.dp),
        backgroundColor = DeepPurple,
        contentPadding = Spacing.medium
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            SeenAvatars(viewedPlayers, Modifier.weight(1f))
            Icon(
                painter = painterResource(LucideR.drawable.lucide_ic_chevrons_right),
                contentDescription = null,
                tint = MainPurple,
                modifier = Modifier.size(30.dp)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                PlayerAvatar(
                    avatarRes = currentPlayer.avatarRes,
                    photo = currentPlayer.photo,
                    decoration = PlayerAvatarDecoration(MarkerYellow, 2.dp),
                    modifier = Modifier.size(if (compact) 56.dp else 64.dp)
                )
                Text(text = currentPlayer.name, style = AppTextStyles.labelsScroll, color = AppWhite)
            }
        }
    }
}

@Composable
private fun SeenAvatars(players: List<TurnPlayerUi>, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier.height(60.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        players.take(MAX_VISIBLE_VIEWED_PLAYERS).forEachIndexed { index, player ->
            Box(
                modifier = Modifier.size(60.dp)
                    .offset(x = (AVATAR_OVERLAP_STEP * index).dp)
                    .zIndex(index.toFloat())
            ) {
                PlayerAvatar(
                    avatarRes = player.avatarRes,
                    photo = player.photo,
                    decoration = PlayerAvatarDecoration(AppWhite, 2.dp),
                    modifier = Modifier.size(54.dp)
                )
                Box(
                    modifier = Modifier.align(Alignment.BottomEnd).size(20.dp)
                        .clip(CircleShape).background(MarkerYellow),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(LucideR.drawable.lucide_ic_check),
                        contentDescription = null,
                        tint = AppBackground,
                        modifier = Modifier.size(14.dp)
                    )
                }
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
    Column(
        modifier = modifier.fillMaxSize().background(AppBackground),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppHeader(onBack = onBack, modifier = Modifier.padding(horizontal = Spacing.medium))
        Spacer(Modifier.height(Spacing.small))
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
private const val AVATAR_OVERLAP_STEP = 34
val COMPACT_HEIGHT = 720.dp

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
internal fun PlayerCountPreview() = ImpostorTheme {
    PlayerCountScreen(8, {}, {}, {})
}

@Preview(showBackground = true)
@Suppress("UnusedPrivateMember")
@Composable
internal fun ClassicImpostorCountPreview() = ImpostorTheme {
    ImpostorCountScreen(GameModeType.CLASSIC, 8, 2, 1, RoleType.MR_WHITE, {}, {}, {}, {}, {})
}

@Preview(showBackground = true, backgroundColor = 0xFF222222)
@Composable
internal fun PlayerTurnPreview() = ImpostorTheme {
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
private fun QuestionImpostorCountPreview() = ImpostorTheme {
    ImpostorCountScreen(GameModeType.QUESTION, 8, 2, 0, RoleType.IMPOSTOR, {}, {}, {}, {}, {})
}
