package com.example.livent.presentation.screens.explore

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.livent.domain.model.Event
import com.example.livent.presentation.components.LiventFilterChipRow
import com.example.livent.presentation.components.LiventSearchField
import com.example.livent.presentation.explore.ExploreUiState
import com.example.livent.presentation.theme.LiventDimens
import com.example.livent.presentation.theme.LiventPrimary
import com.example.livent.presentation.util.formatEventDate
import kotlinx.coroutines.launch

@Composable
fun ExploreHomeScreen(
    uiState: ExploreUiState,
    onEventClick: (String) -> Unit,
    onFavoriteClick: (String) -> Unit,
    onRetry: () -> Unit,
    onFavoriteErrorShown: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Todos") }
    val categories = listOf("Todos", "Música", "Deporte", "Tech", "Arte")

    // Filter events locally
    val filteredFeatured = uiState.featuredEvents.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
    }
    val filteredUpcoming = uiState.upcomingEvents.filter {
        it.title.contains(searchQuery, ignoreCase = true) || it.artist.contains(searchQuery, ignoreCase = true)
    }

    when {
        uiState.isLoading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        uiState.errorMessage != null -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(LiventDimens.PaddingLarge),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(uiState.errorMessage, color = MaterialTheme.colorScheme.error)
                TextButton(onClick = onRetry) { Text("Reintentar", color = MaterialTheme.colorScheme.primary) }
            }
        }
        else -> {
            val snackbarHostState = remember { SnackbarHostState() }
            LaunchedEffect(uiState.favoriteErrorMessage) {
                uiState.favoriteErrorMessage?.let { message ->
                    snackbarHostState.showSnackbar(message)
                    onFavoriteErrorShown()
                }
            }
            Box(modifier = modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = LiventDimens.PaddingLarge),
                    verticalArrangement = Arrangement.spacedBy(LiventDimens.PaddingMedium),
                ) {
                    item {
                        ExploreHeader()
                    }
                    
                    item {
                        LiventSearchField(
                            query = searchQuery,
                            onQueryChange = { searchQuery = it },
                            modifier = Modifier.padding(horizontal = LiventDimens.PaddingMedium)
                        )
                    }
                    
                    item {
                        LiventFilterChipRow(
                            categories = categories,
                            selectedCategory = selectedCategory,
                            onCategorySelected = { selectedCategory = it }
                        )
                    }

                    if (filteredFeatured.isNotEmpty()) {
                        item {
                            Text(
                                text = "Eventos destacados",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = LiventDimens.PaddingMedium, vertical = LiventDimens.PaddingSmall),
                            )
                            LazyRow(
                                contentPadding = PaddingValues(horizontal = LiventDimens.PaddingMedium),
                                horizontalArrangement = Arrangement.spacedBy(LiventDimens.PaddingMedium),
                            ) {
                                items(filteredFeatured, key = { it.id }) { event ->
                                    FeaturedEventCard(
                                        event = event,
                                        isFavorite = event.id in uiState.favoriteIds,
                                        onClick = { onEventClick(event.id) },
                                        onFavoriteClick = { onFavoriteClick(event.id) },
                                    )
                                }
                            }
                        }
                    }
                    
                    if (filteredUpcoming.isNotEmpty()) {
                        item {
                            Text(
                                text = "Próximos eventos",
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(horizontal = LiventDimens.PaddingMedium, vertical = LiventDimens.PaddingSmall),
                            )
                        }
                        item {
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(gridHeight(filteredUpcoming.size)),
                                contentPadding = PaddingValues(horizontal = LiventDimens.PaddingMedium),
                                horizontalArrangement = Arrangement.spacedBy(LiventDimens.PaddingMedium),
                                verticalArrangement = Arrangement.spacedBy(LiventDimens.PaddingMedium),
                                userScrollEnabled = false,
                            ) {
                                items(filteredUpcoming, key = { it.id }) { event ->
                                    UpcomingEventCard(
                                        event = event,
                                        isFavorite = event.id in uiState.favoriteIds,
                                        onClick = { onEventClick(event.id) },
                                        onFavoriteClick = { onFavoriteClick(event.id) },
                                    )
                                }
                            }
                        }
                    } else if (filteredFeatured.isEmpty() && searchQuery.isNotEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                                Text("No se encontraron eventos.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter),
                )
            }
        }
    }
}

@Composable
private fun ExploreHeader() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LiventDimens.PaddingMedium, vertical = LiventDimens.PaddingSmall),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = "Ubicación",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 8.dp)
        )
        Column {
            Text(
                text = "Ubicación actual",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Madrid, España",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

private fun gridHeight(itemCount: Int): androidx.compose.ui.unit.Dp {
    val rows = (itemCount + 1) / 2
    return (rows * 240).dp
}

@Composable
private fun FeaturedEventCard(
    event: Event,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.width(280.dp),
        shape = RoundedCornerShape(LiventDimens.CornerRadiusLarge),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            EventPoster(
                posterUrl = event.posterUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .clickable(onClick = onClick),
            )
            Column(modifier = Modifier.padding(LiventDimens.PaddingMedium)) {
                RowWithFavorite(
                    title = event.title,
                    isFavorite = isFavorite,
                    onFavoriteClick = onFavoriteClick,
                    onTitleClick = onClick,
                )
                Text(
                    text = event.artist,
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClick = onClick),
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatEventDate(event.startsAt),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onClick),
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = onClick,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Ver evento", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun UpcomingEventCard(
    event: Event,
    isFavorite: Boolean,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(LiventDimens.CornerRadiusMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            EventPoster(
                posterUrl = event.posterUrl,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4f / 3f)
                    .clickable(onClick = onClick),
            )
            Column(modifier = Modifier.padding(LiventDimens.PaddingSmall)) {
                RowWithFavorite(
                    title = event.title,
                    isFavorite = isFavorite,
                    onFavoriteClick = onFavoriteClick,
                    onTitleClick = onClick,
                    compact = true,
                )
                Text(
                    text = formatEventDate(event.startsAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.clickable(onClick = onClick),
                )
            }
        }
    }
}

@Composable
private fun RowWithFavorite(
    title: String,
    isFavorite: Boolean,
    onFavoriteClick: () -> Unit,
    onTitleClick: () -> Unit,
    compact: Boolean = false,
) {
    val scale = remember { Animatable(1f) }
    val coroutineScope = rememberCoroutineScope()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = if (compact) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onTitleClick),
        )
        IconButton(
            onClick = {
                coroutineScope.launch {
                    scale.animateTo(1.2f, animationSpec = tween(100))
                    scale.animateTo(1f, animationSpec = tween(100))
                }
                onFavoriteClick()
            },
            modifier = Modifier.scale(scale.value)
        ) {
            Icon(
                imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                contentDescription = if (isFavorite) "Quitar de favoritos" else "Añadir a favoritos",
                tint = if (isFavorite) LiventPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun EventPoster(
    posterUrl: String?,
    modifier: Modifier = Modifier,
) {
    if (posterUrl != null) {
        AsyncImage(
            model = posterUrl,
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = modifier.background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "Sin cartel",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
