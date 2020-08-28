package com.n2.util

import org.apache.avro.generic.GenericDatumWriter
import org.apache.avro.generic.GenericRecord
import org.apache.avro.io.DatumWriter
import org.apache.avro.io.EncoderFactory
import org.apache.avro.specific.SpecificRecordBase
import org.apache.kafka.common.errors.SerializationException
import org.apache.kafka.common.serialization.Serializer
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.IOException

class AvroSerializer<T : SpecificRecordBase?> : Serializer<T?> {
    override fun close() {
        //do nothing
    }

    override fun configure(arg0: Map<String?, *>?, arg1: Boolean) {
       // do nothing
    }

    override fun serialize(topic: String, data: T?): ByteArray {
        if (data == null) {
            return ByteArray(0)
        }
        return try {
                LOGGER.debug("data='{}'", data)
                val byteArrayOutputStream = ByteArrayOutputStream()
                val binaryEncoder = EncoderFactory.get().binaryEncoder(byteArrayOutputStream, null)
                val datumWriter: DatumWriter<GenericRecord> = GenericDatumWriter(data?.schema)
                datumWriter.write(data, binaryEncoder)
                binaryEncoder.flush()
                byteArrayOutputStream.close()
                byteArrayOutputStream.toByteArray()
        } catch (ex: IOException) {
            throw SerializationException(
                    "Can't serialize data='$data' for topic='$topic'", ex)
        }
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(AvroSerializer::class.java)
    }
}