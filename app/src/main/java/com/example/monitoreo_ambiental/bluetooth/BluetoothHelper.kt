package com.example.monitoreo_ambiental.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.UUID

class BluetoothHelper(context: Context) {

    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter? =
        bluetoothManager.adapter

    @Volatile
    private var socket: BluetoothSocket? = null

    @Volatile
    private var conectado = false

    @Volatile
    private var intentando = false

    private val uuid = UUID.fromString(
        "00001101-0000-1000-8000-00805F9B34FB"
    )

    @SuppressLint("MissingPermission")
    fun obtenerDispositivos(): Set<android.bluetooth.BluetoothDevice> {
        return try {
            bluetoothAdapter?.bondedDevices ?: emptySet()
        } catch (_: SecurityException) {
            emptySet()
        }
    }

    @SuppressLint("MissingPermission")
    fun conectar(
        direccion: String,
        alConectar: (Boolean) -> Unit,
        alRecibirDatos: (String) -> Unit
    ) {
        if (conectado || intentando) return

        intentando = true

        Thread {
            var nuevoSocket: BluetoothSocket? = null

            try {
                val adapter = bluetoothAdapter
                    ?: throw Exception("Bluetooth no disponible")

                val dispositivo = adapter.bondedDevices.firstOrNull {
                    it.address == direccion
                } ?: throw SecurityException(
                    "El dispositivo no está emparejado"
                )

                nuevoSocket =
                    dispositivo.createRfcommSocketToServiceRecord(uuid)

                nuevoSocket.connect()

                socket = nuevoSocket
                conectado = true
                intentando = false

                alConectar(true)

                recibirDatos(alRecibirDatos)

            } catch (_: Exception) {
                conectado = false
                intentando = false

                try {
                    nuevoSocket?.close()
                } catch (_: Exception) {
                }

                socket = null
                alConectar(false)
            }
        }.start()
    }

    private fun recibirDatos(
        alRecibirDatos: (String) -> Unit
    ) {
        try {
            val actual = socket ?: return

            val lector = BufferedReader(
                InputStreamReader(actual.inputStream)
            )

            while (conectado) {
                val linea = lector.readLine() ?: break

                if (linea.isNotBlank()) {
                    alRecibirDatos(linea.trim())
                }
            }

        } catch (_: Exception) {
            conectado = false
        } finally {
            desconectar()
        }
    }

    fun desconectar() {
        conectado = false

        try {
            socket?.close()
        } catch (_: Exception) {
        }

        socket = null
    }

    fun estaConectado(): Boolean {
        return conectado
    }
}
