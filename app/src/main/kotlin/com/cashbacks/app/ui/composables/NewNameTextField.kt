package com.cashbacks.app.ui.composables

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import com.cashbacks.app.util.animate
import kotlinx.coroutines.delay

@Composable
fun NewNameTextField(
    modifier: Modifier = Modifier,
    placeholder: String = "",
    onSave: (name: String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember(::FocusRequester)

    LaunchedEffect(Unit) {
        delay(50)
        focusRequester.requestFocus()
    }

    OutlinedTextField(
        value = name,
        onValueChange = { name = it },
        placeholder = {
            Text(text = placeholder)
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = { onSave(name) }
        ),
        shape = MaterialTheme.shapes.medium,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.onBackground.animate(),
            focusedContainerColor = MaterialTheme.colorScheme.surface.animate()
        ),
        modifier = Modifier
            .focusRequester(focusRequester)
            .padding(16.dp)
            .then(modifier)
            .fillMaxWidth()
    )
}