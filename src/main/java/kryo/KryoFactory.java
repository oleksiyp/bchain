package kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import node.Headers;
import node.Message;
import node.discovery.IntroduceMessage;
import node.discovery.JoinRequestMessage;
import node.discovery2.RequestNodeCountMessage;

import java.lang.reflect.Constructor;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class KryoFactory {
    public static Kryo newKryo(Consumer<Kryo> kryoConsumer, final KryoObjectPool pool) {

        Kryo kryo = new Kryo() {
            @Override
            public Registration register(Class type, int id) {

                Registration registration = super.register(type, id);
                Constructor constructor;
                try {
                    constructor = type.getConstructor();
                } catch (NoSuchMethodException e) {
                    throw new RuntimeException(e);
                }
                Supplier<Object> factory = () -> {
                    try {
                        return constructor.newInstance();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };

                registration.setInstantiator(pool.newInstantiator(
                                type,
                                factory,
                                128 * 1024));
                return registration;
            }
        };

        MapSerializer serializer = new MapSerializer();
        kryo.register(HashMap.class, serializer, 10)
            .setInstantiator(pool.newInstantiator(
                HashMap.class, HashMap::new,
                128 * 1024));

        kryo.register(LinkedHashMap.class, serializer, 11);

        kryo.register(Headers.class, 20)
            .setInstantiator(pool.newInstantiator(
                    Headers.class,
                    Headers::new,
                    128 * 1024));

        kryo.register(Headers.HeaderName.class, new HeaderNameSerializer(), 21);
        kryo.register(InetSocketAddress.class, new InetSocketAddressSerializer(), 22);
        kryo.register(Message.class, 23)
                .setInstantiator(pool.newInstantiator(
                        Message.class,
                        Message::new,
                        128 * 1024));

        kryo.register(IntroduceMessage.class, 24);
        kryo.register(JoinRequestMessage.class, 25);
        kryo.register(RequestNodeCountMessage.class, 26);

        if (kryoConsumer != null) {
            kryoConsumer.accept(kryo);
        }

        kryo.setRegistrationRequired(true);

        return kryo;
    }

}
