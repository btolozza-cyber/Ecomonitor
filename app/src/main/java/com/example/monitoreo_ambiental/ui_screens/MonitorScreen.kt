
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
import com.example.monitoreo_ambiental.data.Medicion
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Fondo = Color(0xFF10251D)
private val Panel = Color(0xFF19382B)
private val PanelSecundario = Color(0xFF254536)
private val Verde = Color(0xFF55C98A)
private val Texto = Color(0xFFF1F5F2)
private val TextoSecundario = Color(0xFFB8C9BE)

@Composable
fun MonitorScreen(
    conectado: Boolean,
    temperatura: Double?,
    humedad: Double?,
    luminosidad: Int?,
    historial: List<Medicion>
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Fondo)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {

        item {
            Column {
                Text(
                    text = "EcoMonitor",
                    color = Verde,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Monitoreo ambiental",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Panel
                ),
                shape = RoundedCornerShape(14.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Dispositivo IoT",
                            color = Texto,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = if (conectado) {
                                "Conectado al HC-05"
                            } else {
                                "Sin conexión"
                            },
                            color = TextoSecundario,
                            fontSize = 13.sp
                        )
                    }

                    Text(
                        text = if (conectado) {
                            "● Conectado"
                        } else {
                            "● Desconectado"
                        },
                        color = if (conectado) Verde else Color(0xFFFF7777),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        item {
            Text(
                text = "Mediciones actuales",
                color = Texto,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Indicador(
                    titulo = "Temperatura",
                    valor = temperatura?.let {
                        "%.1f °C".format(it)
                    } ?: "--",
                    modifier = Modifier.weight(1f)
                )

                Indicador(
                    titulo = "Humedad",
                    valor = humedad?.let {
                        "%.1f %%".format(it)
                    } ?: "--",
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Indicador(
                titulo = "Luminosidad",
                valor = luminosidad?.let {
                    "$it / 1023"
                } ?: "--",
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Text(
                text = "Historial de mediciones",
                color = Texto,
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (historial.isEmpty()) {
            item {
                Text(
                    text = "Todavía no hay mediciones almacenadas.",
                    color = TextoSecundario,
                    fontSize = 14.sp
                )
            }
        } else {
            items(
                items = historial,
                key = { it.id }
            ) { medicion ->

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Panel
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = SimpleDateFormat(
                                "dd/MM/yyyy HH:mm:ss",
                                Locale.getDefault()
                            ).format(Date(medicion.fecha)),
                            color = Verde,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )

                        Text(
                            text = "Temperatura: ${medicion.temperatura} °C",
                            color = Texto
                        )

                        Text(
                            text = "Humedad: ${medicion.humedad} %",
                            color = Texto
                        )

                        Text(
                            text = "Luminosidad: ${medicion.luminosidad}",
                            color = Texto
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun Indicador(
    titulo: String,
    valor: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = PanelSecundario
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = titulo,
                color = TextoSecundario,
                fontSize = 13.sp
            )

            Text(
                text = valor,
                color = Verde,
                fontSize = 23.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
