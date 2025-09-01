package com.cashbacks.common.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DataArray
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabPosition
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.common.composables.management.ListState
import com.cashbacks.common.composables.model.AppBarItem
import com.cashbacks.common.composables.utils.animate
import kotlinx.coroutines.launch
import kotlin.math.abs


@Composable
fun <T : AppBarItem> PrimaryTabsLayout(
    pages: List<T>,
    modifier: Modifier = Modifier,
    tabRowColor: Color = MaterialTheme.colorScheme.primary,
    scrollEnabled: Boolean = true,
    pagerState: PagerState = rememberPagerState(pageCount = pages::size),
    contentIndexed: @Composable ((index: Int, page: T) -> Unit)
) {
    TabsLayout(pages.toList(), isPrimary = true, modifier, tabRowColor, scrollEnabled, pagerState, contentIndexed)
}


@Composable
fun <T : AppBarItem> SecondaryTabsLayout(
    pages: Collection<T>,
    modifier: Modifier = Modifier,
    tabRowColor: Color = Color.Transparent,
    scrollEnabled: Boolean = true,
    pagerState: PagerState = rememberPagerState(pageCount = pages::size),
    contentIndexed: @Composable ((index: Int, page: T) -> Unit)
) {
    TabsLayout(pages.toList(), isPrimary = false, modifier, tabRowColor, scrollEnabled, pagerState, contentIndexed)
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T : AppBarItem> TabsLayout(
    pages: List<T>,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    tabRowColor: Color = if (isPrimary) MaterialTheme.colorScheme.primary else Color.Transparent,
    scrollEnabled: Boolean = true,
    pagerState: PagerState = rememberPagerState { pages.size },
    contentIndexed: @Composable ((index: Int, page: T) -> Unit)
) {

    val scope = rememberCoroutineScope()
    val selectedTabIndex = remember {
        derivedStateOf { pagerState.currentPage }
    }

    val scrollToPage = remember(scope) {
        fun(index: Int) {
            scope.launch {
                pagerState.animateScrollToPage(
                    page = index,
                    animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val selectedTabColor = when {
        isPrimary -> Color.White
        else -> MaterialTheme.colorScheme.primary
    }
    val unselectedTabColor = when {
        isPrimary -> Color.Black
        else -> MaterialTheme.colorScheme.onBackground
    }
    val indicatorColor = when {
        isPrimary -> MaterialTheme.colorScheme.onBackground
        else -> MaterialTheme.colorScheme.primary
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        TabRow(
            selectedTabIndex = selectedTabIndex.value,
            containerColor = tabRowColor.animate(),
            contentColor = contentColorFor(tabRowColor).animate(),
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    color = indicatorColor.animate(),
                    modifier = Modifier.tabIndicatorOffset(
                        currentTabPosition = tabPositions[pagerState.currentPage]
                    )
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            pages.forEachIndexed { index, page ->
                Tab(
                    selected = selectedTabIndex.value == index,
                    onClick = { scrollToPage(index) },
                    icon = {
                        Icon(
                            imageVector = when (index) {
                                selectedTabIndex.value -> page.selectedIcon
                                else -> page.unselectedIcon
                            },
                            contentDescription = "tab logo",
                            modifier = Modifier.height(35.dp)
                        )
                    },
                    text = {
                        Text(
                            text = page.tabTitle,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    enabled = scrollEnabled || selectedTabIndex.value == index,
                    selectedContentColor = selectedTabColor.animate(),
                    unselectedContentColor = unselectedTabColor.animate()
                )
            }
        }

        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            userScrollEnabled = scrollEnabled,
            modifier = Modifier.fillMaxWidth(),
            pageContent = { page -> contentIndexed(page, pages[page]) }
        )
    }
}


private fun Modifier.smoothTabIndicatorOffset(
    currentTabPosition: TabPosition,
    pagerState: PagerState
): Modifier = composed {
    val currentTabWidth by animateDpAsState(
        targetValue = currentTabPosition.width,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )
    val indicatorOffset by animateDpAsState(
        targetValue = getPageIndicatorOffset(currentTabPosition, pagerState),
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing)
    )
    fillMaxWidth()
        .wrapContentSize(Alignment.BottomStart)
        .offset(x = getPageIndicatorOffset(currentTabPosition, pagerState))
        .width(currentTabPosition.width)
}


@Composable
private fun getPageIndicatorOffset(currentTabPosition: TabPosition, pagerState: PagerState): Dp {
    val isDragging = pagerState.interactionSource.collectIsDraggedAsState()

    if (!isDragging.value) {
        return currentTabPosition.left
    }

    val isScrolling = pagerState.isScrollInProgress
    val currentTab = pagerState.currentPage
    val settledTab = pagerState.settledPage

    if (isScrolling) {
        if (settledTab == currentTab) { // We are in the first half of scrolling to a next page
            return currentTabPosition.left + currentTabPosition.width * pagerState.currentPageOffsetFraction
        }
        // We are in the second half, now the currentab.left is the target position so we use this to create a smooth transition using the fraction
        val offsetFraction = (1 - abs(pagerState.currentPageOffsetFraction))
        val settledPageLeft = currentTabPosition.left - currentTabPosition.width // Should work as long as we dont mess with the width
        return settledPageLeft + currentTabPosition.width * offsetFraction
    }
    return currentTabPosition.left.animate()
}


@Composable
fun <T : Any> ListContentTabPage(
    contentState: ListState<T>,
    placeholderText: String,
    modifier: Modifier = Modifier,
    state: LazyListState = rememberLazyListState(),
    contentPadding: PaddingValues = PaddingValues(16.dp),
    bottomSpacing: Dp = 0.dp,
    itemComposable: @Composable ((index: Int, item: T) -> Unit)
) {
    Crossfade(
        targetState = contentState,
        label = "tabItems",
        modifier = Modifier
            .then(modifier)
            .clip(MaterialTheme.shapes.small)
            .background(MaterialTheme.colorScheme.background.animate())
    ) { listState ->
        when (listState) {
            is ListState.Loading -> LoadingInBox()
            is ListState.Empty -> EmptyList(
                text = placeholderText,
                icon = Icons.Rounded.DataArray,
                iconModifier = Modifier.scale(2.5f),
                modifier = Modifier
                    .padding(contentPadding)
                    .fillMaxSize()
            )
            is ListState.Stable<T> -> {
                LazyColumn(
                    state = state,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = contentPadding,
                    modifier = Modifier.fillMaxSize()
                ) {
                    itemsIndexed(listState.data) { index, item ->
                        itemComposable(index, item)
                    }
                    item {
                        Spacer(modifier = Modifier.height(bottomSpacing))
                    }
                }
            }
        }
    }
}