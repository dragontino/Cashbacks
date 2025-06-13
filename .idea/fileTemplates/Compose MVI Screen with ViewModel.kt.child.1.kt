#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME} #end

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.arkivanov.mvikotlin.core.store.Store
import com.arkivanov.mvikotlin.core.store.StoreFactory
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineBootstrapper
import com.arkivanov.mvikotlin.extensions.coroutines.coroutineExecutorFactory
import com.arkivanov.mvikotlin.extensions.coroutines.labels
import com.arkivanov.mvikotlin.extensions.coroutines.stateFlow
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "") import ${PACKAGE_NAME}.${NAME}Action #end
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "") import ${PACKAGE_NAME}.${NAME}Label #end
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "") import ${PACKAGE_NAME}.${NAME}Intent #end
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "") import ${PACKAGE_NAME}.${NAME}Message #end
#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "") import ${PACKAGE_NAME}.${NAME}State #end
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow

class ${NAME}ViewModel(
    private val storeFactory: StoreFactory
) : ViewModel() {

    private val ${NAME}Store: Store<${NAME}Intent, ${NAME}State, ${NAME}Label> by lazy {
        object : Store<${NAME}Intent, ${NAME}State, ${NAME}Label> by storeFactory.create(
            name = "${NAME}Store",
            initialState = ${NAME}State(),
            bootstrapper = coroutineBootstrapper<${NAME}Action> {
                
            },
            executorFactory = coroutineExecutorFactory {
                TODO("Handle actions and intents")
            },
            reducer = { message: ${NAME}Message ->
                when (message) {
                    else -> TODO("Handle messages")
                }
            }
        ) {}
    }


    val stateFlow: StateFlow<${NAME}State> = ${NAME}Store.stateFlow(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000)
    )

    val labelFlow: Flow<${NAME}Label> by lazy { ${NAME}Store.labels }


    fun sendIntent(intent: ${NAME}Intent) {
        ${NAME}Store.accept(intent)
    }
}