// presentation/viewmodel/MotocicletasAdminViewModel.kt
package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.Categoria
import com.motosapp.domain.model.Marca
import com.motosapp.domain.model.Motocicleta
import com.motosapp.domain.model.MotocicletaPayload
import com.motosapp.domain.repository.CategoriaRepository
import com.motosapp.domain.repository.MarcaRepository
import com.motosapp.domain.repository.MotocicletaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class MotocicletaStockFilter(val label: String) {
    ALL("Todos"),
    IN_STOCK("Con stock"),
    OUT_OF_STOCK("Sin stock"),
    ACTIVE("Activos"),
    INACTIVE("Inactivos"),
}

data class MotocicletasAdminUiState(
    val motocicletas:    List<Motocicleta>      = emptyList(),
    val isLoading:   Boolean            = false,
    val isPaginating: Boolean           = false,
    val error:       String?            = null,
    val total:       Int                = 0,
    val search:      String             = "",
    val stockFilter: MotocicletaStockFilter = MotocicletaStockFilter.ALL,
    val page:        Int                = 1,
    val hasMore:     Boolean            = false
)

sealed interface MotocicletaFormState {
    data object Idle                       : MotocicletaFormState
    data object Saving                     : MotocicletaFormState
    data class  Success(val msg: String)   : MotocicletaFormState
    data class  Error(val message: String) : MotocicletaFormState
}

@HiltViewModel
class MotocicletasAdminViewModel @Inject constructor(
    private val repository:          MotocicletaRepository,
    private val marcaRepository:     MarcaRepository,
    private val categoriaRepository: CategoriaRepository,
) : ViewModel() {

    // ── UI state ──────────────────────────────────────────────────
    private val _state = MutableStateFlow(MotocicletasAdminUiState())
    val state: StateFlow<MotocicletasAdminUiState> = _state.asStateFlow()

    private val _formState = MutableStateFlow<MotocicletaFormState>(MotocicletaFormState.Idle)
    val formState: StateFlow<MotocicletaFormState> = _formState.asStateFlow()

    // ── Marcas y Categorías ───────────────────────────────────────
    private val _marcas = MutableStateFlow<List<Marca>>(emptyList())
    val marcas: StateFlow<List<Marca>> = _marcas.asStateFlow()

    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias: StateFlow<List<Categoria>> = _categorias.asStateFlow()

    // ── Filtrado local (solo stock) ─────
    val filtered: StateFlow<List<Motocicleta>> = _state
        .map { s ->
            s.motocicletas
                .filter { p ->
                    when (s.stockFilter) {
                        MotocicletaStockFilter.ALL          -> true
                        MotocicletaStockFilter.IN_STOCK     -> p.stock > 0
                        MotocicletaStockFilter.OUT_OF_STOCK -> p.stock == 0
                        MotocicletaStockFilter.ACTIVE       -> p.is_active
                        MotocicletaStockFilter.INACTIVE     -> !p.is_active
                    }
                }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private var searchJob: Job? = null

    init {
        load()
        loadMarcas()
        loadCategorias()
    }

    // ── Carga de datos ────────────────────────────────────────────

    fun load() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, error = null, page = 1) }
            val currentSearch = _state.value.search.ifBlank { null }
            repository.getMotocicletas(search = currentSearch, page = 1)
                .onSuccess { (motocicletas, total) ->
                    _state.update { 
                        it.copy(
                            motocicletas = motocicletas, 
                            total = total, 
                            isLoading = false,
                            hasMore = motocicletas.size < total
                        ) 
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isLoading = false, error = e.message) }
                }
        }
    }
    
    fun loadMore() {
        val s = _state.value
        if (s.isLoading || s.isPaginating || !s.hasMore) return
        
        viewModelScope.launch {
            _state.update { it.copy(isPaginating = true, error = null) }
            val nextPage = s.page + 1
            val currentSearch = s.search.ifBlank { null }
            
            repository.getMotocicletas(search = currentSearch, page = nextPage)
                .onSuccess { (newItems, total) ->
                    _state.update { 
                        val updatedList = it.motocicletas + newItems
                        it.copy(
                            motocicletas = updatedList,
                            total = total,
                            page = nextPage,
                            isPaginating = false,
                            hasMore = updatedList.size < total
                        )
                    }
                }
                .onFailure { e ->
                    _state.update { it.copy(isPaginating = false, error = e.message) }
                }
        }
    }

    private fun loadMarcas() {
        viewModelScope.launch {
            marcaRepository.getMarcas(page = 1)
                .onSuccess { _marcas.value = it.first }
        }
    }

    private fun loadCategorias() {
        viewModelScope.launch {
            categoriaRepository.getCategorias(page = 1)
                .onSuccess { _categorias.value = it.first }
        }
    }

    // ── Filtros ───────────────────────────────────────────────────

    fun setSearch(query: String) {
        _state.update { it.copy(search = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(500)
            load()
        }
    }
    
    fun setStockFilter(filter: MotocicletaStockFilter) = _state.update { it.copy(stockFilter = filter) }

    // ── Toggle activo (optimista) ─────────────────────────────────

    fun toggleActive(id: Int, isActive: Boolean) {
        _state.update { s ->
            s.copy(motocicletas = s.motocicletas.map {
                if (it.id == id) it.copy(is_active = isActive) else it
            })
        }
    }

    // ── CRUD ──────────────────────────────────────────────────────

    fun createMotocicleta(payload: MotocicletaPayload) {
        viewModelScope.launch {
            _formState.value = MotocicletaFormState.Saving
            repository.createMotocicleta(payload)
                .onSuccess {
                    _formState.value = MotocicletaFormState.Success("Motocicleta creada")
                    load() // Recargar lista
                }
                .onFailure { e ->
                    _formState.value = MotocicletaFormState.Error(
                        e.message ?: "Error al crear la motocicleta"
                    )
                }
        }
    }

    fun updateMotocicleta(id: Int, payload: MotocicletaPayload) {
        viewModelScope.launch {
            _formState.value = MotocicletaFormState.Saving
            repository.updateMotocicleta(id, payload)
                .onSuccess {
                    _formState.value = MotocicletaFormState.Success("Motocicleta actualizada")
                    load()
                }
                .onFailure { e ->
                    _formState.value = MotocicletaFormState.Error(
                        e.message ?: "Error al actualizar la motocicleta"
                    )
                }
        }
    }

    fun restock(id: Int, quantity: Int, onResult: (String) -> Unit) {
        val current = _state.value.motocicletas.find { it.id == id } ?: return
        val payload = MotocicletaPayload(
            marca      = current.marca,
            categoria  = current.categoria,
            modelo     = current.modelo,
            anio       = current.anio,
            cilindrada = current.cilindrada,
            color      = current.color,
            precio     = current.precio,
            stock      = current.stock + quantity,
            isActive   = current.is_active,
            descripcion = current.descripcion,
        )
        viewModelScope.launch {
            repository.updateMotocicleta(id, payload)
                .onSuccess { onResult("Stock actualizado: ${it.stock} unidades") }
                .onFailure { e -> onResult("Error: ${e.message}") }
        }
    }

    fun deleteMotocicleta(id: Int) {
        viewModelScope.launch {
            repository.deleteMotocicleta(id)
                .onSuccess { load() }
                .onFailure { e ->
                    _state.update { it.copy(error = e.message) }
                }
        }
    }

    fun resetFormState() {
        _formState.value = MotocicletaFormState.Idle
    }
}