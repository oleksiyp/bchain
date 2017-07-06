package node;

import java.io.Serializable;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static node.Headers.HeaderName.register;

public class Headers implements Serializable {
    public static final HeaderName<Long> ID = register(1, "ID", Long.class);
    public static final HeaderName<Long> TIMESTAMP = register(2, "TIMESTAMP", Long.class);
    public static final HeaderName<InetSocketAddress> ORIGINATOR = register(3, "ORIGINATOR", InetSocketAddress.class);
    public static final HeaderName<InetSocketAddress> SENDER = register(4, "SENDER", InetSocketAddress.class);
    public static final HeaderName<Long> ROUTE_BACK_ID = register(5, "ROUTE_BACK_ID", Long.class);
    public static final HeaderName<InetSocketAddress> ROUTE_BACK_TARGET = register(6, "ROUTE_BACK_TARGET", InetSocketAddress.class);

    private long id;
    private long timestamp;
    private InetSocketAddress originator;
    private InetSocketAddress sender;

    private Map<HeaderName<?>, Object> map;

    public Headers() {
        map = new HashMap<>();
    }

    public <T> void set(HeaderName<? super T> name, T value) {
        if (ID.equals(name)) {
            id = (Long) value;
        } else if (TIMESTAMP.equals(name)) {
            timestamp = (Long) value;
        } else if (ORIGINATOR.equals(name)) {
            originator = (InetSocketAddress) value;
        } else if (SENDER.equals(name)) {
            sender = (InetSocketAddress) value;
        } else {
            map.put(name, value);
        }
    }


    @SuppressWarnings("unchecked")
    public <T> T get(HeaderName<T> name) {
        if (ID.equals(name)) {
            return (T) (Long) id;
        } else if (TIMESTAMP.equals(name)) {
            return (T) (Long) timestamp;
        } else if (ORIGINATOR.equals(name)) {
            return (T) originator;
        } else if (SENDER.equals(name)) {
            return (T) sender;
        } else {
            return (T) map.get(name);
        }
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public InetSocketAddress getOriginator() {
        return originator;
    }

    public void setOriginator(InetSocketAddress originator) {
        this.originator = originator;
    }

    public InetSocketAddress getSender() {
        return sender;
    }

    public void setSender(InetSocketAddress sender) {
        this.sender = sender;
    }

    public Set<HeaderName<?>> allNames() {
        Set<HeaderName<?>> names = new HashSet<>(map.keySet());
        names.add(ORIGINATOR);
        names.add(SENDER);
        names.add(ID);
        names.add(TIMESTAMP);
        return names;
    }

    public boolean isRouteBack() {
        if (this.get(Headers.ROUTE_BACK_ID) == null) {
            return false;
        }
        if (this.get(Headers.ROUTE_BACK_TARGET) == null) {
            return false;
        }
        return true;
    }

    public static class HeaderName<T> implements Serializable {
        private final int tag;
        private final String name;
        private final Class<T> clazz;
        public static final Map<Integer, HeaderName<?>> ALL_HEADERS = new HashMap<>();

        public static <T> HeaderName<T> register(int tag, String id, Class<T> clazz) {
            HeaderName<T> headerName = new HeaderName<>(tag, id, clazz);
            if (ALL_HEADERS.put(tag, headerName) != null) {
                throw new RuntimeException("Tag " + tag + " already exist: " + headerName);
            }
            return headerName;
        }

        private HeaderName(int tag, String name, Class<T> clazz) {
            this.tag = tag;
            this.name = name;
            this.clazz = clazz;
        }

        public int getTag() {
            return tag;
        }

        public Class<T> getClazz() {
            return clazz;
        }

        public String getName() {
            return name;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            HeaderName<?> that = (HeaderName<?>) o;

            return name.equals(that.name);
        }

        @Override
        public int hashCode() {
            return name.hashCode();
        }

        @Override
        public String toString() {
            return name;
        }
    }

    @Override
    public String toString() {
        return "{" + allNames()
                .stream()
                .map(name -> name + "=" + get(name))
                .collect(Collectors.joining(", ")) + "}";
    }
}
