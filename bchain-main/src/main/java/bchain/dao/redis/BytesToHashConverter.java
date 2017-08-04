package bchain.dao.redis;

import bchain.domain.Hash;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;

import static bchain.domain.Hash.hash;

@WritingConverter
public class BytesToHashConverter implements Converter<byte[], Hash> {
    @Override
    public Hash convert(byte[] bytes) {
        return hash(bytes);
    }
}
