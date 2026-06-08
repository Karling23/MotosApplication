// presentation/ui/admin/dashboard/DashboardScreen.kt
package com.motosapp.presentation.ui.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.motosapp.presentation.components.LoadingScreen
import com.motosapp.presentation.components.orderStatusColor
import com.motosapp.presentation.viewmodel.DashboardUiState
import com.motosapp.presentation.viewmodel.DashboardViewModel
import com.motosapp.theme.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    viewModel:  DashboardViewModel = hiltViewModel(),
) {
    val state       by viewModel.state.collectAsState()
    val lastUpdated by viewModel.lastUpdated.collectAsState()
    val apiOnline   by viewModel.apiOnline.collectAsState()

    when (val s = state) {
        is DashboardUiState.Loading ->
            LoadingScreen("Cargando dashboard...")
        is DashboardUiState.Error   -> {
            Box(Modifier.fillMaxSize(), Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("⚠️ ${s.message}", color = Error)
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = viewModel::load,
                        colors = ButtonDefaults.buttonColors(containerColor = Accent)) {
                        Text("Reintentar", color = AccentOnDark)
                    }
                }
            }
        }
        is DashboardUiState.Success ->
            DashboardContent(
                stats       = s.stats,
                lastUpdated = lastUpdated,
                apiOnline   = apiOnline,
                onNavigate  = onNavigate,
                onRefresh   = viewModel::load,
            )
    }
}

