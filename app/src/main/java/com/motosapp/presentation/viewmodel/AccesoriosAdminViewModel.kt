package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.Accesorio
import com.motosapp.domain.model.AccesorioPayload
import com.motosapp.domain.model.Marca
import com.motosapp.domain.repository.AccesorioRepository
import com.motosapp.domain.repository.MarcaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class AccesorioStockFilter(val label: String) {
    ALL("Todos"),
    IN_STOCK("Con stock"),
    OUT_OF_STOCK("Sin stock"),
    ACTIVE("Activos"),
    INACTIVE("Inactivos"),
}

data class AccesoriosAdminUiState(
    val accesorios:  List<Accesorio>    = emptyList(),
    val isLoading:   Boolean            = false,
    val error:       String?            = null,
    val total:       Int                = 0,
    val search:      String             = "",
    val stockFilter: AccesorioStockFilter = AccesorioStockFilter.ALL,
)

sealed interface AccesorioFormState {
    data object Idle                       : AccesorioFormState
    data object Saving                     : AccesorioFormState
    data class  Success(val msg: String)   : AccesorioFormState
    data class  Error(val message: String) : AccesorioFormState
}

@HiltViewModel
class AccesoriosAdminViewModel @Inject constructor(
    private val repository:      AccesorioRepository,
    private val marcaRepository: MarcaRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(AccesoriosAdminUiState())
    val state: StateFlow<AccesoriosAdminUiState> = _state.asStateFlow()

    private val _formState = MutableStateFlow<AccesorioFormState>(AccesorioFormState.Idle)
    val formState: StateFlow<AccesorioFormState> = _formState.asStateFlow()

    private val _marcas = MutableStateFlow<List<Marca>>(emptyList())
    val marcas: StateFlow<List<Marca>> = _marcas.asStateFlow()

    val filtered: StateFlow<List<Accesorio>> = _state
        .map { s ->
            s.accesorios
                .filter { p ->
                    s.search.isBlank() || p.displayName.contains(s.search, ignoreCase = true)
                }
                .filter { p ->
                    when (s.stockFilter) {
                        AccesorioStockFilter.ALL          -> true
                        AccesorioStockFilter.IN_STOCK     -> p.stock > 0
                        AccesorioStockFilter.OUT_OF_STOCK -> p.stock == 0
                        AccesorioStockFilter.ACTIVE       -> p.is_active
                        AccesorioStockFilter.INACTIVE     -> !p.is_active
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    init {
        load()
        loadMarcas()
    }

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null) }
            repository.getAccesorios()
                .onSuccess { (accesorios, total) ->
                    _state.update { it.copy(accesorios = accesorios, total = total, isLoading = false) }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }

    private fun loadMarcas() {
        viewModelScope.launch {
            marcaRepository.getMarcas().onSuccess { _marcas.value = it.first }
        }
    }

    fun setSearch(query: String) = _state.update { it.copy(search = query) }
    fun setStockFilter(filter: AccesorioStockFilter) = _state.update { it.copy(stockFilter = filter) }

    fun toggleActive(id: Int, isActive: Boolean) {
        _state.update { s ->
            s.copy(accesorios = s.accesorios.map {
                if (it.id == id) it.copy(is_active = isActive) else it
            })
        }
        // TODO: Call API
    }

    fun createAccesorio(payload: AccesorioPayload) {
        viewModelScope.launch {
            _formState.value = AccesorioFormState.Saving
            repository.createAccesorio(payload)
                .onSuccess {
                    _formState.value = AccesorioFormState.Success("Accesorio creado")
                    load()
                }
                .onFailure { e ->
                    _formState.value = AccesorioFormState.Error(e.message ?: "Error al crear")
                }
        }
    }

    fun updateAccesorio(id: Int, payload: AccesorioPayload) {
        viewModelScope.launch {
            _formState.value = AccesorioFormState.Saving
            repository.updateAccesorio(id, payload)
                .onSuccess {
                    _formState.value = AccesorioFormState.Success("Accesorio actualizado")
                    load()
                }
                .onFailure { e ->
                    _formState.value = AccesorioFormState.Error(e.message ?: "Error al actualizar")
                }
        }
    }

    fun restock(id: Int, quantity: Int, onResult: (String) -> Unit) {
        val current = _state.value.accesorios.find { it.id == id } ?: return
        val payload = AccesorioPayload(
            nombre             = current.nombre,
            marca              = current.marca,
            categoriaAccesorio = current.categoria_accesorio,
            precio             = current.precio,
            stock              = current.stock + quantity,
            isActive           = current.is_active,
            descripcion        = current.descripcion,
        )
        viewModelScope.launch {
            repository.updateAccesorio(id, payload)
                .onSuccess { onResult("Stock actualizado: ${it.stock} unidades") }
                .onFailure { e -> onResult("Error: ${e.message}") }
        }
    }

    fun deleteAccesorio(id: Int) {
        viewModelScope.launch {
            repository.deleteAccesorio(id)
                .onSuccess { load() }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun resetFormState() {
        _formState.value = AccesorioFormState.Idle
    }
}
