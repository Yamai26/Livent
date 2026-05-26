package com.example.livent.presentation.screens.publisher

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.livent.presentation.publisher.EventEditorViewModel
import com.example.livent.presentation.publisher.PaymentKind
import com.example.livent.presentation.publisher.PaymentViewModel
import com.example.livent.presentation.components.LiventPrimaryButton
import com.example.livent.presentation.components.LiventSecondaryButton
import com.example.livent.presentation.components.LiventTopBar
import com.example.livent.presentation.theme.LiventDimens
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventWizardScreen(
    onBack: () -> Unit,
    onSaved: () -> Unit,
    onSubscribePremium: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: EventEditorViewModel = hiltViewModel(),
    paymentViewModel: PaymentViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paymentState by paymentViewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val isEdit = uiState.eventId != null

    LaunchedEffect(paymentViewModel) {
        paymentViewModel.paymentCompleted.collect { kind ->
            if (kind == PaymentKind.PREMIUM) {
                viewModel.reloadProfileTier()
            }
        }
    }

    LaunchedEffect(paymentState.errorMessage) {
        paymentState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    LaunchedEffect(uiState.saveSuccess) {
        if (uiState.saveSuccess) {
            viewModel.consumeSaveSuccess()
            onSaved()
        }
    }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it) }
    }

    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
    ) { uri: Uri? ->
        uri?.let { viewModel.onPosterPicked(it) }
    }

    FreePlanLimitBottomSheet(
        visible = uiState.showFreePlanSheet,
        onDismiss = { viewModel.dismissFreePlanSheet() },
        onSubscribePremium = onSubscribePremium,
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            LiventTopBar(
                title = if (isEdit) "Editar evento" else "Nuevo evento",
                onNavigateBack = onBack
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
        ) {
            LinearProgressIndicator(
                progress = { uiState.wizardStep / 3f },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
            )
            Text(
                text = "Paso ${uiState.wizardStep} de 3",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
            ) {
                when (uiState.wizardStep) {
                    1 -> WizardStepPoster(
                        previewUri = uiState.posterPreviewUri,
                        existingUrl = uiState.existingPosterUrl,
                        onPickImage = {
                            photoPicker.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                            )
                        },
                    )
                    2 -> WizardStepDetails(
                        title = uiState.title,
                        artist = uiState.artist,
                        description = uiState.description,
                        onTitleChange = viewModel::updateTitle,
                        onArtistChange = viewModel::updateArtist,
                        onDescriptionChange = viewModel::updateDescription,
                    )
                    3 -> WizardStepSchedule(
                        startsAtMillis = uiState.startsAtMillis,
                        location = uiState.location,
                        onLocationChange = viewModel::updateLocation,
                        onDateTimeSelected = viewModel::updateStartsAtMillis,
                    )
                }
            }

            WizardNavigationBar(
                step = uiState.wizardStep,
                isSaving = uiState.isSaving,
                onPrevious = { viewModel.previousStep() },
                onNext = { viewModel.nextStep() },
                onSaveDraft = { viewModel.saveDraft() },
                onPublish = { viewModel.publish() },
            )
        }
    }
}

@Composable
private fun WizardStepPoster(
    previewUri: Uri?,
    existingUrl: String?,
    onPickImage: () -> Unit,
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text("Cartel del evento", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "Elige una imagen (JPEG o PNG). Se subirá a tu carpeta en Storage.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        val model = previewUri ?: existingUrl
        if (model != null) {
            AsyncImage(
                model = model,
                contentDescription = "Vista previa del cartel",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop,
            )
        }
        LiventSecondaryButton(
            onClick = onPickImage,
            modifier = Modifier.fillMaxWidth(),
            text = if (model == null) "Seleccionar imagen" else "Cambiar imagen"
        )
    }
}

@Composable
private fun WizardStepDetails(
    title: String,
    artist: String,
    description: String,
    onTitleChange: (String) -> Unit,
    onArtistChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
) {
    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Información del evento", style = MaterialTheme.typography.titleMedium)
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text("Título *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = artist,
            onValueChange = onArtistChange,
            label = { Text("Artista") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
        OutlinedTextField(
            value = description,
            onValueChange = onDescriptionChange,
            label = { Text("Descripción") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WizardStepSchedule(
    startsAtMillis: Long?,
    location: String,
    onLocationChange: (String) -> Unit,
    onDateTimeSelected: (Long) -> Unit,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var pickedDate by remember(startsAtMillis) {
        mutableStateOf(
            startsAtMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalDate()
            } ?: LocalDate.now().plusDays(7),
        )
    }
    var pickedTime by remember(startsAtMillis) {
        mutableStateOf(
            startsAtMillis?.let {
                Instant.ofEpochMilli(it).atZone(ZoneId.systemDefault()).toLocalTime()
            } ?: LocalTime.of(20, 0),
        )
    }

    LaunchedEffect(startsAtMillis) {
        if (startsAtMillis == null) {
            val default = LocalDateTime.of(pickedDate, pickedTime)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            onDateTimeSelected(default)
        }
    }

    Column(
        modifier = Modifier.padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Fecha, hora y lugar", style = MaterialTheme.typography.titleMedium)
        LiventSecondaryButton(
            onClick = { showDatePicker = true },
            modifier = Modifier.fillMaxWidth(),
            text = "Fecha: ${pickedDate.dayOfMonth}/${pickedDate.monthValue}/${pickedDate.year}"
        )
        LiventSecondaryButton(
            onClick = { showTimePicker = true },
            modifier = Modifier.fillMaxWidth(),
            text = "Hora: ${"%02d".format(pickedTime.hour)}:${"%02d".format(pickedTime.minute)}"
        )
        OutlinedTextField(
            value = location,
            onValueChange = onLocationChange,
            label = { Text("Ubicación *") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = pickedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli(),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        state.selectedDateMillis?.let { millis ->
                            pickedDate = Instant.ofEpochMilli(millis)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate()
                            commitDateTime(pickedDate, pickedTime, onDateTimeSelected)
                        }
                        showDatePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancelar") }
            },
        ) {
            DatePicker(state = state)
        }
    }

    if (showTimePicker) {
        val state = rememberTimePickerState(
            initialHour = pickedTime.hour,
            initialMinute = pickedTime.minute,
            is24Hour = true,
        )
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickedTime = LocalTime.of(state.hour, state.minute)
                        commitDateTime(pickedDate, pickedTime, onDateTimeSelected)
                        showTimePicker = false
                    },
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancelar") }
            },
            text = { TimePicker(state = state) },
        )
    }
}

private fun commitDateTime(
    date: LocalDate,
    time: LocalTime,
    onDateTimeSelected: (Long) -> Unit,
) {
    val millis = LocalDateTime.of(date, time)
        .atZone(ZoneId.systemDefault())
        .toInstant()
        .toEpochMilli()
    onDateTimeSelected(millis)
}

@Composable
private fun WizardNavigationBar(
    step: Int,
    isSaving: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSaveDraft: () -> Unit,
    onPublish: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (step > 1) {
                LiventSecondaryButton(
                    onClick = onPrevious,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    text = "Anterior"
                )
            }
            if (step < 3) {
                LiventPrimaryButton(
                    onClick = onNext,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    text = "Siguiente"
                )
            }
        }
        if (step == 3) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                LiventSecondaryButton(
                    onClick = onSaveDraft,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    text = "Borrador"
                )
                LiventPrimaryButton(
                    onClick = onPublish,
                    modifier = Modifier.weight(1f),
                    enabled = !isSaving,
                    text = "Publicar"
                )
            }
        }
    }
}
