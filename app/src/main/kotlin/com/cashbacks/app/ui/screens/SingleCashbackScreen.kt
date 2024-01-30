package com.cashbacks.app.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.rounded.ArrowBackIosNew
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.cashbacks.app.R
import com.cashbacks.app.ui.composables.EditableTextField
import com.cashbacks.app.ui.managment.ViewModelState
import com.cashbacks.app.ui.screens.navigation.AppScreens
import com.cashbacks.app.util.LoadingInBox
import com.cashbacks.app.util.animate
import com.cashbacks.app.viewmodel.CashbackViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleCashbackScreen(
    viewModel: CashbackViewModel,
    navigateTo: (route: String) -> Unit,
    popBackStack: () -> Unit
) {
    val snackbarHostState = remember(::SnackbarHostState)
    val scope = rememberCoroutineScope()

    val showSnackbar = { message: String ->
        scope.launch { snackbarHostState.showSnackbar(message) }
        Unit
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = stringResource(AppScreens.Cashback.titleRes),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    AnimatedContent(
                        targetState = viewModel.state.value,
                        contentAlignment = Alignment.Center,
                        transitionSpec = {
                            val slideSpec = tween<IntOffset>(durationMillis = 300, easing = LinearEasing)
                            val scaleSpec = tween<Float>(durationMillis = 300, easing = LinearEasing)

                            val enter = when (targetState) {
                                ViewModelState.Editing -> slideInVertically(slideSpec)
                                else -> slideInHorizontally(slideSpec)
                            } + scaleIn(scaleSpec)
                            val exit = when (targetState) {
                                ViewModelState.Editing -> slideOutHorizontally(slideSpec)
                                else -> slideOutVertically(slideSpec)
                            } + scaleOut(scaleSpec)

                            return@AnimatedContent enter togetherWith exit
                        },
                        label = "navigationIconAnimation"
                    ) { vmState ->
                        IconButton(
                            onClick = {
                                if (vmState == ViewModelState.Editing) {
                                    viewModel.deleteCashback(showSnackbar)
                                }
                                popBackStack()
                            },
                            enabled = viewModel.state.value != ViewModelState.Loading
                        ) {
                            Icon(
                                imageVector = when (vmState) {
                                    ViewModelState.Editing -> Icons.Rounded.DeleteOutline
                                    else -> Icons.Rounded.ArrowBackIosNew
                                },
                                contentDescription = null,
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }
                },
                actions = {
                    Crossfade(
                        targetState = viewModel.state.value,
                        animationSpec = tween(durationMillis = 300, easing = LinearEasing),
                        label = "actionIconsAnimation"
                    ) { vmState ->
                        IconButton(
                            onClick = {
                                when (vmState) {
                                    ViewModelState.Editing -> {
                                        viewModel.save()
                                        if (viewModel.cashbackId == null) popBackStack()
                                    }
                                    else -> viewModel.edit()
                                }
                            },
                            enabled = viewModel.state.value != ViewModelState.Loading
                        ) {
                            Icon(
                                imageVector = when (viewModel.state.value) {
                                    ViewModelState.Editing -> Icons.Outlined.Save
                                    else -> Icons.Outlined.Edit
                                },
                                contentDescription = null,
                                modifier = Modifier.scale(1.2f)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary.animate(),
                    titleContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary.animate(),
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary.animate()
                )
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) {
                Snackbar(
                    snackbarData = it,
                    containerColor = MaterialTheme.colorScheme.onBackground.animate(),
                    contentColor = MaterialTheme.colorScheme.background.animate()
                )
            }
        }
    ) { contentPadding ->
        Crossfade(
            targetState = viewModel.state.value,
            label = "loading",
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) { state ->
            when (state) {
                ViewModelState.Loading -> LoadingInBox()
                else -> CashbackContent(viewModel)
            }
        }
    }
}


@Composable
private fun CashbackContent(viewModel: CashbackViewModel) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Spacer(Modifier.height(16.dp))
        EditableTextField(
            text = viewModel.cashback.value.amount,
            label = stringResource(R.string.amount),
            onTextChange = viewModel.cashback.value::amount::set,
            enabled = viewModel.state.value == ViewModelState.Editing
        )

        EditableTextField(
            text = viewModel.cashback.value.expirationDate,
            label = stringResource(R.string.expiration_date),
            onTextChange = viewModel.cashback.value::expirationDate::set,
            keyboardType = KeyboardType.Number,
            enabled = viewModel.state.value == ViewModelState.Editing
        )

        EditableTextField(
            text = viewModel.cashback.value.comment,
            label = stringResource(R.string.comment),
            onTextChange = viewModel.cashback.value::comment::set,
            singleLine = false,
            imeAction = ImeAction.Done,
            enabled = viewModel.state.value == ViewModelState.Editing
        )
    }
}