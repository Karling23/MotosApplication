// presentation/ui/admin/motocicletas/MotocicletasAdminScreen.kt
package com.motosapp.presentation.ui.admin.motocicletas

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.motosapp.domain.model.Motocicleta
import com.motosapp.presentation.components.LoadingScreen
import com.motosapp.presentation.components.ErrorScreen
import com.motosapp.presentation.viewmodel.MotocicletaStockFilter
import com.motosapp.presentation.viewmodel.MotocicletasAdminViewModel
import com.motosapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotocicletasAdminScreen(
    viewModel: MotocicletasAdminViewModel = hiltViewModel(),
) {
    val state      by viewModel.state.collectAsState()
    val filtered   by viewModel.filtered.collectAsState()
    val formState  by viewModel.formState.collectAsState()
    val marcas     by viewModel.marcas.collectAsState()
    val categorias by viewModel.categorias.collectAsState()

    var showForm      by remember { mutableStateOf(false) }
    var editTarget    by remember { mutableStateOf<Motocicleta?>(null) }
    var restockTarget by remember { mutableStateOf<Motocicleta?>(null) }
    var deleteTarget  by remember { mutableStateOf<Motocicleta?>(null) }
    var snackMsg      by remember { mutableStateOf<String?>(null) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(snackMsg) {
        snackMsg?.let {
            snackbarHostState.showSnackbar(it)
            snackMsg = null
        }
    }

    Scaffold(
        snackbarHost   = { SnackbarHost(snackbarHostState) },
        containerColor = Background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(Background),
        ) {
            // ── Header ────────────────────────────────────────────
            Surface(color = Surface, tonalElevation = 0.dp) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment     = Alignment.CenterVertically,
                    ) {
                        Column {
                            Text(
                                "Motocicletaos",
                                style      = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                            Text(
                                "${state.total} motocicletaos",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary,
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = viewModel::load) {
                                Icon(Icons.Default.Refresh, null, tint = TextSecondary)
                            }
                            Button(
                                onClick = { editTarget = null; showForm = true },
                                colors  = ButtonDefaults.buttonColors(
                                    containerColor = Accent,
                                    contentColor   = AccentOnDark,
                                ),
                                shape          = MaterialTheme.shapes.medium,
                                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                            ) {
                                Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(4.dp))
                                Text("Nuevo", fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    OutlinedTextField(
                        value         = state.search,
                        onValueChange = viewModel::setSearch,
                        placeholder   = { Text("Buscar motocicleta...", color = TextFaint) },
                        leadingIcon   = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth(),
                        shape         = MaterialTheme.shapes.medium,
                        colors        = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = Accent,
                            unfocusedBorderColor = Border,
                            cursorColor          = Accent,
                        ),
                    )

                    Spacer(Modifier.height(10.dp))

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(MotocicletaStockFilter.entries) { filter ->
                            FilterChip(
                                selected = state.stockFilter == filter,
                                onClick  = { viewModel.setStockFilter(filter) },
                                label    = {
                                    Text(filter.label, style = MaterialTheme.typography.labelSmall)
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Accent,
                                    selectedLabelColor     = AccentOnDark,
                                    containerColor         = Surface2,
                                    labelColor             = TextSecondary,
                                ),
                            )
                        }
                    }
                }
            }

            // ── Lista ──────────────────────────────────────────────
            when {
                state.isLoading     -> LoadingScreen("Cargando motocicletaos...")
                state.error != null -> ErrorScreen(state.error!!, onRetry = viewModel::load)
                filtered.isEmpty()  -> {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📦", fontSize = 48.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (state.search.isBlank() && state.stockFilter == MotocicletaStockFilter.ALL)
                                    "Sin motocicletaos"
                                else
                                    "Sin resultados",
                                style      = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color      = TextPrimary,
                            )
                        }
                    }
                }
                else -> {
                    LazyColumn(
                        modifier            = Modifier.fillMaxSize(),
                        contentPadding      = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        items(filtered.size, key = { filtered[it].id }) { index ->
                            val motocicleta = filtered[index]
                            
                            if (index >= filtered.size - 1 && !state.isLoading && !state.isPaginating && state.hasMore) {
                                LaunchedEffect(index) { viewModel.loadMore() }
                            }

                            MotocicletaAdminCard(
                                motocicleta   = motocicleta,
                                onToggle  = { viewModel.toggleActive(motocicleta.id, !motocicleta.is_active) },
                                onEdit    = { editTarget = motocicleta; showForm = true },
                                onRestock = { restockTarget = motocicleta },
                                onDelete  = { deleteTarget = motocicleta },
                            )
                        }
                        if (state.isPaginating) {
                            item {
                                Box(modifier = Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(color = Accent)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Bottom Sheet: formulario crear / editar ────────────────────
    if (showForm) {
        MotocicletaFormSheet(
            initial    = editTarget,
            marcas     = marcas,
            categorias = categorias,
            formState  = formState,
            onSave     = { payload ->
                if (editTarget != null) viewModel.updateMotocicleta(editTarget!!.id, payload)
                else viewModel.createMotocicleta(payload)
            },
            onDismiss = {
                showForm   = false
                editTarget = null
                viewModel.resetFormState()
            },
        )
    }

    // ── Dialog: restock ───────────────────────────────────────────
    restockTarget?.let { motocicleta ->
        RestockDialog(
            motocicleta   = motocicleta,
            onRestock = { qty ->
                viewModel.restock(motocicleta.id, qty) { msg ->
                    snackMsg      = msg
                    restockTarget = null
                }
            },
            onDismiss = { restockTarget = null },
        )
    }

    // ── Dialog: confirmación de eliminación ───────────────────────
    deleteTarget?.let { motocicleta ->
        AlertDialog(
            onDismissRequest = { deleteTarget = null },
            containerColor   = Surface,
            shape            = MaterialTheme.shapes.large,
            title            = { Text("¿Eliminar motocicletao?", color = TextPrimary) },
            text             = {
                Text(
                    "\"${motocicleta.displayName}\" se eliminará permanentemente.",
                    color = TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteMotocicleta(motocicleta.id)
                    deleteTarget = null
                }) {
                    Text("Eliminar", color = Error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { deleteTarget = null }) {
                    Text("Cancelar", color = TextSecondary)
                }
            },
        )
    }
}

// ── MotocicletaAdminCard ──────────────────────────────────────────────────────────

@Composable
private fun MotocicletaAdminCard(
    motocicleta:   Motocicleta,
    onToggle:  () -> Unit,
    onEdit:    () -> Unit,
    onRestock: () -> Unit,
    onDelete:  () -> Unit,
) {
    Surface(
        shape    = MaterialTheme.shapes.large,
        color    = if (motocicleta.is_active) Surface else Surface.copy(alpha = 0.6f),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier          = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Imagen
            Box(
                modifier         = Modifier
                    .size(56.dp)
                    .background(Surface2, MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center,
            ) {
                if (motocicleta.displayImageUrl != null) {
                    AsyncImage(
                        model              = motocicleta.displayImageUrl,
                        contentDescription = motocicleta.displayName,
                        contentScale       = ContentScale.Crop,
                        modifier           = Modifier.fillMaxSize(),
                    )
                } else {
                    Text("📦", fontSize = 22.sp)
                }
            }

            Spacer(Modifier.width(12.dp))

            // Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = motocicleta.displayName,
                    style      = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = TextPrimary,
                    maxLines   = 1,
                    overflow   = TextOverflow.Ellipsis,
                )
                Text(
                    text  = "Categoría ${motocicleta.categoria}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Text(
                        "$${"%.2f".format(motocicleta.displayPrice)}",
                        style      = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color      = Accent,
                    )
                    // Badge de stock
                    Surface(
                        color = when {
                            motocicleta.stock == 0 -> Error.copy(alpha = 0.15f)
                            motocicleta.stock < 5  -> Warning.copy(alpha = 0.15f)
                            else               -> Success.copy(alpha = 0.12f)
                        },
                        shape = MaterialTheme.shapes.extraSmall,
                    ) {
                        Text(
                            text = when {
                                motocicleta.stock == 0 -> "Agotado"
                                else               -> "${motocicleta.stock} uds."
                            },
                            color = when {
                                motocicleta.stock == 0 -> Error
                                motocicleta.stock < 5  -> Warning
                                else               -> Success
                            },
                            fontSize   = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier   = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        )
                    }
                }
            }

            // Acciones
            Column(horizontalAlignment = Alignment.End) {
                Switch(
                    checked         = motocicleta.is_active,
                    onCheckedChange = { onToggle() },
                    colors          = SwitchDefaults.colors(
                        checkedThumbColor    = AccentOnDark,
                        checkedTrackColor    = Accent,
                        uncheckedTrackColor  = Surface2,
                        uncheckedBorderColor = Border,
                    ),
                    modifier = Modifier.size(width = 48.dp, height = 28.dp),
                )
                Spacer(Modifier.height(4.dp))
                Row {
                    IconButton(onClick = onRestock, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Inventory, null,
                            tint     = Accent,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Edit, null,
                            tint     = TextSecondary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Default.Delete, null,
                            tint     = Error,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }
            }
        }
    }
}