package bchain.dao.redis;

import bchain.dao.BlockDao;
import bchain.dao.TxDao;
import bchain.domain.Hash;
import bchain.domain.Tx;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import redis.clients.jedis.Jedis;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.*;

import static bchain.domain.Hash.hash;

@Configuration
@ConditionalOnProperty("redis.port")
public class RedisConfig {
    @Value("${redis.port}")
    private int redisPort;

    @Bean
    public EmbeddedRedis embeddedRedis() {
        return new EmbeddedRedis();
    }

    @Bean
    public RedisConnectionFactory connectionFactory() {
        JedisConnectionFactory factory = new JedisConnectionFactory();
        factory.setPort(redisPort);
        return factory;
    }

    @Bean
    public RedisTemplate<Hash, Tx> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<Hash, Tx> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);
        template.setKeySerializer(new RedisSerializer<Hash>() {
            @Override
            public byte[] serialize(Hash hash) throws SerializationException {
                return hash.getValues();
            }

            @Override
            public Hash deserialize(byte[] bytes) throws SerializationException {
                return hash(bytes);
            }
        });
        template.setValueSerializer(new RedisSerializer<Tx>() {
            @Override
            public byte[] serialize(Tx tx) throws SerializationException {
                try (ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
                     DataOutputStream dataOut = new DataOutputStream(byteOut)) {

                    tx.serialize(dataOut);

                    return byteOut.toByteArray();
                } catch (IOException e) {
                    throw new SerializationException("IO error", e);
                }
            }

            @Override
            public Tx deserialize(byte[] bytes) throws SerializationException {
                if (bytes == null) {
                    return null;
                }
                try (ByteArrayInputStream bIn = new ByteArrayInputStream(bytes);
                     DataInputStream dataIn = new DataInputStream(bIn)){

                    return Tx.deserialize(dataIn);
                } catch (IOException e) {
                    throw new SerializationException("IO error", e);
                }
            }
        });
        return template;
    }


    @Bean
    public TxDao txDao() {
        return new RedisTxDao();
    }

    protected static class EmbeddedRedis {
        private RedisServer redisServer;

        @Value("${redis.port}")
        private int redisPort;

        @PostConstruct
        public void startRedis() throws IOException {
            redisServer = new RedisServer(redisPort);
            redisServer.start();
        }

        @PreDestroy
        public void stopRedis() {
            redisServer.stop();
        }
    }


}
