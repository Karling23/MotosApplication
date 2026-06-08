// presentation/ui/admin/orders/StatusDropdown.kt
package com.motosapp.presentation.ui.admin.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motosapp.presentation.components.orderStatusColor
import com.motosapp.theme.*

@Composable
fun StatusDropdown(
    current:   String,
    onChange:  (String) -> Unit,
    modifier:  Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val color    = orderStatusColor(current)
    val statusMap = mapOf(
        "PENDIENTE" to "Pendiente",
        "CONFIRMADO" to "Confirmado",
        "ENVIADO" to "Enviado",
        "ENTREGADO" to "Entregado",
        "CANCELADO" to "Cancelado"
    )
    val currentLabel = statusMap[current] ?: current

    Box(modifier = modifier) {
        // Botón actual
        Surface(
            onClick = { expanded = true },
            shape   = RoundedCornerShape(999.dp),
            color   = color.copy(alpha = 0.12f),
            border  = androidx.compose.foundation.BorderStroke(
                width = 1.dp,
                color = color.copy(alpha = 0.4f),
            ),
        ) {
            Row(
                modifier          = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(7.dp)
                        .background(color, RoundedCornerShape(50)),
                )
                Text(
                    text       = currentLabel,
                    color      = color,
                    fontSize   = 12.sp,
                    fontWeight = FontWeight.Bold,
                )
                Icon(
                    Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint     = color,
                    modifier = Modifier.size(14.dp),
                )
            }
        }

        // Dropdown
        DropdownMenu(
            expanded         = expanded,
            onDismissRequest = { expanded = false },
            modifier         = Modifier.background(Surface),
        ) {
            statusMap.forEach { (status, label) ->
                val statusColor = orderStatusColor(status)
                val isSelected  = status == current

                DropdownMenuItem(
                    text = {
                        Row(
                            verticalAlignment     = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(statusColor, RoundedCornerShape(50)),
                            )
                            Text(
                                text       = label,
                                color      = if (isSelected) statusColor else TextSecondary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize   = 13.sp,
                            )
                        }
                    },
                    trailingIcon = if (isSelected) {
                        { Icon(Icons.Default.Check, null, tint = statusColor, modifier = Modifier.size(14.dp)) }
                    } else null,
                    onClick = {
                        expanded = false
                        if (status != current) onChange(status)
                    },
                    colors = MenuDefaults.itemColors(
                        textColor = if (isSelected) statusColor else TextSecondary,
                    ),
                )
            }
        }
    }
}