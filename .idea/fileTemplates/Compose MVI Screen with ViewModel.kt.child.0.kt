#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME} #end

internal sealed interface ${NAME}Action {
    
}


internal sealed interface ${NAME}Label {
    
}


internal sealed interface ${NAME}Intent {
    
}


internal sealed interface ${NAME}Message {
    
}


internal data class ${NAME}State(
    val paramOne: String = "default",
    val paramTwo: List<String> = emptyList(),
)