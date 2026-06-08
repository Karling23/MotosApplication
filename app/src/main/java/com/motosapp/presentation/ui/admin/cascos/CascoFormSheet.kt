package com.motosapp.presentation.ui.admin.cascos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.motosapp.domain.model.Casco
import com.motosapp.domain.model.CascoPayload
import com.motosapp.domain.model.Marca
import com.motosapp.presentation.components.ShopTextField
import com.motosapp.presentation.viewmodel.CascoFormState
import com.motosapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CascoFormSheet(
    casco: Casco?,
    marcas: List<Marca>,
    formState: CascoFormState,
    onDismiss: () -> Unit,
    onSave: (CascoPayload) -> Unit,
) {
    val isEdit = casco != null

    var modelo by remember { mutableStateOf(casco?.modelo ?: "") }
    var descripcion by remember { mutableStateOf(casco?.descripcion ?: "") }
    var precio by remember { mutableStateOf(casco?.precio ?: "") }
    var stock by remember { mutableStateOf(casco?.stock?.toString() ?: "") }
    var talla by remember { mutableStateOf(casco?.talla ?: "") }
    var color by remember { mutableStateOf(casco?.color ?: "") }
    var certificacion by remember { mutableStateOf(casco?.certificacion ?: "") }
    var isActive by remember { mutableStateOf(casco?.is_active ?: true) }

    var selectedMarca by remember { mutableStateOf<Int?>(casco?.marca) }
    var marcaExpanded by remember { mutableStateOf(false) }

    val isSaving = formState is CascoFormState.Saving
    val stockVal = stock.toIntOrNull()

    val canSave = modelo.length >= 2 &&
            precio.isNotBlank() &&
            stockVal != null && stockVal >= 0 &&
            talla.isNotBlank() &&
            color.isNotBlank() &&
            certificacion.isNotBlank() &&
            selectedMarca != null &&
            !isSaving

    LaunchedEffect(formState) {
        if (formState is CascoFormState.Success) onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor = Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = if (isEdit) "Editar Casco" else "Nuevo Casco",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
            )

            if (formState is CascoFormState.Error) {
                Surface(color = Error.copy(alpha = 0.1f), shape = MaterialTheme.shapes.small) {
                    Text(formState.message, color = Error, modifier = Modifier.padding(12.dp))
                }
            }

            ShopTextField(
                value = modelo,
                onValueChange = { modelo = it },
                label = "Modelo *",
                enabled = !isSaving,
            )

            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                minLines = 3, maxLines = 4,
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth(),
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = precio,
                    onValueChange = { precio = it },
                    label = { Text("Precio $ *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled = !isSaving,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock *") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled = !isSaving,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = talla,
                    onValueChange = { talla = it },
                    label = { Text("Talla *") },
                    enabled = !isSaving,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
                OutlinedTextField(
                    value = certificacion,
                    onValueChange = { certificacion = it },
                    label = { Text("Certificación *") },
                    enabled = !isSaving,
                    singleLine = true,
                    modifier = Modifier.weight(1f),
                )
            }

            OutlinedTextField(
                value = color,
                onValueChange = { color = it },
                label = { Text("Color *") },
                enabled = !isSaving,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            ExposedDropdownMenuBox(
                expanded = marcaExpanded,
                onExpandedChange = { marcaExpanded = !marcaExpanded },
            ) {
                OutlinedTextField(
                    value = marcas.find { it.id == selectedMarca }?.nombre ?: "— Seleccionar marca —",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Marca *") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = marcaExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded = marcaExpanded,
                    onDismissRequest = { marcaExpanded = false },
                ) {
                    marcas.filter { it.activo }.forEach { marca ->
                        DropdownMenuItem(
                            text = { Text(marca.nombre) },
                            onClick = { selectedMarca = marca.id; marcaExpanded = false },
                        )
                    }
                }
            }

            Surface(color = Surface2, shape = MaterialTheme.shapes.medium) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Activo (visible en catálogo)", color = TextPrimary)
                    Switch(checked = isActive, onCheckedChange = { isActive = it }, enabled = !isSaving)
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    enabled = !isSaving,
                    modifier = Modifier.weight(1f).height(52.dp),
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        onSave(
                            CascoPayload(
                                marca = selectedMarca!!,
                                modelo = modelo.trim(),
                                talla = talla.trim(),
                                color = color.trim(),
                                certificacion = certificacion.trim(),
                                precio = precio.trim(),
                                stock = stockVal!!,
                                isActive = isActive,
                                descripcion = descripcion.trim(),
                            )
                        )
                    },
                    enabled = canSave,
                    modifier = Modifier.weight(1f).height(52.dp),
                ) {
                    Text(if (isSaving) "Guardando..." else "Guardar")
                }
            }
        }
    }
}
