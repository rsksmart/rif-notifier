package org.rif.notifier.models.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.math.BigInteger;

public class BigIntegerSerializer extends StdSerializer<BigInteger> {

    public BigIntegerSerializer()   {
        super(BigInteger.class, false);
    }
    public BigIntegerSerializer(Class<BigInteger> cls)   {
        super(cls);
    }

    @Override
    public void serialize(BigInteger bigInteger, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
       jsonGenerator.writeString(bigInteger.toString()) ;
    }
}
