@file:Suppress("MagicNumber")

package com.impostor.app.ui.screens

import androidx.annotation.RawRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.material.Text
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.components.AppButtonStyle
import com.impostor.app.ui.components.CategoryCard
import com.impostor.app.ui.components.GameActionButton
import com.impostor.app.ui.components.GameScreenLayout
import com.impostor.app.ui.components.PageIndicator
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.ImpostorTheme
import com.impostor.app.ui.theme.MainPurple
import com.impostor.app.ui.theme.Spacing
import com.impostor.library.compose.R
import com.impostor.library.domain.enums.QuestionCategory
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.abs

private data class CategoryPresentation(val category: QuestionCategory,
    @StringRes val label: Int, @RawRes val image: Int)

private val categories = listOf(
    CategoryPresentation(QuestionCategory.ACTORS, R.string.category_actors, R.raw.category_actors),
    CategoryPresentation(QuestionCategory.MOVIES, R.string.category_movies, R.raw.category_movies),
    CategoryPresentation(QuestionCategory.NBA, R.string.category_nba, R.raw.category_nba),
    CategoryPresentation(QuestionCategory.FOOTBALL, R.string.category_football, R.raw.category_football),
    CategoryPresentation(QuestionCategory.SERIES, R.string.category_series, R.raw.category_series),
    CategoryPresentation(QuestionCategory.SINGERS, R.string.category_singers, R.raw.category_singers)
)

@Composable
fun CategoryScreen(selectedCategory: QuestionCategory, onCategorySelected: (QuestionCategory) -> Unit,
    onBack: () -> Unit, onContinue: () -> Unit, modifier: Modifier = Modifier, enabled: Boolean = true) {
    var scrolling by remember { mutableStateOf(false) }
    BoxWithConstraints(modifier.fillMaxSize()) {
        val compact = maxHeight < 500.dp
        GameScreenLayout(onBack, scrollContent = false, contentPadding = 0.dp,
            contentArrangement = Arrangement.spacedBy(Spacing.small), bottomAction = {
                GameActionButton(stringResource(R.string.continue_label), onContinue, enabled && !scrolling, AppButtonStyle.OUTLINED)
            }) {
            Text(stringResource(R.string.choose_category_title),
                style = if (compact) AppTextStyles.labelsScroll else AppTextStyles.title,
                color = AppWhite, textAlign = TextAlign.Center)
            Text(stringResource(R.string.category_title),
                style = if (compact) AppTextStyles.labelsScroll else AppTextStyles.title,
                color = MainPurple, textAlign = TextAlign.Center)
            CategoryCarousel(selectedCategory, onCategorySelected, enabled,
                onScrollingChanged = { scrolling = it }, modifier = Modifier.weight(1f).fillMaxWidth())
        }
    }
}

@Composable
private fun CategoryCarousel(selected: QuestionCategory, onSelected: (QuestionCategory) -> Unit,
    enabled: Boolean, onScrollingChanged: (Boolean) -> Unit, modifier: Modifier = Modifier) {
    val selectedIndex = categories.indexOfFirst { it.category == selected }
    val pager = rememberPagerState(initialPage = selectedIndex, pageCount = { categories.size })
    val scope = rememberCoroutineScope()
    LaunchedEffect(selected) {
        if (!pager.isScrollInProgress && pager.currentPage != selectedIndex) pager.scrollToPage(selectedIndex)
    }
    LaunchedEffect(pager) {
        snapshotFlow { pager.settledPage }.distinctUntilChanged().collect { onSelected(categories[it].category) }
    }
    LaunchedEffect(pager) {
        snapshotFlow { pager.isScrollInProgress }.distinctUntilChanged().collect(onScrollingChanged)
    }
    BoxWithConstraints(modifier, contentAlignment = Alignment.Center) {
        val availableWidth = maxWidth
        val maximumCardHeight = minOf(330.dp, (maxHeight - 36.dp).coerceAtLeast(48.dp))
        val cardWidth = minOf(maxWidth * 0.5f, maximumCardHeight * (446f / 701f))
        val cardHeight = cardWidth * (701f / 446f)
        Column(horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.medium)) {
            HorizontalPager(pager, modifier = Modifier.fillMaxWidth().height(cardHeight),
                pageSize = PageSize.Fixed(cardWidth), pageSpacing = Spacing.medium,
                contentPadding = PaddingValues(horizontal = (availableWidth - cardWidth) / 2),
                userScrollEnabled = enabled, key = { categories[it].category.key }) { page ->
                val category = categories[page]
                val focused = page == pager.currentPage
                CategoryCard(category.image, stringResource(category.label), focused,
                    onClick = { scope.launch { pager.animateScrollToPage(page) } }, enabled = enabled,
                    modifier = Modifier.width(cardWidth).height(cardHeight).graphicsLayer {
                        val distance = abs(pager.currentPage - page + pager.currentPageOffsetFraction).coerceIn(0f, 1f)
                        scaleX = 1f - distance * 0.18f
                        scaleY = scaleX
                        alpha = 1f - distance * 0.6f
                    })
            }
            PageIndicator(categories.size, pager.currentPage, selectedColor = MainPurple)
        }
    }
}

@Preview @Composable internal fun CategoryPreview() = ImpostorTheme {
    CategoryScreen(QuestionCategory.NBA, {}, {}, {})
}
@Preview(widthDp = 320, heightDp = 480) @Composable internal fun CategorySmallPreview() = ImpostorTheme {
    CategoryScreen(QuestionCategory.NBA, {}, {}, {})
}
@Preview(widthDp = 800, heightDp = 600) @Composable internal fun CategoryLargePreview() = ImpostorTheme {
    CategoryScreen(QuestionCategory.SERIES, {}, {}, {})
}

internal fun categoryLabelResource(category: QuestionCategory): Int = categories.single { it.category == category }.label

@Preview @Composable internal fun CategoryActorsPreview() = ImpostorTheme {
    CategoryScreen(QuestionCategory.ACTORS, {}, {}, {})
}
@Preview @Composable internal fun CategoryMoviesPreview() = ImpostorTheme {
    CategoryScreen(QuestionCategory.MOVIES, {}, {}, {})
}
@Preview @Composable internal fun CategoryFootballPreview() = ImpostorTheme {
    CategoryScreen(QuestionCategory.FOOTBALL, {}, {}, {})
}
@Preview @Composable internal fun CategorySingersPreview() = ImpostorTheme {
    CategoryScreen(QuestionCategory.SINGERS, {}, {}, {})
}
