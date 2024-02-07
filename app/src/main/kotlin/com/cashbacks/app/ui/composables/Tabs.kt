package com.cashbacks.app.ui.composables

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.ui.theme.CashbacksTheme
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.util.reversed
import kotlinx.coroutines.launch


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PrimaryTabsLayout(
    pages: Array<AppScreens.TabPages>,
    modifier: Modifier = Modifier,
    tabRowColor: Color = MaterialTheme.colorScheme.primary,
    scrollEnabled: Boolean = true,
    pagerState: PagerState = rememberPagerState(pageCount = pages::size),
    content: @Composable ((page: AppScreens.TabPages) -> Unit)
) {
    TabsLayout(pages, isPrimary = true, modifier, tabRowColor, scrollEnabled, pagerState, content)
}


@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun SecondaryTabsLayout(
    pages: Array<AppScreens.TabPages>,
    modifier: Modifier = Modifier,
    tabRowColor: Color = Color.Transparent,
    scrollEnabled: Boolean = true,
    pagerState: PagerState = rememberPagerState(pageCount = pages::size),
    content: @Composable ((page: AppScreens.TabPages) -> Unit)
) {
    TabsLayout(pages, isPrimary = false, modifier, tabRowColor, scrollEnabled, pagerState, content)
}


@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TabsLayout(
    pages: Array<AppScreens.TabPages>,
    isPrimary: Boolean,
    modifier: Modifier = Modifier,
    tabRowColor: Color = if (isPrimary) MaterialTheme.colorScheme.primary else Color.Transparent,
    scrollEnabled: Boolean = true,
    pagerState: PagerState = rememberPagerState { pages.size },
    content: @Composable ((page: AppScreens.TabPages) -> Unit)
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
        isPrimary -> contentColorFor(tabRowColor).reversed
        else -> MaterialTheme.colorScheme.primary
    }


    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .then(modifier)
            .fillMaxSize()
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
                        color = MaterialTheme.colorScheme.primaryContainer.animate()
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
                        Icon(imageVector = page.icon, contentDescription = "tab logo")
                    },
                    text = {
                        Text(
                            text = stringResource(page.tabTitleRes),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    },
                    enabled = scrollEnabled || selectedTabIndex.value == index,
                    selectedContentColor = selectedTabColor.animate(),
                    unselectedContentColor = contentColorFor(tabRowColor).animate()
                )
            }
        }

        Spacer(
            modifier = when {
                isPrimary -> Modifier.height(16.dp)
                else -> Modifier
            }
        )

        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            userScrollEnabled = scrollEnabled,
            modifier = Modifier.fillMaxWidth(),
            pageContent = { page -> content(pages[page]) }
        )
    }
}


@Composable
internal fun <T : Any> PrimaryListContentTabPage(
    items: List<T>?,
    placeholderText: String,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    itemComposable: @Composable ((T) -> Unit)
) {
    ListContentTabPage(
        items, isPrimary = true, placeholderText, contentPadding, itemComposable
    )
}


@Composable
internal fun <T : Any> SecondaryListContentTabPage(
    items: List<T>?,
    placeholderText: String,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    itemComposable: @Composable ((T) -> Unit)
) {
    ListContentTabPage(
        items, isPrimary = false, placeholderText, contentPadding, itemComposable
    )
}



@Composable
private fun <T> ListContentTabPage(
    items: List<T>?,
    isPrimary: Boolean,
    placeholderText: String,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    itemComposable: @Composable ((T) -> Unit)
) {
    val lazyListState = rememberLazyListState()

    val modifier = when {
        isPrimary -> Modifier
        else -> Modifier.clip(MaterialTheme.shapes.small)
    }

    Crossfade(
        targetState = items,
        label = "tabItems",
        modifier = Modifier
            .then(modifier)
            .background(MaterialTheme.colorScheme.background.animate())
            .padding(contentPadding)
    ) { list ->
        when {
            list == null -> LoadingInBox()
            list.isEmpty() -> EmptyList(
                text = placeholderText,
                icon = Icons.Rounded.DataArray,
                iconModifier = Modifier.scale(2.5f),
                modifier = Modifier.clip(MaterialTheme.shapes.small)
            )
            else -> {
                LazyColumn(
                    state = lazyListState,
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(list, itemContent = { itemComposable(it) })
                }
            }
        }
    }
}




@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun PrimaryTabLayoutPreview() {
    CashbacksTheme(isDarkTheme = false) {
        PrimaryTabsLayout(
            pages = arrayOf(AppScreens.Shop, AppScreens.Cashback),
            modifier = Modifier.background(MaterialTheme.colorScheme.background),
            tabRowColor = MaterialTheme.colorScheme.primary
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Preview")
            }
        }
    }
}


@OptIn(ExperimentalFoundationApi::class)
@Preview
@Composable
private fun SecondaryTabsLayoutPreview() {
    CashbacksTheme(isDarkTheme = true) {
        SecondaryTabsLayout(
            pages = arrayOf(AppScreens.Shop, AppScreens.Cashback),
            modifier = Modifier
                .background(MaterialTheme.colorScheme.background)
                .clip(RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)),
            tabRowColor = Color.Transparent
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Preview")
            }
        }
    }
}