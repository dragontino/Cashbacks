#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "") package ${PACKAGE_NAME} #end
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun ${NAME}Root(
    viewModel: ${NAME}ViewModel = koinViewModel()
) {
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    
    LaunchedEffect(Unit) {
        viewModel.labelFlow.collect { label ->
            when (label) {
                else -> TODO("Handle labels")
            }
        }
    }
    
    ${NAME}Screen(
        state = state,
        sendIntent = viewModel::sendIntent
    )
}

@Composable
internal fun ${NAME}Screen(
    state: ${NAME}State,
    sendIntent: (${NAME}Intent) -> Unit,
) {

}

@Preview
@Composable
private fun ${NAME}ScreenPreview() {
    ${PROJECT_NAME}Theme {
        ${NAME}Screen(
            state = ${NAME}State(),
            sendIntent = {}
        )
    }
}