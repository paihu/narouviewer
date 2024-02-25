package dev.paihu.narou_viewer.datastore

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

class AppStateSerializer : Serializer<AppState> {
    override val defaultValue = AppState.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): AppState =
        try {
            // readFrom is already called on the data store background thread
            AppState.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }

    override suspend fun writeTo(t: AppState, output: OutputStream) {
        // writeTo is already called on the data store background thread
        t.writeTo(output)
    }
}
