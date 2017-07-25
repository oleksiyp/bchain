package node2.registry;

import io.netty.util.collection.IntObjectHashMap;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Registry<F> {
    private final Map<Integer, RegistryItem<?>> choiceTypes;
    private final Map<Integer, Supplier<?>> constructors;

    private RegistryMapping<?, ?> savedMapping;
    private Class<?> savedClass;

    @Getter
    @Setter
    private F constructorParam;

    public Registry() {
        choiceTypes = new IntObjectHashMap<>();
        constructors = new IntObjectHashMap<>();
    }

    public <M> Registry<F> register(
            int tag,
            RegistryItem<M> type,
            Supplier<M> constructor) {
        reg(tag, type, constructor);
        return this;
    }

    public <M> Registry<F> register(
            int tag,
            RegistryItem<M> type,
            Function<F, M> constructor) {
        register(tag, type, () -> constructor.apply(constructorParam));
        return this;
    }

    private void reg(int tag,
                     RegistryItem<?> type,
                     Supplier<?> constructor) {
        if (choiceTypes.containsKey(tag) ||
                choiceTypes.put(tag, type) != null) {
            throw new RuntimeException("Tag " + tag + " already exist: " + type);
        }

        constructors.put(tag, constructor);

        clearSavedMapping();
    }

    public Registry<F> merge(int offset, Registry<F> registry) {
        registry.choiceTypes.forEach((tag, type) -> {
            reg(tag + offset, type, registry.constructors.get(tag));
        });

        return this;
    }

    private void clearSavedMapping() {
        savedMapping = null;
        savedClass = null;
    }

    @SuppressWarnings("unchecked")
    public <T extends RegistryItem<C>, C>
    RegistryMapping<T, C> mapping(Class<?> clazz) {
        if (savedClass == clazz) {
            return (RegistryMapping<T, C>) savedMapping;
        }

        RegistryMapping<T, C> res = new RegistryMapping<>(
                choiceTypes,
                constructors,
                (Class<T>) clazz);

        savedMapping = res;
        savedClass = clazz;

        return res;
    }

    public static <T> Registry<T> emptyRegistry() {
        return new Registry<>();
    }
}
