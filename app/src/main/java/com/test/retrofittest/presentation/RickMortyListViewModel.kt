package com.test.retrofittest.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.test.retrofittest.data.RickMortyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import java.net.UnknownHostException
import javax.inject.Inject

sealed class CharacterUiState {
    data object Loading : CharacterUiState() // Estado inicial, cargando por primera vez.
    data class Success(val data: PagingData<CharacterModel>) : CharacterUiState() // La carga fue exitosa.
    data class Error(val message: String) : CharacterUiState() // Ocurrió un error.
}

@HiltViewModel
class RickMortyListViewModel @Inject constructor(
    private val rickMortyRepository: RickMortyRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<CharacterUiState>(CharacterUiState.Loading)
    val uiState = _uiState.asStateFlow()

    private val _charactersState = MutableStateFlow<PagingData<CharacterModel>>(PagingData.empty())
    val charactersState = _charactersState.asStateFlow()


    init {
        getCharacters()
    }

    fun getCharacters() {
        viewModelScope.launch {
            _uiState.value = CharacterUiState.Loading

            rickMortyRepository.getAllCharacters()
                .distinctUntilChanged()
                .cachedIn(viewModelScope)
                .catch { exception ->
                    val errorMessage = when (exception) {
                        is UnknownHostException -> "No hay conexión a Internet. Revisa tu conexión y vuelve a intentarlo."
                        else -> exception.message ?: "Ha ocurrido un error inesperado."
                    }
                }
                .collect { pagingData ->
                    _charactersState.value = pagingData
                    _uiState.value = CharacterUiState.Success(pagingData)
                }
        }
    }
}
