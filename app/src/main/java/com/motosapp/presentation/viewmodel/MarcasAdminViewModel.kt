package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.Marca
import com.motosapp.domain.repository.MarcaRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MarcasAdminViewModel @Inject constructor(
    private val repository: MarcaRepository
) : ViewModel() {

    private val _marcas = MutableStateFlow<List<Marca>>(emptyList())
    val marcas: StateFlow<List<Marca>> = _marcas.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val res = repository.getMarcas()
            if (res.isSuccess) {
                _marcas.value = res.getOrNull()?.first ?: emptyList()
            }
        }
    }

    fun save(id: Int?, payload: com.motosapp.domain.model.MarcaPayload) {
        viewModelScope.launch {
            if (id == null) {
                repository.createMarca(payload)
            } else {
                repository.updateMarca(id, payload)
            }
            load()
        }
    }

    fun delete(id: Int) {
        viewModelScope.launch {
            repository.deleteMarca(id)
            load()
        }
    }
}
