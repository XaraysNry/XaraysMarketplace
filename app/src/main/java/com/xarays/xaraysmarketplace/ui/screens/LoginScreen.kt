package com.xarays.xaraysmarketplace.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xarays.xaraysmarketplace.ui.components.CustomButton
import com.xarays.xaraysmarketplace.ui.components.CustomTextField

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onBack: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "👤",
                fontSize = 64.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Login",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Masuk untuk melanjutkan pembelian",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(32.dp))

            CustomTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                placeholder = "contoh@email.com"
            )

            Spacer(modifier = Modifier.height(16.dp))

            CustomTextField(
                value = password,
                onValueChange = { password = it },
                label = "Password",
                placeholder = "Masukkan password",
                isPassword = true
            )

            Spacer(modifier = Modifier.height(32.dp))

            CustomButton(
                text = if (isLoading) "Loading..." else "Login",
                onClick = {
                    isLoading = true
                    // Simulasi login
                    // Dalam implementasi nyata, lakukan validasi di sini
                    onLoginSuccess()
                },
                enabled = !isLoading && email.isNotEmpty() && password.isNotEmpty()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text(
                    text = "Kembali",
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}