
package com.example.monitoreo_ambiental.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class Medicion(
    val id: Int,
    val temperatura: Double,
    val humedad: Double,
    val luminosidad: Int,
    val fecha: Long
)

class DatabaseHelper(context: Context) : SQLiteOpenHelper(
    context,
    "ecomonitor.db",
    null,
    2
) {

    companion object {
        private const val ALIAS = "EcoMonitorDatabaseKey"
        private const val IV_SIZE = 12
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("""
            CREATE TABLE mediciones (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                datos_cifrados TEXT NOT NULL
            )
        """.trimIndent())
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db.execSQL("""
                CREATE TABLE mediciones_seguras (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    datos_cifrados TEXT NOT NULL
                )
            """.trimIndent())

            val cursor = db.query(
                "mediciones",
                arrayOf(
                    "id",
                    "temperatura",
                    "humedad",
                    "luminosidad",
                    "fecha"
                ),
                null, null, null, null, null
            )

            cursor.use {
                while (it.moveToNext()) {
                    val id = it.getInt(0)
                    val temperatura = it.getDouble(1)
                    val humedad = it.getDouble(2)
                    val luminosidad = it.getInt(3)
                    val fecha = it.getLong(4)

                    val datos = "$temperatura|$humedad|$luminosidad|$fecha"

                    val valores = ContentValues().apply {
                        put("id", id)
                        put("datos_cifrados", cifrar(datos))
                    }

                    db.insertOrThrow(
                        "mediciones_seguras",
                        null,
                        valores
                    )
                }
            }

            db.execSQL("DROP TABLE mediciones")
            db.execSQL(
                "ALTER TABLE mediciones_seguras RENAME TO mediciones"
            )
        }
    }

    private fun obtenerClave(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore")
        keyStore.load(null)

        val existente = keyStore.getKey(ALIAS, null) as? SecretKey

        if (existente != null) return existente

        val generador = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )

        val parametros = KeyGenParameterSpec.Builder(
            ALIAS,
            KeyProperties.PURPOSE_ENCRYPT or
                    KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setEncryptionPaddings(
                KeyProperties.ENCRYPTION_PADDING_NONE
            )
            .setRandomizedEncryptionRequired(true)
            .build()

        generador.init(parametros)

        return generador.generateKey()
    }

    private fun cifrar(datos: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(Cipher.ENCRYPT_MODE, obtenerClave())

        val cifrado = cipher.doFinal(
            datos.toByteArray(StandardCharsets.UTF_8)
        )

        val resultado = cipher.iv + cifrado

        return Base64.encodeToString(
            resultado,
            Base64.NO_WRAP
        )
    }

    private fun descifrar(datos: String): String {
        val resultado = Base64.decode(datos, Base64.NO_WRAP)

        val iv = resultado.copyOfRange(0, IV_SIZE)
        val cifrado = resultado.copyOfRange(
            IV_SIZE,
            resultado.size
        )

        val cipher = Cipher.getInstance("AES/GCM/NoPadding")

        cipher.init(
            Cipher.DECRYPT_MODE,
            obtenerClave(),
            GCMParameterSpec(128, iv)
        )

        val original = cipher.doFinal(cifrado)

        return String(original, StandardCharsets.UTF_8)
    }

    fun guardarMedicion(
        temperatura: Double,
        humedad: Double,
        luminosidad: Int
    ): Long {
        return try {
            val fecha = System.currentTimeMillis()

            val datos = "$temperatura|$humedad|$luminosidad|$fecha"

            val valores = ContentValues().apply {
                put("datos_cifrados", cifrar(datos))
            }

            writableDatabase.insert(
                "mediciones",
                null,
                valores
            )
        } catch (_: Exception) {
            -1L
        }
    }

    fun obtenerMediciones(): List<Medicion> {
        val lista = mutableListOf<Medicion>()

        val cursor = readableDatabase.query(
            "mediciones",
            arrayOf("id", "datos_cifrados"),
            null, null, null, null,
            "id DESC"
        )

        cursor.use {
            while (it.moveToNext()) {
                try {
                    val id = it.getInt(0)
                    val datos = descifrar(it.getString(1))
                    val partes = datos.split("|")

                    if (partes.size == 4) {
                        lista.add(
                            Medicion(
                                id = id,
                                temperatura = partes[0].toDouble(),
                                humedad = partes[1].toDouble(),
                                luminosidad = partes[2].toInt(),
                                fecha = partes[3].toLong()
                            )
                        )
                    }
                } catch (_: Exception) {
                    // Se omite un registro que no pueda descifrarse.
                }
            }
        }

        return lista
    }
}
