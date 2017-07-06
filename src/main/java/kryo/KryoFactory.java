package kryo;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.serializers.MapSerializer;
import node.Headers;
import node.Message;
import node.discovery.IntroduceMessage;
import node.discovery.JoinRequestMessage;
import node.discovery2.RequestNodeCountMessage;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.function.Consumer;

public class KryoFactory {
    public static Kryo newKryo(Consumer<Kryo> kryoConsumer) {
        Kryo kryo = new Kryo();

        MapSerializer serializer = new MapSerializer();
        kryo.register(HashMap.class, serializer, 10);
        kryo.register(LinkedHashMap.class, serializer, 11);

        kryo.register(Headers.class, 20);
        kryo.register(Headers.HeaderName.class, new HeaderNameSerializer(), 21);
        kryo.register(InetSocketAddress.class, new InetSocketAddressSerializer(), 22);
        kryo.register(Message.class, 23);
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
