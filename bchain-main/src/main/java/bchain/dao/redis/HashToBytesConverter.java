package bchain.dao.redis;

import bchain.domain.Hash;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

@WritingConverter
public class HashToBytesConverter implements Converter<Hash, byte[]> {

    public static final byte[] ANY_HASH = "*".getBytes();

    @Override
    public byte[] convert(Hash hash) {
        if (hash == null) {
            return ANY_HASH;
        }
        return hash.getValues();
    }
}
