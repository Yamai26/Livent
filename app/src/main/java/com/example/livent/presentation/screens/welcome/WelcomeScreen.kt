package com.example.livent.presentation.screens.welcome

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.livent.presentation.components.LiventPrimaryButton
import com.example.livent.presentation.components.LiventSecondaryButton
import com.example.livent.presentation.components.LiventTextButton
import com.example.livent.presentation.theme.LiventDimens

@Composable
fun WelcomeScreen(
    onSignUp: () -> Unit,
    onLogIn: () -> Unit,
    onBrowseFirst: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(LiventDimens.PaddingLarge),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Placeholder for the hero image in the mockup
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = LiventDimens.PaddingLarge)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Livent",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = "Encuentra eventos cerca de ti",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Spacer(modifier = Modifier.height(LiventDimens.PaddingSmall))
        
        Text(
            text = "Descubre eventos, guarda favoritos y vive experiencias únicas.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        
        Spacer(modifier = Modifier.height(LiventDimens.PaddingExtraLarge))
        
        LiventPrimaryButton(
            text = "Registrarse",
            onClick = onSignUp
        )
        
        Spacer(modifier = Modifier.height(LiventDimens.PaddingMedium))
        
        LiventSecondaryButton(
            text = "Iniciar sesión",
            onClick = onLogIn
        )
        
        Spacer(modifier = Modifier.height(LiventDimens.PaddingSmall))
        
        LiventTextButton(
            text = "Explorar primero",
            onClick = onBrowseFirst
        )
        
        Spacer(modifier = Modifier.height(LiventDimens.PaddingMedium))
    }
}
