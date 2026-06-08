package com.motosapp.presentation.ui.admin.categorias

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.motosapp.domain.model.Categoria
import com.motosapp.domain.model.CategoriaPayload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoriaFormSheet(
    categoria: Categoria? = null,
    onDismiss: () -> Unit,
    onSave: (CategoriaPayload) -> Unit
) {
    var nombre by remember { mutableStateOf(categoria?.nombre ?: "") }
    var descripcion by remember { mutableStateOf(categoria?.descripcion ?: "") }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(if (categoria == null) "Nueva Categoría" else "Editar Categoría", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = descripcion,
                onValueChange = { descripcion = it },
                label = { Text("Descripción") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(CategoriaPayload(nombre = nombre, descripcion = descripcion.ifBlank { null })) },
                modifier = Modifier.fillMaxWidth(),
                enabled = nombre.isNotBlank()
            ) {
                Text("Guardar")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
