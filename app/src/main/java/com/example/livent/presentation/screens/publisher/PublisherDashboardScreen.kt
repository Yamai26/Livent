package com.example.livent.presentation.screens.publisher

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.livent.domain.model.Event
import com.example.livent.domain.model.EventStatus
import com.example.livent.domain.model.SubscriptionTier
import com.example.livent.presentation.publisher.PaymentViewModel
import com.example.livent.presentation.publisher.PublisherDashboardViewModel
import com.example.livent.presentation.theme.LiventDimens
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PublisherDashboardScreen(
    onCreateEvent: () -> Unit,
    onEditEvent: (String) -> Unit,
    onSignOut: () -> Unit,
    onSubscribePremium: () -> Unit,
    onBoostEvent: (String) -> Unit,
    paymentViewModel: PaymentViewModel,
    modifier: Modifier = Modifier,
    viewModel: PublisherDashboardViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    LaunchedEffect(paymentState.errorMessage) {
        paymentState.errorMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            paymentViewModel.clearError()
        }
    }

    LaunchedEffect(paymentState.successMessage) {
        paymentState.successMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            paymentViewModel.clearSuccess()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mis eventos", fontWeight = FontWeight.Bold) },
                actions = {
                    when (uiState.subscriptionTier) {
                        SubscriptionTier.PREMIUM -> {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("Premium") },
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                        SubscriptionTier.FREE -> {
                            TextButton(onClick = onSubscribePremium) {
                                Text("Premium", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    TextButton(onClick = onSignOut) {
                        Text("Salir", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateEvent,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nuevo evento")
            }
        },
    ) { padding ->
        if (uiState.isLoading && uiState.events.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(LiventDimens.PaddingMedium),
                verticalArrangement = Arrangement.spacedBy(LiventDimens.PaddingMedium),
            ) {
                item {
                    StatsRow(
                        activeCount = uiState.activeCount,
                        pastCount = uiState.pastCount,
                        draftCount = uiState.draftCount,
                    )
                }
                if (uiState.events.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Aún no tienes eventos. Pulsa + para crear uno.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                } else {
                    items(uiState.events, key = { it.id }) { event ->
                        EventListItem(
                            event = event,
                            onEdit = { onEditEvent(event.id) },
                            onDelete = { viewModel.requestDelete(event) },
                            onMarkPast = { viewModel.markAsPast(event) },
                            onUnpublish = { viewModel.unpublishToDraft(event) },
                            onBoost = { viewModel.requestBoost(event) },
                        )
                    }
                }
            }
        }
    }

    uiState.eventPendingBoost?.let { event ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissBoostDialog() },
            title = { Text("Destacar evento", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "¿Destacar «${event.title}» en el carrusel de exploración por 2,99 €?",
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.dismissBoostDialog()
                        onBoostEvent(event.id)
                    },
                ) {
                    Text("Pagar 2,99 €", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissBoostDialog() }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(LiventDimens.CornerRadiusLarge)
        )
    }

    if (paymentState.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }

    uiState.eventPendingDelete?.let { event ->
        AlertDialog(
            onDismissRequest = { viewModel.dismissDeleteDialog() },
            title = { Text("Eliminar evento", fontWeight = FontWeight.Bold) },
            text = { Text("¿Eliminar «${event.title}»? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = { viewModel.confirmDelete() },
                    enabled = !uiState.isDeleting,
                ) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissDeleteDialog() }) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            shape = RoundedCornerShape(LiventDimens.CornerRadiusLarge)
        )
    }
}

@Composable
private fun StatsRow(
    activeCount: Int,
    pastCount: Int,
    draftCount: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(LiventDimens.PaddingSmall),
    ) {
        StatCard(label = "Activos", value = activeCount.toString(), modifier = Modifier.weight(1f))
        StatCard(label = "Pasados", value = pastCount.toString(), modifier = Modifier.weight(1f))
        StatCard(label = "Borradores", value = draftCount.toString(), modifier = Modifier.weight(1f))
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(LiventDimens.CornerRadiusMedium)
    ) {
        Column(modifier = Modifier.padding(LiventDimens.PaddingMedium)) {
            Text(value, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun EventListItem(
    event: Event,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onMarkPast: () -> Unit,
    onUnpublish: () -> Unit,
    onBoost: () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LiventDimens.CornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LiventDimens.PaddingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val imageUrl = event.posterUrl
            if (!imageUrl.isNullOrBlank()) {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp).clip(RoundedCornerShape(LiventDimens.CornerRadiusSmall)),
                    contentScale = ContentScale.Crop,
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(LiventDimens.CornerRadiusSmall)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("🎫", style = MaterialTheme.typography.headlineSmall)
                }
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = LiventDimens.PaddingMedium),
            ) {
                Text(event.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    text = formatEventDate(event.startsAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp),
                ) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(statusLabel(event.status)) },
                    )
                    if (event.status == EventStatus.ACTIVE) {
                        if (event.isFeatured) {
                            AssistChip(
                                onClick = {},
                                enabled = false,
                                label = { Text("Destacado") },
                            )
                        } else {
                            AssistChip(
                                onClick = onBoost,
                                label = { Text("Destacar (2,99 €)") },
                            )
                        }
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Default.Edit, contentDescription = "Editar", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Box {
                IconButton(onClick = { menuExpanded = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Más acciones", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                DropdownMenu(
                    expanded = menuExpanded,
                    onDismissRequest = { menuExpanded = false },
                ) {
                    if (event.status == EventStatus.ACTIVE) {
                        DropdownMenuItem(
                            text = { Text("Marcar como pasado") },
                            onClick = {
                                menuExpanded = false
                                onMarkPast()
                            },
                        )
                        DropdownMenuItem(
                            text = { Text("Despublicar (borrador)") },
                            onClick = {
                                menuExpanded = false
                                onUnpublish()
                            },
                        )
                    }
                    DropdownMenuItem(
                        text = { Text("Eliminar", color = MaterialTheme.colorScheme.error) },
                        onClick = {
                            menuExpanded = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                    )
                }
            }
        }
    }
}

private fun statusLabel(status: EventStatus): String = when (status) {
    EventStatus.ACTIVE -> "Activo"
    EventStatus.PAST -> "Pasado"
    EventStatus.DRAFT -> "Borrador"
}

private fun formatEventDate(iso: String): String = runCatching {
    val instant = Instant.parse(iso)
    val formatter = DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm", Locale.forLanguageTag("es-ES"))
    formatter.format(instant.atZone(ZoneId.systemDefault()))
}.getOrElse { iso }
