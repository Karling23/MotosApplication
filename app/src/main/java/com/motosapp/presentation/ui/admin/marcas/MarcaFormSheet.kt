package com.motosapp.presentation.ui.admin.marcas

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.motosapp.domain.model.Marca
import com.motosapp.domain.model.MarcaPayload

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarcaFormSheet(
    marca: Marca? = null,
    onDismiss: () -> Unit,
    onSave: (MarcaPayload) -> Unit
) {
    var nombre by remember { mutableStateOf(marca?.nombre ?: "") }
    var paisOrigen by remember { mutableStateOf(marca?.pais_origen ?: "") }
    var descripcion by remember { mutableStateOf(marca?.descripcion ?: "") }
    var activo by remember { mutableStateOf(marca?.activo ?: true) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Marca", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = nombre,
                onValueChange = { nombre = it },
                label = { Text("Nombre") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = paisOrigen,
                onValueChange = { paisOrigen = it },
                label = { Text("País de Origen") },
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
            Row(
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Activo")
                Spacer(modifier = Modifier.weight(1f))
                Switch(checked = activo, onCheckedChange = { activo = it })
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { onSave(MarcaPayload(nombre, paisOrigen, descripcion, activo)) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Guardar")
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
