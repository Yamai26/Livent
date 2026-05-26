package com.example.livent.presentation.screens.publisher

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
import com.example.livent.presentation.components.LiventPrimaryButton
import com.example.livent.presentation.components.LiventSecondaryButton
import com.example.livent.presentation.theme.LiventDimens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FreePlanLimitBottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onSubscribePremium: () -> Unit,
) {
    if (!visible) return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(modifier = Modifier.padding(horizontal = LiventDimens.PaddingLarge, vertical = LiventDimens.PaddingMedium)) {
            Text(
                text = "Límite del plan gratuito",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Solo puedes tener un evento activo a la vez. " +
                    "Suscríbete a Premium (9,99 €/mes) para publicar sin límites.",
                modifier = Modifier.padding(vertical = LiventDimens.PaddingMedium),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(LiventDimens.PaddingSmall))
            
            LiventPrimaryButton(
                text = "Suscribirse a Premium",
                onClick = {
                    onSubscribePremium()
                    onDismiss()
                }
            )
            
            Spacer(modifier = Modifier.height(LiventDimens.PaddingSmall))
            
            LiventSecondaryButton(
                text = "Más tarde",
                onClick = onDismiss
            )
            
            Spacer(modifier = Modifier.height(LiventDimens.PaddingLarge))
        }
    }
}
