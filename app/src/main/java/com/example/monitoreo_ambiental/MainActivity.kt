
package com.example.monitoreo_ambiental

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.monitoreo_ambiental.bluetooth.BluetoothHelper
import com.example.monitoreo_ambiental.data.ProcesadorMediciones
import com.example.monitoreo_ambiental.ui.theme.Monitoreo_ambientalTheme
import com.example.monitoreo_ambiental.ui_screens.BluetoothScreen
import com.example.monitoreo_ambiental.ui_screens.DispositivoBluetooth
import com.example.monitoreo_ambiental.ui_screens.MonitorScreen
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            Monitoreo_ambientalTheme {
                EcoMonitorApp()
            }
        }
    }
}

@SuppressLint("MissingPermission")
@Composable
fun EcoMonitorApp() {

    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val bluetooth = remember { BluetoothHelper(context) }
    val procesador = remember { ProcesadorMediciones(context) }
    val handler = remember { Handler(Looper.getMainLooper()) }

    var usuario by remember {
        mutableStateOf(auth.currentUser?.email)
    }

    var pantalla by remember { mutableStateOf("monitor") }

    var dispositivos by remember {
        mutableStateOf<List<DispositivoBluetooth>>(emptyList())
    }

    var conectado by remember { mutableStateOf(false) }
    var direccionConectada by remember { mutableStateOf<String?>(null) }

    var temperatura by remember { mutableStateOf<Double?>(null) }
    var humedad by remember { mutableStateOf<Double?>(null) }
    var luminosidad by remember { mutableStateOf<Int?>(null) }

    var historial by remember {
        mutableStateOf(procesador.obtenerHistorial())
    }

    var mensajeBluetooth by remember { mutableStateOf("") }

    // Sincronización del estado de autenticación con Firebase.
    DisposableEffect(auth) {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            usuario = firebaseAuth.currentUser?.email
        }

        auth.addAuthStateListener(listener)

        onDispose {
            auth.removeAuthStateListener(listener)
        }
    }

    fun cargarDispositivos() {
        dispositivos = bluetooth.obtenerDispositivos().map {
            DispositivoBluetooth(
                nombre = it.name ?: "Dispositivo desconocido",
                direccion = it.address
            )
        }
    }

    val permisoBluetoothLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission()
        ) { concedido ->

            if (concedido) {
                cargarDispositivos()
                mensajeBluetooth = ""
            } else {
                mensajeBluetooth = "Se necesita permiso Bluetooth para continuar."
            }
        }

    fun solicitarPermisoBluetooth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {

            val permiso = Manifest.permission.BLUETOOTH_CONNECT

            if (
                ContextCompat.checkSelfPermission(
                    context,
                    permiso
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                cargarDispositivos()
            } else {
                permisoBluetoothLauncher.launch(permiso)
            }

        } else {
            cargarDispositivos()
        }
    }

    LaunchedEffect(pantalla) {
        if (pantalla == "bluetooth") {
            solicitarPermisoBluetooth()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            bluetooth.desconectar()
            procesador.cerrar()
        }
    }

    if (usuario == null) {

        LoginScreen(
            auth = auth,
            onAuthSuccess = {
                usuario = auth.currentUser?.email
                pantalla = "monitor"
            }
        )

    } else {

        Column(
            modifier = Modifier.fillMaxSize()
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {

                Button(
                    onClick = {
                        pantalla = "monitor"
                    }
                ) {
                    Text("Monitor")
                }

                Button(
                    onClick = {
                        pantalla = "bluetooth"
                    }
                ) {
                    Text("Bluetooth")
                }

                Button(
                    onClick = {
                        bluetooth.desconectar()

                        conectado = false
                        direccionConectada = null

                        temperatura = null
                        humedad = null
                        luminosidad = null

                        auth.signOut()
                        pantalla = "monitor"
                    }
                ) {
                    Text("Cerrar sesión")
                }
            }

            if (pantalla == "monitor") {

                MonitorScreen(
                    temperatura = temperatura,
                    humedad = humedad,
                    luminosidad = luminosidad,
                    historial = historial,
                    conectado = conectado
                )

            } else {

                BluetoothScreen(
                    dispositivos = dispositivos,
                    conectado = conectado,
                    direccionConectada = direccionConectada,

                    onConectar = { direccion ->

                        bluetooth.conectar(
                            direccion = direccion,

                            alConectar = { resultado ->
                                handler.post {
                                    conectado = resultado

                                    if (resultado) {
                                        direccionConectada = direccion
                                        mensajeBluetooth = "Dispositivo conectado."
                                    } else {
                                        direccionConectada = null
                                        mensajeBluetooth =
                                            "No se pudo conectar al dispositivo."
                                    }
                                }
                            },

                            alRecibirDatos = { linea ->

                                val guardado =
                                    procesador.procesarLinea(linea)

                                if (guardado) {

                                    val valores = linea.split(",")

                                    if (valores.size == 3) {
                                        handler.post {
                                            temperatura =
                                                valores[0].toDoubleOrNull()

                                            humedad =
                                                valores[1].toDoubleOrNull()

                                            luminosidad =
                                                valores[2].toIntOrNull()

                                            historial =
                                                procesador.obtenerHistorial()
                                        }
                                    }
                                }
                            }
                        )
                    },

                    onDesconectar = {
                        bluetooth.desconectar()
                        conectado = false
                        direccionConectada = null
                    }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onAuthSuccess: () -> Unit
) {

    var correo by remember { mutableStateOf("") }
    var contrasena by remember { mutableStateOf("") }
    var registrar by remember { mutableStateOf(false) }
    var mensaje by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text(
            text = "EcoMonitor",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = correo,
            onValueChange = {
                correo = it
                mensaje = ""
            },
            label = {
                Text("Correo electrónico")
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = contrasena,
            onValueChange = {
                contrasena = it
                mensaje = ""
            },
            label = {
                Text("Contraseña")
            },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {

                val email = correo.trim()

                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    mensaje = "Ingresa un correo electrónico válido."
                    return@Button
                }

                if (contrasena.length < 6) {
                    mensaje = "La contraseña debe tener al menos 6 caracteres."
                    return@Button
                }

                cargando = true
                mensaje = ""

                val tarea = if (registrar) {
                    auth.createUserWithEmailAndPassword(
                        email,
                        contrasena
                    )
                } else {
                    auth.signInWithEmailAndPassword(
                        email,
                        contrasena
                    )
                }

                tarea.addOnCompleteListener { resultado ->

                    cargando = false

                    if (resultado.isSuccessful) {
                        contrasena = ""
                        onAuthSuccess()
                    } else {
                        mensaje = resultado.exception?.localizedMessage
                            ?: "No se pudo completar la autenticación."
                    }
                }
            },
            enabled = !cargando,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = when {
                    cargando -> "Procesando..."
                    registrar -> "Registrarse"
                    else -> "Iniciar sesión"
                }
            )
        }

        TextButton(
            onClick = {
                registrar = !registrar
                mensaje = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                if (registrar) {
                    "¿Ya tienes cuenta? Inicia sesión"
                } else {
                    "¿No tienes cuenta? Regístrate"
                }
            )
        }

        if (mensaje.isNotBlank()) {

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mensaje,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
