package com.tenco.feature.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.tenco.R

@Composable
fun LoginScreen(
    onLoggedIn: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var code by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf(false) }

    // Dev convenience: prefill the OTP returned by the backend in dev-mode.
    LaunchedEffect(state.devOtp) { state.devOtp?.let { code = it } }
    LaunchedEffect(state.done) { if (state.done) onLoggedIn() }

    val focusManager = androidx.compose.ui.platform.LocalFocusManager.current
    val phoneFocus = remember { androidx.compose.ui.focus.FocusRequester() }
    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(rememberScrollState())
            .imePadding()
            .navigationBarsPadding()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        androidx.compose.foundation.Image(
            painter = androidx.compose.ui.res.painterResource(R.drawable.ic_tc_logo),
            contentDescription = null,
            modifier = Modifier.padding(bottom = 20.dp).size(104.dp),
        )
        Text(
            text = stringResource(R.string.login_title),
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = stringResource(R.string.login_subtitle),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp),
        )

        if (state.phase == LoginPhase.PHONE) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text(stringResource(R.string.your_name)) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { phoneFocus.requestFocus() }),
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(16.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { if (it.length <= 10) { phone = it.filter(Char::isDigit); phoneError = false } },
                label = { Text(stringResource(R.string.phone_number)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    focusManager.clearFocus()
                    if (phone.length == 10) viewModel.sendOtp(name.trim(), phone) else phoneError = true
                }),
                singleLine = true,
                isError = phoneError,
                supportingText = if (phoneError) { { Text(stringResource(R.string.invalid_phone)) } } else null,
                modifier = Modifier.fillMaxWidth().focusRequester(phoneFocus),
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (phone.length == 10) viewModel.sendOtp(name.trim(), phone) else phoneError = true },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (state.loading) com.tenco.ui.components.CoconutLoader(size = 22.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text(stringResource(R.string.send_otp))
            }
        } else {
            OutlinedTextField(
                value = code,
                onValueChange = { if (it.length <= 6) code = it.filter(Char::isDigit) },
                label = { Text(stringResource(R.string.enter_otp)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                isError = state.error != null,
                supportingText = state.error?.let { { Text(it) } },
                modifier = Modifier.fillMaxWidth(),
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { if (code.length in 4..6) viewModel.verify(code) },
                enabled = !state.loading,
                modifier = Modifier.fillMaxWidth().height(56.dp),
            ) {
                if (state.loading) com.tenco.ui.components.CoconutLoader(size = 22.dp, color = MaterialTheme.colorScheme.onPrimary)
                else Text(stringResource(R.string.verify_otp))
            }
        }
    }
}
