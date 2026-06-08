// presentation/viewmodel/DashboardViewModel.kt
package com.motosapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motosapp.domain.model.Motocicleta
import com.motosapp.data.remote.api.HealthApi
import com.motosapp.domain.repository.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DashboardStats(
    val totalActiveMotocicletas:   Int    = 0,
    val outOfStockMotocicletas:    Int    = 0,
    val totalStock:            Int    = 0,
    val avgPrice:              Double = 0.0,
    val activeMarcas:      Int    = 0,
    val totalMarcas:       Int    = 0,
    val totalOrders:           Int    = 0,
    val totalRevenue:          Double = 0.0,
    val pendingOrders:         Int    = 0,
    val ordersByStatus:        Map<String, Int> = emptyMap(),
    val activeUsers:           Int    = 0,
    val totalUsers:            Int    = 0,
    val staffUsers:            Int    = 0,
    val lowStockMotocicletas:      List<Motocicleta> = emptyList(),
)

sealed interface DashboardUiState {
    data object Loading                          : DashboardUiState
    data class  Success(val stats: DashboardStats) : DashboardUiState
    data class  Error(val message: String)       : DashboardUiState
}

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val motocicletaRepository:  MotocicletaRepository,
    private val marcaRepository: MarcaRepository,
    private val orderRepository:    OrderRepository,
    private val userRepository:     UserRepository,
    private val healthApi:          HealthApi,
) : ViewModel() {

    private val _state = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val state: StateFlow<DashboardUiState> = _state.asStateFlow()

    private val _lastUpdated = MutableStateFlow<Long>(0L)
    val lastUpdated: StateFlow<Long> = _lastUpdated.asStateFlow()

    // null = checking, true = online, false = offline
    private val _apiOnline = MutableStateFlow<Boolean?>(null)
    val apiOnline: StateFlow<Boolean?> = _apiOnline.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _state.value  = DashboardUiState.Loading
            _apiOnline.value = null // resetting while checking

            // Check API health in parallel with data load
            val healthDeferred = async {
                try { healthApi.checkHealth().isSuccessful } catch (e: Exception) { false }
            }

            try {
                // Todas las llamadas en paralelo — si alguna falla, usamos null/vacío
                val motocicletasDeferred = async { motocicletaRepository.getMotocicletas().getOrNull() }
                val marcasDeferred       = async { marcaRepository.getMarcas().getOrNull() }
                val userStatsDeferred    = async { userRepository.getStats().getOrNull() }

                val motocicletas = motocicletasDeferred.await()?.first ?: emptyList()
                val marcas       = marcasDeferred.await()?.first ?: emptyList()
                val userStats    = userStatsDeferred.await()

                // Calcular estadísticas de motocicletas localmente
                val activeMotos    = motocicletas.filter { it.is_active }
                val outOfStock     = motocicletas.count { it.stock == 0 }
                val totalStock     = motocicletas.sumOf { it.stock }
                val avgPrice       = if (motocicletas.isEmpty()) 0.0
                                     else motocicletas.mapNotNull { it.precio.toDoubleOrNull() }.average()
                val lowStock       = motocicletas.filter { it.stock in 1..4 }.take(5)

                val stats = DashboardStats(
                    totalActiveMotocicletas  = activeMotos.size,
                    outOfStockMotocicletas   = outOfStock,
                    totalStock               = totalStock,
                    avgPrice                 = if (avgPrice.isNaN()) 0.0 else avgPrice,
                    activeMarcas             = marcas.count { it.activo },
                    totalMarcas              = marcas.size,
                    totalOrders              = 0,
                    totalRevenue             = 0.0,
                    pendingOrders            = 0,
                    ordersByStatus           = emptyMap(),
                    activeUsers              = (userStats?.get("active") ?: 0),
                    totalUsers               = (userStats?.get("total")  ?: 0),
                    staffUsers               = (userStats?.get("staff")  ?: 0),
                    lowStockMotocicletas     = lowStock,
                )

                _apiOnline.value   = healthDeferred.await()
                _state.value       = DashboardUiState.Success(stats)
                _lastUpdated.value = System.currentTimeMillis()

            } catch (e: Exception) {
                _apiOnline.value = healthDeferred.runCatching { await() }.getOrDefault(false)
                _state.value = DashboardUiState.Error(e.message ?: "Error al cargar el dashboard")
            }
        }
    }
}