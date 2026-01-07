package com.test.retrofittest.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemKey
import coil.compose.AsyncImage

/**
 * Función composable para mostrar el listado de personajes.
 * @param rickMortyListViewModel ViewModel para manejar estados y la data.
 * */
@Composable
fun RickMortyListScreen(rickMortyListViewModel: RickMortyListViewModel = hiltViewModel()) {
    // Recolectamos los estados del ViewModel de forma segura para el ciclo de vida
    val uiState by rickMortyListViewModel.uiState.collectAsStateWithLifecycle()
    val characters = rickMortyListViewModel.charactersState.collectAsLazyPagingItems()

    // Usamos `when` para reaccionar al estado general de la UI
    when (uiState) {
        is CharacterUiState.Loading -> {
            // Muestra un indicador de carga en el centro mientras se obtienen los datos iniciales
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is CharacterUiState.Error -> {
            // Muestra un mensaje de error si algo falló
            val message = (uiState as CharacterUiState.Error).message
            ErrorState(
                message = message,
                onRetry = { rickMortyListViewModel.getCharacters() }
            )
        }
        is CharacterUiState.Success -> {
            // Si la carga fue exitosa, muestra la lista de personajes
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Renderiza cada item de la lista paginada
                items(
                    count = characters.itemCount,
                    key = characters.itemKey { it.id } // Clave única para cada item
                ) { index ->
                    val character = characters[index]
                    if (character != null) {
                        CharacterItem(character = character)
                    }
                }

                // Maneja los estados de carga de la paginación
                characters.loadState.apply {
                    when {
                        // Muestra un spinner si se está refrescando la lista
                        refresh is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier.fillParentMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        // Muestra un spinner al final de la lista cuando se carga la siguiente página
                        append is LoadState.Loading -> {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        // --- ESTA ES LA PARTE NUEVA Y MÁS IMPORTANTE ---
                        // Muestra un error y un botón de reintento si falla la carga de la siguiente página
                        append is LoadState.Error -> {
                            item {
                                ErrorState(
                                    message = "No se pudo cargar más personajes. Revisa tu conexión.",
                                    onRetry = { characters.retry() }, // Llama a la función retry() de Paging
                                    modifier = Modifier.padding(16.dp) // Usa un padding para que no ocupe toda la pantalla
                                )
                            }
                        }
                        // También puedes manejar el error del refresh
                        refresh is LoadState.Error -> {
                            item {
                                ErrorState(
                                    message = "No se pudo actualizar la lista. Revisa tu conexión.",
                                    onRetry = { characters.retry() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CharacterItem(character: CharacterModel) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.medium
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Imagen del personaje
            AsyncImage(
                model = character.image,
                contentDescription = "Imagen de ${character.name}",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop // Asegura que la imagen cubra el espacio
            )

            // Contenedor para el texto debajo de la imagen
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Nombre del personaje
                Text(
                    text = character.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
                // Estado del personaje
                Text(
                    text = "Estado: ${character.status}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

/**
 * Un Composable reutilizable para mostrar un estado de error
 * con un mensaje y un botón para reintentar.
 */
@Composable
fun ErrorState(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text(text = "Reintentar")
            }
        }
    }
}
