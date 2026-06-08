package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.Casco
import com.motosapp.domain.model.CascoPayload
import com.motosapp.domain.model.Marca
import com.motosapp.domain.repository.CascoRepository
import com.motosapp.domain.repository.MarcaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class CascoStockFilter(val label: String) {
    ALL("Todos"),
    IN_STOCK("Con stock"),
    OUT_OF_STOCK("Sin stock"),
    ACTIVE("Activos"),
    INACTIVE("Inactivos"),
}

data class CascosAdminUiState(
    val cascos:      List<Casco>        = emptyList(),
    val isLoading:   Boolean            = false,
    val error:       String?            = null,
    val total:       Int                = 0,
    val search:      String             = "",
    val stockFilter: CascoStockFilter   = CascoStockFilter.ALL,
)

sealed interface CascoFormState {
    data object Idle                       : CascoFormState
    data object Saving                     : CascoFormState
    data class  Success(val msg: String)   : CascoFormState
    data class  Error(val message: String) : CascoFormState
}

@HiltViewModel
class CascosAdminViewModel @Inject constructor(
    private val repository:      CascoRepository,
    private val marcaRepository: MarcaRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(CascosAdminUiState())
    val state: StateFlow<CascosAdminUiState> = _state.asStateFlow()

    private val _formState = MutableStateFlow<CascoFormState>(CascoFormState.Idle)
    val formState: StateFlow<CascoFormState> = _formState.asStateFlow()

    private val _marcas = MutableStateFlow<List<Marca>>(emptyList())
    val marcas: StateFlow<List<Marca>> = _marcas.asStateFlow()

    val filtered: StateFlow<List<Casco>> = _state
        .map { s ->
            s.cascos
                .filter { p ->
                    s.search.isBlank() || p.displayName.contains(s.search, ignoreCase = true)
                }
                .filter { p ->
                    when (s.stockFilter) {
                        CascoStockFilter.ALL          -> true
                        CascoStockFilter.IN_STOCK     -> p.stock > 0
                        CascoStockFilter.OUT_OF_STOCK -> p.stock == 0
                        CascoStockFilter.ACTIVE       -> p.is_active
                        CascoStockFilter.INACTIVE     -> !p.is_active
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
            repository.getCascos()
                .onSuccess { (cascos, total) ->
                    _state.update { it.copy(cascos = cascos, total = total, isLoading = false) }
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
    fun setStockFilter(filter: CascoStockFilter) = _state.update { it.copy(stockFilter = filter) }

    fun toggleActive(id: Int, isActive: Boolean) {
        _state.update { s ->
            s.copy(cascos = s.cascos.map {
                if (it.id == id) it.copy(is_active = isActive) else it
            })
        }
        // TODO: Call API
    }

    fun createCasco(payload: CascoPayload) {
        viewModelScope.launch {
            _formState.value = CascoFormState.Saving
            repository.createCasco(payload)
                .onSuccess {
                    _formState.value = CascoFormState.Success("Casco creado")
                    load()
                }
                .onFailure { e ->
                    _formState.value = CascoFormState.Error(e.message ?: "Error al crear")
                }
        }
    }

    fun updateCasco(id: Int, payload: CascoPayload) {
        viewModelScope.launch {
            _formState.value = CascoFormState.Saving
            repository.updateCasco(id, payload)
                .onSuccess {
                    _formState.value = CascoFormState.Success("Casco actualizado")
                    load()
                }
                .onFailure { e ->
                    _formState.value = CascoFormState.Error(e.message ?: "Error al actualizar")
                }
        }
    }

    fun restock(id: Int, quantity: Int, onResult: (String) -> Unit) {
        val current = _state.value.cascos.find { it.id == id } ?: return
        val payload = CascoPayload(
            marca         = current.marca,
            modelo        = current.modelo,
            talla         = current.talla,
            color         = current.color,
            certificacion = current.certificacion,
            precio        = current.precio,
            stock         = current.stock + quantity,
            isActive      = current.is_active,
            descripcion   = current.descripcion,
        )
        viewModelScope.launch {
            repository.updateCasco(id, payload)
                .onSuccess { onResult("Stock actualizado: ${it.stock} unidades") }
                .onFailure { e -> onResult("Error: ${e.message}") }
        }
    }

    fun deleteCasco(id: Int) {
        viewModelScope.launch {
            repository.deleteCasco(id)
                .onSuccess { load() }
                .onFailure { e -> _state.update { it.copy(error = e.message) } }
        }
    }

    fun resetFormState() {
        _formState.value = CascoFormState.Idle
    }
}
