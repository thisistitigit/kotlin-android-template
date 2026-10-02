package com.impostor.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.impostor.app.ui.theme.AppBackground
import com.impostor.app.ui.theme.AppTextStyles
import com.impostor.app.ui.theme.AppWhite
import com.impostor.app.ui.theme.LightPurple
import com.impostor.app.ui.theme.MarkerYellow
import kotlinx.coroutines.flow.distinctUntilChanged

@Suppress("LongParameterList")
@Composable
fun NumberSelector(
    values: IntRange,
    selectedValue: Int,
    onValueChange: (Int) -> Unit,
    modifier: Modifier = Modifier,
    summaryPosition: NumberSummaryPosition = NumberSummaryPosition.BELOW,
    summaryLabel: String? = null,
    formatValue: (Int) -> String = Int::toString
) {
    require(!values.isEmpty()) { "NumberSelector requires at least one value." }
    val options = values.toList()
    val selected = selectedValue.coerceIn(values)
    val pagerState = rememberPagerState(initialPage = options.indexOf(selected)) { options.size }

    LaunchedEffect(selected, options) {
        val target = options.indexOf(selected)
        if (target != pagerState.currentPage) pagerState.animateScrollToPage(target)
    }
    LaunchedEffect(pagerState, options) {
        snapshotFlow { pagerState.currentPage }
            .distinctUntilChanged()
            .collect { onValueChange(options[it]) }
    }

    Column(
        modifier = modifier.semantics { stateDescription = selected.toString() },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (summaryPosition == NumberSummaryPosition.ABOVE) {
            ChosenNumber(selected, summaryLabel)
            SelectionArrow()
        }
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth().height(SELECTOR_HEIGHT).background(LightPurple),
            contentAlignment = Alignment.Center
        ) {
            HorizontalPager(
                state = pagerState,
                pageSize = PageSize.Fixed(ITEM_WIDTH),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    horizontal = (maxWidth - ITEM_WIDTH) / 2
                ),
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                val isSelected = page == pagerState.currentPage
                Box(Modifier.size(ITEM_WIDTH, SELECTOR_HEIGHT), contentAlignment = Alignment.Center) {
                    Text(
                        text = formatValue(options[page]),
                        style = AppTextStyles.scrollNumber,
                        color = if (isSelected) AppWhite else AppBackground.copy(alpha = 0.65f),
                        textAlign = TextAlign.Center
                    )
                }
            }
            SelectionGuides()
        }
        if (summaryPosition == NumberSummaryPosition.BELOW) {
            SelectionArrow()
            ChosenNumber(selected, summaryLabel)
        }
    }
}

@Composable
private fun SelectionGuides() {
    Canvas(Modifier.size(ITEM_WIDTH, SELECTOR_HEIGHT)) {
        val stroke = 2.dp.toPx()
        drawLine(AppWhite, Offset(0f, 0f), Offset(0f, size.height), stroke, StrokeCap.Square)
        drawLine(AppWhite, Offset(size.width, 0f), Offset(size.width, size.height), stroke, StrokeCap.Square)
    }
}

@Composable
private fun SelectionArrow() {
    Canvas(Modifier.padding(vertical = 12.dp).size(36.dp, 24.dp)) {
        val path = Path().apply {
            moveTo(size.width / 2f, 0f)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(path, MarkerYellow)
    }
}

@Composable
private fun ChosenNumber(value: Int, label: String?) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.Bottom
    ) {
        Text(text = value.toString(), style = AppTextStyles.chosenNumber, color = AppWhite)
        if (label != null) {
            Spacer(Modifier.width(8.dp))
            Text(
                text = label,
                style = AppTextStyles.labelsScroll,
                color = AppWhite.copy(alpha = 0.65f),
                modifier = Modifier.padding(bottom = 12.dp)
            )
        }
    }
}

private val ITEM_WIDTH = 100.dp
private val SELECTOR_HEIGHT = 108.dp
