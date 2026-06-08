// presentation/ui/client/orders/OrderDetailScreen.kt
package com.motosapp.presentation.ui.client.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.motosapp.domain.model.Order
import com.motosapp.presentation.components.ErrorScreen
import com.motosapp.presentation.components.LoadingScreen
import com.motosapp.presentation.components.StatusBadge
import com.motosapp.presentation.viewmodel.OrderDetailUiState
import com.motosapp.presentation.viewmodel.OrderDetailViewModel
import com.motosapp.theme.*

private val PROGRESS_STEPS = listOf(
    "PENDIENTE",
    "CONFIRMADO",
    "ENVIADO",
    "ENTREGADO",
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    orderId:   Int,
    onBack:    () -> Unit,
    viewModel: OrderDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    LaunchedEffect(orderId) { viewModel.load(orderId) }

    when (val s = state) {
        is OrderDetailUiState.Loading ->
            LoadingScreen("Cargando pedido...")
        is OrderDetailUiState.Error   ->
            ErrorScreen(s.message, onRetry = { viewModel.load(orderId) })
        is OrderDetailUiState.Success ->
            OrderDetailContent(order = s.order, onBack = onBack)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderDetailContent(order: Order, onBack: () -> Unit) {
    val dateStr = "Envío: ${order.direccion_envio}"

    val isCancelled = order.estado == "CANCELADO"
    val currentStep = PROGRESS_STEPS.indexOf(order.estado).coerceAtLeast(0)
    val taxAmount   = order.total - order.total / 1.15
    val subtotal    = order.total - taxAmount

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
                        Text(dateStr, style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver", tint = TextPrimary)
                    }
                },
                actions = { StatusBadge(order.estado, modifier = Modifier.padding(end = 16.dp)) },
                colors  = TopAppBarDefaults.topAppBarColors(containerColor = Surface),
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {

            // ── Barra de progreso ──────────────────────────────
            if (!isCancelled) {
                OrderProgressBar(
                    steps       = PROGRESS_STEPS,
                    currentStep = currentStep,
                )
            } else {
                Surface(
                    color    = Error.copy(alpha = 0.08f),
                    shape    = MaterialTheme.shapes.medium,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text     = "⚠️ Este pedido fue cancelado",
                        color    = Error,
                        fontWeight = FontWeight.SemiBold,
                        style    = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(16.dp),
                    )
                }
            }

            // ── Ítems ──────────────────────────────────────────
            // ── Ítems ──────────────────────────────────────────
            SectionCard(title = "Productos") {
                Text(
                    text = "Detalles de productos no disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }

            // ── Resumen de totales ─────────────────────────────
            SectionCard(title = "Resumen") {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    TotalLine("Subtotal (sin IVA)", subtotal,  false)
                    TotalLine("IVA (15%)",           taxAmount, false)
                    HorizontalDivider(color = Border, thickness = 0.5.dp)
                    TotalLine("Total",               order.total, true)
                }
            }

            // Meta info
            Text(
                text  = "Actualizado: $dateStr",
                style = MaterialTheme.typography.bodySmall,
                color = TextFaint,
                modifier = Modifier.align(Alignment.CenterHorizontally),
            )
        }
    }
}

// ── Barra de progreso ─────────────────────────────────────────

@Composable
private fun OrderProgressBar(steps: List<String>, currentStep: Int) {
    Surface(
        color    = Surface,
        shape    = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text       = "Estado del pedido",
                style      = MaterialTheme.typography.labelSmall,
                color      = TextSecondary,
                letterSpacing = 0.8.sp,
                modifier   = Modifier.padding(bottom = 20.dp),
            )
            Row(
                modifier          = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                steps.forEachIndexed { index, step ->
                    val isDone    = index <= currentStep
                    val isCurrent = index == currentStep
                    val color     = if (isDone) Accent else Border

                    // Nodo
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Box(
                            modifier         = Modifier
                                .size(if (isCurrent) 36.dp else 30.dp)
                                .background(
                                    if (isDone) Accent else Surface2,
                                    CircleShape,
                                ),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text       = if (isDone) "✓" else "${index + 1}",
                                color      = if (isDone) AccentOnDark else TextFaint,
                                fontSize   = if (isCurrent) 14.sp else 12.sp,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text       = step,
                            style      = MaterialTheme.typography.bodySmall.copy(fontSize = 10.sp),
                            color      = if (isDone) Accent else TextFaint,
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        )
                    }

                    // Línea conectora
                    if (index < steps.lastIndex) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(2.dp)
                                .padding(bottom = 20.dp)
                                .background(if (index < currentStep) Accent else Border),
                        )
                    }
                }
            }
        }
    }
}

// ── Sub-componentes ───────────────────────────────────────────

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    Surface(color = Surface, shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text       = title,
                style      = MaterialTheme.typography.labelSmall,
                color      = TextSecondary,
                letterSpacing = 0.8.sp,
                modifier   = Modifier.padding(bottom = 14.dp),
            )
            content()
        }
    }
}



@Composable
private fun TotalLine(label: String, value: Double, isFinal: Boolean) {
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text       = label,
            style      = if (isFinal) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.Bold else FontWeight.Normal,
            color      = if (isFinal) TextPrimary else TextSecondary,
        )
        Text(
            text       = "$${"%.2f".format(value)}",
            style      = if (isFinal) MaterialTheme.typography.titleMedium
            else MaterialTheme.typography.bodyMedium,
            fontWeight = if (isFinal) FontWeight.ExtraBold else FontWeight.SemiBold,
            color      = if (isFinal) Accent else TextPrimary,
        )
    }
}