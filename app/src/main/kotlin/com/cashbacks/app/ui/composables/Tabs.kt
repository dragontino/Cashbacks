package com.cashbacks.app.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.cashbacks.app.ui.managment.ListState
import com.cashbacks.app.ui.navigation.AppBarItem
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import kotlinx.coroutines.launch


@Composable
internal fun <T : AppBarItem> PrimaryTabsLayout(
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
internal fun <T : AppBarItem> SecondaryTabsLayout(
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

    val scrollToPage = { index: Int ->
        scope.launch {
            pagerState.animateScrollToPage(
                page = index,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing)
            )
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


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth()
    ) {
        PrimaryTabRow(
            selectedTabIndex = selectedTabIndex.value,
            containerColor = tabRowColor.animate(),
            contentColor = contentColorFor(tabRowColor).animate(),
            indicator = {
                when {
                    isPrimary -> TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            selectedTabIndex.value,
                            matchContentSize = false
                        ),
                        color = MaterialTheme.colorScheme.onBackground.animate()
                    )
                    else -> TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(
                            selectedTabIndex.value,
                            matchContentSize = false
                        )
                    )
                }
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