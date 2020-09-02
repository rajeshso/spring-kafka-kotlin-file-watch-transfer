package com.n2.util

import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumReader
import org.apache.avro.io.Decoder
import org.apache.avro.io.DecoderFactory
import org.apache.avro.specific.SpecificDatumReader
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Deserializer
import org.slf4j.LoggerFactory
import java.util.*
import javax.xml.bind.DatatypeConverter
//NOTE: Refer this link for more details http://avro.apache.org/docs/current/spec.html

class AvroDeserializer<T : SpecificRecordBase?>(val targetType: Class<T>) : Deserializer<T?> {
    override fun close() {
        // do nothing
    }

    override fun configure(arg0: Map<String?, *>?, arg1: Boolean) {
        // do nothing
    }

    override fun deserialize(topic: String, data: ByteArray): T? {
        if (data == null) return null
        return try {
                LOGGER.debug("data='{}'", DatatypeConverter.printHexBinary(data))
                val datumReader: DatumReader<GenericRecord?> = SpecificDatumReader(targetType.getDeclaredConstructor().newInstance()?.schema)
                val decoder: Decoder = DecoderFactory.get().binaryDecoder(data, null)
                val result = datumReader.read(null, decoder) as T
                LOGGER.debug("deserialized data='{}'", result)
                return result
        } catch (ex: Exception) {
            throw SerializationException(
                    "Can't deserialize data '" + Arrays.toString(data) + "' from topic '" + topic + "'", ex)
        }
    }
    companion object {
        private val LOGGER = LoggerFactory.getLogger(AvroDeserializer::class.java)
    }

}