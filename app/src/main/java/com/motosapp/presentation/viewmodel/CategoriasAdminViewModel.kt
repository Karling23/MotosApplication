package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.Categoria
import com.motosapp.domain.repository.CategoriaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriasAdminViewModel @Inject constructor(
    private val repository: CategoriaRepository
) : ViewModel() {

    private val _categorias = MutableStateFlow<List<Categoria>>(emptyList())
    val categorias: StateFlow<List<Categoria>> = _categorias.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val res = repository.getCategorias()
            if (res.isSuccess) {
                _categorias.value = res.getOrNull()?.first ?: emptyList()
            }
        }
    }

    fun save(id: Int?, payload: com.motosapp.domain.model.CategoriaPayload) {
        viewModelScope.launch {
            if (id == null) {
                repository.createCategoria(payload)
            } else {
                repository.updateCategoria(id, payload)
            }
            load()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            repository.deleteCategoria(id)
            load()
        }
    }
}
