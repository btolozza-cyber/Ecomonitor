
package com.example.monitoreo_ambiental.data

import android.content.Context

class ProcesadorMediciones(context: Context) {

    private val database = DatabaseHelper(context)

    fun procesarLinea(linea: String): Boolean {

        val datos = linea.split(",")

        if (datos.size != 3) {
            return false
        }

        val temperatura = datos[0].trim().toDoubleOrNull()
            ?: return false

        val humedad = datos[1].trim().toDoubleOrNull()
            ?: return false

        val luminosidad = datos[2].trim().toIntOrNull()
            ?: return false

        if (temperatura !in -40.0..80.0) {
            return false
        }

        if (humedad !in 0.0..100.0) {
            return false
        }

        if (luminosidad !in 0..1023) {
            return false
        }

        return database.guardarMedicion(
            temperatura = temperatura,
            humedad = humedad,
            luminosidad = luminosidad
        ) != -1L
    }

    fun obtenerHistorial(): List<Medicion> {
        return database.obtenerMediciones()
    }

    fun cerrar() {
        database.close()
    }
}
