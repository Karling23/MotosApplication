package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.*
import com.motosapp.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class CatalogUiState(
    val motocicletas: List<Motocicleta> = emptyList(),
    val cascos: List<Casco> = emptyList(),
    val accesorios: List<Accesorio> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedTab: Int = 0, // 0: Motos, 1: Cascos, 2: Accesorios
    val search: String = ""
) {
    val currentItems: List<ShopItem> get() {
        val list = when (selectedTab) {
            0 -> motocicletas
            1 -> cascos
            2 -> accesorios
            else -> emptyList()
        }
        if (search.isBlank()) return list
        return list.filter { it.displayName.contains(search, ignoreCase = true) }
    }
}

@HiltViewModel
class CatalogViewModel @Inject constructor(
    private val motoRepo: MotocicletaRepository,
    private val cascoRepo: CascoRepository,
    private val accesorioRepo: AccesorioRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CatalogUiState())
    val state: StateFlow<CatalogUiState> = _state.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        _state.update { it.copy(isLoading = true, error = null) }
        viewModelScope.launch {
            val motosRes = motoRepo.getMotocicletas()
            val cascosRes = cascoRepo.getCascos()
            val accRes = accesorioRepo.getAccesorios()
            
            _state.update { 
                it.copy(
                    motocicletas = motosRes.getOrNull()?.first ?: emptyList(),
                    cascos = cascosRes.getOrNull()?.first ?: emptyList(),
                    accesorios = accRes.getOrNull()?.first ?: emptyList(),
                    isLoading = false
                )
            }
        }
    }

    fun setTab(index: Int) {
        _state.update { it.copy(selectedTab = index) }
    }

    fun setSearch(query: String) {
        _state.update { it.copy(search = query) }
    }

    fun refresh() = loadData()
}
