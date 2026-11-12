
package com.example.monitoreo_ambiental.ui_screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Fondo = Color(0xFF10251D)
private val Panel = Color(0xFF19382B)
private val PanelSecundario = Color(0xFF254536)
private val Verde = Color(0xFF55C98A)
private val Texto = Color(0xFFF1F5F2)
private val TextoSecundario = Color(0xFFB8C9BE)

data class DispositivoBluetooth(
    val nombre: String,
    val direccion: String
)

@Composable
fun BluetoothScreen(
    dispositivos: List<DispositivoBluetooth>,
    conectado: Boolean,
    direccionConectada: String?,
    onConectar: (String) -> Unit,
    onDesconectar: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        Text(
            text = "Dispositivos Bluetooth",
            color = Verde,
            fontSize = 25.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Selecciona un dispositivo previamente emparejado.",
            color = TextoSecundario,
            fontSize = 14.sp
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Panel
            ),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Estado de conexión",
                    color = Texto,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (conectado) {
                        "Conectado al dispositivo"
                    } else {
                        "No hay dispositivos conectados"
                    },
                    color = if (conectado) Verde else TextoSecundario,
                    fontSize = 14.sp
                )

                if (conectado) {
                    Button(
                        onClick = onDesconectar,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF8C4545)
                        )
                    ) {
                        Text("Desconectar")
                    }
                }
            }
        }

        Text(
            text = "Dispositivos emparejados",
            color = Texto,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        if (dispositivos.isEmpty()) {
            Text(
                text = "No se encontraron dispositivos emparejados.",
                color = TextoSecundario,
                fontSize = 14.sp
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = dispositivos,
                    key = { it.direccion }
                ) { dispositivo ->

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = PanelSecundario
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    text = dispositivo.nombre,
                                    color = Texto,
                                    fontWeight = FontWeight.Bold
                                )

                                Text(
                                    text = dispositivo.direccion,
                                    color = TextoSecundario,
                                    fontSize = 12.sp
                                )
                            }

                            if (conectado &&
                                direccionConectada == dispositivo.direccion
                            ) {
                                Text(
                                    text = "Conectado",
                                    color = Verde,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            } else {
                                Button(
                                    onClick = {
                                        onConectar(dispositivo.direccion)
                                    },
                                    enabled = !conectado,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Verde,
                                        contentColor = Fondo
                                    )
                                ) {
                                    Text("Conectar")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
