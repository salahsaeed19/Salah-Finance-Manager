package com.salahabusaif.financemanager.core.data.preferences

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.salahabusaif.financemanager.core.data.proto.StoredAppPreferences
import java.io.InputStream
import java.io.OutputStream

object AppPreferencesSerializer : Serializer<StoredAppPreferences> {
    override val defaultValue: StoredAppPreferences = StoredAppPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): StoredAppPreferences = try {
        StoredAppPreferences.parseFrom(input)
    } catch (exception: InvalidProtocolBufferException) {
        throw CorruptionException("Cannot read app preferences.", exception)
    }

    override suspend fun writeTo(t: StoredAppPreferences, output: OutputStream) = t.writeTo(output)
}
