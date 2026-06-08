// presentation/ui/admin/motocicletas/MotocicletaFormSheet.kt
package com.motosapp.presentation.ui.admin.motocicletas

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
import com.motosapp.domain.model.Categoria
import com.motosapp.domain.model.Marca
import com.motosapp.domain.model.Motocicleta
import com.motosapp.domain.model.MotocicletaPayload
import com.motosapp.presentation.components.ShopTextField
import com.motosapp.presentation.viewmodel.MotocicletaFormState
import com.motosapp.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotocicletaFormSheet(
    initial:    Motocicleta?,
    marcas:     List<Marca>,
    categorias: List<Categoria>,
    formState:  MotocicletaFormState,
    onSave:     (MotocicletaPayload) -> Unit,
    onDismiss:  () -> Unit,
) {
    val isEdit = initial != null

    // ── Estado del formulario ─────────────────────────────────────
    var modelo      by remember { mutableStateOf(initial?.modelo             ?: "") }
    var descripcion by remember { mutableStateOf(initial?.descripcion        ?: "") }
    var precio      by remember { mutableStateOf(initial?.precio             ?: "") }
    var stock       by remember { mutableStateOf(initial?.stock?.toString()  ?: "") }
    var anio        by remember { mutableStateOf(initial?.anio?.toString()   ?: "") }
    var cilindrada  by remember { mutableStateOf(initial?.cilindrada?.toString() ?: "") }
    var color       by remember { mutableStateOf(initial?.color              ?: "") }
    var isActive    by remember { mutableStateOf(initial?.is_active          ?: true) }

    // Dropdowns
    var selectedMarca by remember { mutableStateOf<Int?>(initial?.marca) }
    var marcaExpanded by remember { mutableStateOf(false) }
    var selectedCat   by remember { mutableStateOf<Int?>(initial?.categoria) }
    var catExpanded   by remember { mutableStateOf(false) }

    val isSaving      = formState is MotocicletaFormState.Saving
    val stockVal      = stock.toIntOrNull()
    val anioVal       = anio.toIntOrNull()
    val cilindradaVal = cilindrada.toIntOrNull()

    val canSave = modelo.length >= 2 &&
            precio.isNotBlank() &&
            stockVal != null && stockVal >= 0 &&
            anioVal != null && anioVal in 1900..2100 &&
            cilindradaVal != null && cilindradaVal > 0 &&
            color.isNotBlank() &&
            selectedMarca != null &&
            selectedCat != null &&
            !isSaving

    LaunchedEffect(formState) {
        if (formState is MotocicletaFormState.Success) onDismiss()
    }

    ModalBottomSheet(
        onDismissRequest = { if (!isSaving) onDismiss() },
        containerColor   = Surface,
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
            // ── Título ─────────────────────────────────────────────
            Text(
                text       = if (isEdit) "Editar: ${initial?.modelo}" else "Nueva motocicleta",
                style      = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color      = TextPrimary,
            )

            // ── Error global ───────────────────────────────────────
            if (formState is MotocicletaFormState.Error) {
                Surface(
                    color    = Error.copy(alpha = 0.1f),
                    shape    = MaterialTheme.shapes.small,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        formState.message, color = Error,
                        style    = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(12.dp),
                    )
                }
            }

            // ── Modelo ─────────────────────────────────────────────
            ShopTextField(
                value         = modelo,
                onValueChange = { modelo = it },
                label         = "Modelo *",
                placeholder   = "ej. CBR600RR",
                isError       = modelo.isNotEmpty() && modelo.length < 2,
                errorMessage  = "Mínimo 2 caracteres",
                enabled       = !isSaving,
            )

            // ── Descripción ────────────────────────────────────────
            OutlinedTextField(
                value         = descripcion,
                onValueChange = { descripcion = it },
                label         = { Text("Descripción") },
                minLines      = 3, maxLines = 4,
                enabled       = !isSaving,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Accent,
                    unfocusedBorderColor = Border,
                    focusedLabelColor    = Accent,
                    unfocusedLabelColor  = TextSecondary,
                ),
            )

            // ── Precio y Stock ─────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value           = precio,
                    onValueChange   = { precio = it },
                    label           = { Text("Precio $ *") },
                    placeholder     = { Text("0.00") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    enabled         = !isSaving,
                    singleLine      = true,
                    modifier        = Modifier.weight(1f),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Accent,
                        unfocusedBorderColor = Border,
                        focusedLabelColor    = Accent,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                )
                OutlinedTextField(
                    value           = stock,
                    onValueChange   = { stock = it },
                    label           = { Text("Stock *") },
                    placeholder     = { Text("0") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    enabled         = !isSaving,
                    singleLine      = true,
                    modifier        = Modifier.weight(1f),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Accent,
                        unfocusedBorderColor = Border,
                        focusedLabelColor    = Accent,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                )
            }

            // ── Año y Cilindrada ───────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value           = anio,
                    onValueChange   = { anio = it },
                    label           = { Text("Año *") },
                    placeholder     = { Text("2024") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError         = anio.isNotEmpty() && (anioVal == null || anioVal !in 1900..2100),
                    enabled         = !isSaving,
                    singleLine      = true,
                    modifier        = Modifier.weight(1f),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Accent,
                        unfocusedBorderColor = Border,
                        focusedLabelColor    = Accent,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                )
                OutlinedTextField(
                    value           = cilindrada,
                    onValueChange   = { cilindrada = it },
                    label           = { Text("Cilindrada (cc) *") },
                    placeholder     = { Text("600") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError         = cilindrada.isNotEmpty() && (cilindradaVal == null || cilindradaVal <= 0),
                    enabled         = !isSaving,
                    singleLine      = true,
                    modifier        = Modifier.weight(1f),
                    colors          = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Accent,
                        unfocusedBorderColor = Border,
                        focusedLabelColor    = Accent,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                )
            }

            // ── Color ──────────────────────────────────────────────
            OutlinedTextField(
                value         = color,
                onValueChange = { color = it },
                label         = { Text("Color *") },
                placeholder   = { Text("ej. Azul metálico") },
                enabled       = !isSaving,
                singleLine    = true,
                modifier      = Modifier.fillMaxWidth(),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = Accent,
                    unfocusedBorderColor = Border,
                    focusedLabelColor    = Accent,
                    unfocusedLabelColor  = TextSecondary,
                ),
            )

            // ── Dropdown Marca ─────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded         = marcaExpanded,
                onExpandedChange = { marcaExpanded = !marcaExpanded },
            ) {
                OutlinedTextField(
                    value         = marcas.find { it.id == selectedMarca }?.nombre ?: "— Seleccionar marca —",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Marca *") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = marcaExpanded) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Accent,
                        unfocusedBorderColor = if (selectedMarca == null) Error else Border,
                        focusedLabelColor    = Accent,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded         = marcaExpanded,
                    onDismissRequest = { marcaExpanded = false },
                ) {
                    marcas.filter { it.activo }.forEach { marca ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    marca.nombre,
                                    color      = if (selectedMarca == marca.id) Accent else TextPrimary,
                                    fontWeight = if (selectedMarca == marca.id) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            onClick = { selectedMarca = marca.id; marcaExpanded = false },
                        )
                    }
                }
            }

            // ── Dropdown Categoría ─────────────────────────────────
            ExposedDropdownMenuBox(
                expanded         = catExpanded,
                onExpandedChange = { catExpanded = !catExpanded },
            ) {
                OutlinedTextField(
                    value         = categorias.find { it.id == selectedCat }?.nombre ?: "— Seleccionar categoría —",
                    onValueChange = {},
                    readOnly      = true,
                    label         = { Text("Categoría *") },
                    trailingIcon  = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = catExpanded) },
                    colors        = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = Accent,
                        unfocusedBorderColor = if (selectedCat == null) Error else Border,
                        focusedLabelColor    = Accent,
                        unfocusedLabelColor  = TextSecondary,
                    ),
                    modifier = Modifier.menuAnchor().fillMaxWidth(),
                )
                ExposedDropdownMenu(
                    expanded         = catExpanded,
                    onDismissRequest = { catExpanded = false },
                ) {
                    categorias.filter { it.activo }.forEach { cat ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    cat.nombre,
                                    color      = if (selectedCat == cat.id) Accent else TextPrimary,
                                    fontWeight = if (selectedCat == cat.id) FontWeight.Bold else FontWeight.Normal,
                                )
                            },
                            onClick = { selectedCat = cat.id; catExpanded = false },
                        )
                    }
                }
            }

            // ── Toggle activo ──────────────────────────────────────
            Surface(
                color    = Surface2,
                shape    = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Row(
                    modifier              = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(
                            "Motocicleta activa",
                            style      = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color      = TextPrimary,
                        )
                        Text(
                            "Visible en el catálogo público",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                        )
                    }
                    Switch(
                        checked         = isActive,
                        onCheckedChange = { isActive = it },
                        enabled         = !isSaving,
                        colors          = SwitchDefaults.colors(
                            checkedThumbColor    = AccentOnDark,
                            checkedTrackColor    = Accent,
                            uncheckedTrackColor  = Surface,
                            uncheckedBorderColor = Border,
                        ),
                    )
                }
            }

            // ── Botones ────────────────────────────────────────────
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick  = { if (!isSaving) onDismiss() },
                    enabled  = !isSaving,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border   = ButtonDefaults.outlinedButtonBorder.copy(
                        brush = androidx.compose.ui.graphics.SolidColor(Border),
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) { Text("Cancelar") }

                Button(
                    onClick = {
                        onSave(
                            MotocicletaPayload(
                                marca       = selectedMarca!!,
                                categoria   = selectedCat!!,
                                modelo      = modelo.trim(),
                                anio        = anioVal!!,
                                cilindrada  = cilindradaVal!!,
                                color       = color.trim(),
                                precio      = precio.trim(),
                                stock       = stockVal!!,
                                isActive    = isActive,
                                descripcion = descripcion.trim(),
                            )
                        )
                    },
                    enabled  = canSave,
                    modifier = Modifier.weight(1f).height(52.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor         = Accent,
                        contentColor           = AccentOnDark,
                        disabledContainerColor = Accent.copy(alpha = 0.4f),
                    ),
                    shape = MaterialTheme.shapes.medium,
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            color       = AccentOnDark,
                            modifier    = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(
                        if (isSaving) "Guardando..."
                        else if (isEdit) "Guardar cambios"
                        else "Crear motocicleta",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }
    }
}