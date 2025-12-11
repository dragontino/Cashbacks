package com.cashbacks.core.database.datastore.model

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import kotlinx.serialization.json.encodeToStream
import java.io.InputStream
import java.io.OutputStream

@Serializable
data class LoginCredentialsDto(
    val password: String
)


@OptIn(ExperimentalSerializationApi::class)
internal object LoginCredentialsSerializer : Serializer<LoginCredentialsDto> {
    override val defaultValue = LoginCredentialsDto(password = "")

    override suspend fun readFrom(input: InputStream): LoginCredentialsDto {
        try {
            return Json.decodeFromStream(input)
        } catch (e: SerializationException) {
            throw CorruptionException("Unable to read LoginCredentialsDto", e)
        }
    }

    override suspend fun writeTo(t: LoginCredentialsDto, output: OutputStream) {
        Json.encodeToStream(t, output)
    }
}
