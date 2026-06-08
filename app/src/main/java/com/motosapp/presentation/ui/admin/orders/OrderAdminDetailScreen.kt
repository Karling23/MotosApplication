// presentation/ui/admin/orders/OrderAdminDetailScreen.kt
package com.motosapp.presentation.ui.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.motosapp.domain.model.Order
import com.motosapp.presentation.components.LoadingScreen
import com.motosapp.presentation.components.ErrorScreen
import com.motosapp.presentation.viewmodel.OrderDetailUiState
import com.motosapp.presentation.viewmodel.OrderDetailViewModel
import com.motosapp.theme.*
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderAdminDetailScreen(
    orderId:    Int,
    onBack:     () -> Unit,
    onStatusChange: (Int, String) -> Unit,
    viewModel:  OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(orderId) { viewModel.load(orderId) }

    when (val s = state) {
        is OrderDetailUiState.Loading ->
            LoadingScreen("Cargando pedido #$orderId...")
        is OrderDetailUiState.Error   ->
            ErrorScreen(s.message, onRetry = { viewModel.load(orderId) })
        is OrderDetailUiState.Success ->
            AdminOrderDetailContent(
                order         = s.order,
                onBack        = onBack,
                onStatusChange = { newStatus ->
                    onStatusChange(orderId, newStatus)
                    viewModel.load(orderId)
                },
            )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AdminOrderDetailContent(
    order:         Order,
    onBack:        () -> Unit,
    onStatusChange:(String) -> Unit,
) {
    val dateStr = "Envío: ${order.direccion_envio}"
    val updatedStr = "N/A"

    val taxAmount = order.total - order.total / 1.15
    val subtotal  = order.total - taxAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            "Pedido #${order.id}",
                            style      = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color      = TextPrimary,
                        )
                        Text(
                            "Cliente #${order.usuario}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = TextPrimary)
                    }
                },
                actions = {
                    // Selector de estado en la TopAppBar
                    StatusDropdown(
                        current  = order.estado,
                        onChange = onStatusChange,
                        modifier = Modifier.padding(end = 12.dp),
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
            )
        },
        containerColor = Background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {

            // ── Info general ──────────────────────────────────
            Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Información del pedido")
                    Spacer(Modifier.height(12.dp))
                    InfoGrid(listOf(
                        "Cliente"      to "${order.usuario}",
                        "Fecha"        to dateStr,
                        "Actualizado"  to updatedStr,
                        "Total ítems"  to "N/A",
                    ))
                }
            }

            // ── Ítems ──────────────────────────────────────────
            Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Productos")
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "Detalles de productos no disponibles.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }

            // ── Totales ────────────────────────────────────────
            Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Resumen financiero")
                    Spacer(Modifier.height(12.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        FinancialRow("Subtotal (sin IVA)", subtotal, false)
                        FinancialRow("IVA (15%)",          taxAmount, false)
                        HorizontalDivider(color = Border, thickness = 0.5.dp)
                        FinancialRow("Total",              order.total, true)
                    }
                }
            }

            // ── Cambio rápido de estado ────────────────────────
            Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    SectionLabel("Cambiar estado")
                    Spacer(Modifier.height(12.dp))
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val statusList = listOf("PENDIENTE", "CONFIRMADO", "ENVIADO", "ENTREGADO", "CANCELADO")
                        statusList
                            .filter { it != order.estado }
                            .forEach { status ->
                                Surface(
                                    onClick   = { onStatusChange(status) },
                                    shape     = MaterialTheme.shapes.small,
                                    color     = com.motosapp.presentation.components
                                        .orderStatusColor(status).copy(alpha = 0.1f),
                                    modifier  = Modifier.weight(1f),
                                ) {
                                    Text(
                                        text     = status,
                                        color    = com.motosapp.presentation.components
                                            .orderStatusColor(status),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(8.dp),
                                    )
                                }
                            }
                    }
                }
            }
        }
    }
}

// ── Sub-composables ───────────────────────────────────────────

@Composable
private fun SectionLabel(text: String) {
    Text(
        text          = text,
        style         = MaterialTheme.typography.labelSmall,
        color         = TextSecondary,
        letterSpacing = 0.8.sp,
    )
}

@Composable
private fun InfoGrid(items: List<Pair<String, String>>) {
    items.chunked(2).forEach { row ->
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            row.forEach { (label, value) ->
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text  = label,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                    )
                    Text(
                        text       = value,
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color      = TextPrimary,
                    )
                    Spacer(Modifier.height(10.dp))
                }
            }
            // Celda vacía si el row tiene 1 elemento
            if (row.size == 1) Spacer(Modifier.weight(1f))
        }
    }
}



@Composable
private fun FinancialRow(label: String, value: Double, isFinal: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text       = label,
            style      = if (isFinal) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.Bold else FontWeight.Normal,
            color      = if (isFinal) TextPrimary else TextSecondary,
        )
        Text(
            text       = "$${"%.2f".format(value)}",
            style      = if (isFinal) MaterialTheme.typography.titleSmall
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.ExtraBold else FontWeight.SemiBold,
            color      = if (isFinal) Accent else TextPrimary,
        )
    }
}