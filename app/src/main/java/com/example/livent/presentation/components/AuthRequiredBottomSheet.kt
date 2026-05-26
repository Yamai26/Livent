package com.example.livent.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.livent.presentation.theme.LiventDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthRequiredBottomSheet(
    onDismiss: () -> Unit,
    onLogin: () -> Unit,
    onRegister: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LiventDimens.PaddingLarge, vertical = LiventDimens.PaddingMedium),
            verticalArrangement = Arrangement.spacedBy(LiventDimens.PaddingMedium),
        ) {
            Text(
                text = "Inicia sesión para continuar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Guarda favoritos y accede a tu perfil creando una cuenta o iniciando sesión.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(LiventDimens.PaddingSmall))
            
            LiventPrimaryButton(
                text = "Iniciar sesión",
                onClick = onLogin
            )
            LiventSecondaryButton(
                text = "Registrarse",
                onClick = onRegister
            )
            LiventTextButton(
                text = "Cancelar",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(LiventDimens.PaddingLarge))
        }
    }
}