@Composable
private fun DashboardContent(
    stats:       com.motosapp.presentation.viewmodel.DashboardStats,
    lastUpdated: Long,
    apiOnline:   Boolean?,
    onNavigate:  (String) -> Unit,
    onRefresh:   () -> Unit,
) {
    val timeFmt = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    val timeStr = if (lastUpdated > 0) timeFmt.format(Date(lastUpdated)) else "—"

    LazyColumn(
        modifier       = Modifier.fillMaxSize().background(Background),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header
        item {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        text       = "Dashboard",
                        style      = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                    )
                    Text(
                        text  = "Actualizado: $timeStr",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextFaint,
                    )
                }
                IconButton(onClick = onRefresh) {
                    Icon(Icons.Default.Refresh, contentDescription = "Actualizar", tint = Accent)
                }
            }
        }

        // ── KPIs — fila 1 ─────────────────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title    = "Motocicletas activas",
                    value    = stats.totalActiveMotocicletas.toString(),
                    subtitle = if (stats.outOfStockMotocicletas > 0)
                        "${stats.outOfStockMotocicletas} sin stock" else null,
                    icon     = Icons.Default.Inventory,
                    color    = Accent,
                    hasAlert = stats.outOfStockMotocicletas > 0,
                    onClick  = { onNavigate("admin/motocicletas") },
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title   = "Categorías activas",
                    value   = stats.activeMarcas.toString(),
                    subtitle = "${stats.totalMarcas} total",
                    icon    = Icons.Default.List,
                    color   = Info,
                    onClick = { onNavigate("admin/marcas") },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── KPIs — fila 2 ─────────────────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title    = "Pedidos totales",
                    value    = stats.totalOrders.toString(),
                    subtitle = if (stats.pendingOrders > 0)
                        "${stats.pendingOrders} pendientes" else null,
                    icon     = Icons.Default.ShoppingBag,
                    color    = Success,
                    hasAlert = stats.pendingOrders > 0,
                    onClick  = { onNavigate("admin/orders") },
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title    = "Usuarios activos",
                    value    = stats.activeUsers.toString(),
                    subtitle = "${stats.totalUsers} registrados",
                    icon     = Icons.Default.People,
                    color    = Warning,
                    onClick  = { onNavigate("admin/users") },
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── KPIs financieros ──────────────────────────────────
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                KpiCard(
                    title    = "Facturación total",
                    value    = "$${"%.0f".format(stats.totalRevenue)}",
                    icon     = Icons.Default.TrendingUp,
                    color    = Accent,
                    modifier = Modifier.weight(1f),
                )
                KpiCard(
                    title    = "Precio medio",
                    value    = "$${"%.2f".format(stats.avgPrice)}",
                    subtitle = "por motocicleta",
                    icon     = Icons.Default.Sell,
                    color    = TextSecondary,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        // ── Pedidos por estado — gráfica de barras ─────────────
        if (stats.ordersByStatus.isNotEmpty()) {
            item {
                Surface(
                    color    = Surface,
                    shape    = MaterialTheme.shapes.large,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier              = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment     = Alignment.CenterVertically,
                        ) {
                            Text(
                                text       = "Pedidos por estado",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            TextButton(onClick = { onNavigate("admin/orders") }) {
                                Text("Ver todos", color = Accent,
                                    style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Spacer(Modifier.height(16.dp))

                        val total = stats.totalOrders.coerceAtLeast(1)
                        stats.ordersByStatus.entries.forEach { (statusValue, count) ->
                            val color  = orderStatusColor(statusValue)
                            val pct    = (count.toFloat() / total).coerceIn(0.02f, 1f)

                            Column(modifier = Modifier.padding(bottom = 10.dp)) {
                                Row(
                                    modifier              = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                ) {
                                    Text(
                                        text  = statusValue,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary,
                                    )
                                    Text(
                                        text       = count.toString(),
                                        style      = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.Bold,
                                        color      = color,
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(7.dp)
                                        .background(Surface2, MaterialTheme.shapes.extraSmall),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(pct)
                                            .fillMaxHeight()
                                            .background(color, MaterialTheme.shapes.extraSmall),
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Conexión con la API ──────────────────────────────
        item {
            Surface(
                color    = Surface,
                shape    = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Wifi,
                                contentDescription = null,
                                tint    = when (apiOnline) {
                                    true  -> Success
                                    false -> Error
                                    null  -> TextSecondary
                                },
                                modifier = Modifier.size(18.dp),
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                text       = "Conexión con la API",
                                style      = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                        }
                        TextButton(onClick = onRefresh) {
                            Text("Verificar", color = Accent,
                                style = MaterialTheme.typography.bodySmall)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    Box(
                        modifier         = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        when (apiOnline) {
                            null  -> {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(14.dp),
                                        strokeWidth = 2.dp,
                                        color = TextSecondary,
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Verificando conexión...", color = TextSecondary,
                                        style = MaterialTheme.typography.bodySmall)
                                }
                            }
                            true  -> {
                                Surface(
                                    color = Success.copy(alpha = 0.12f),
                                    shape = MaterialTheme.shapes.medium,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(Icons.Default.CheckCircle, null,
                                            tint = Success, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("API conectada y funcionando",
                                            color = Success,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                            false -> {
                                Surface(
                                    color = Error.copy(alpha = 0.12f),
                                    shape = MaterialTheme.shapes.medium,
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center,
                                    ) {
                                        Icon(Icons.Default.WifiOff, null,
                                            tint = Error, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(6.dp))
                                        Text("No se pudo conectar con la API",
                                            color = Error,
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Medium)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── Acciones rápidas ──────────────────────────────────
        item {
            Surface(
                color    = Surface,
                shape    = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text       = "⚡ Acciones rápidas",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color      = TextPrimary,
                        modifier   = Modifier.padding(bottom = 12.dp),
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(listOf(
                            Triple("+ Categoría", Info,    "admin/marcas"),
                            Triple("+ Motocicleta",  Accent,  "admin/motocicletas"),
                            Triple("Ver pedidos", Success, "admin/orders"),
                            Triple("Usuarios",    Warning, "admin/users"),
                        )) { (label, color, route) ->
                            Surface(
                                onClick  = { onNavigate(route) },
                                color    = color.copy(alpha = 0.1f),
                                shape    = MaterialTheme.shapes.medium,
                            ) {
                                Text(
                                    text       = label,
                                    color      = color,
                                    fontWeight = FontWeight.Bold,
                                    style      = MaterialTheme.typography.bodySmall,
                                    modifier   = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}