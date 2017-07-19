package node.factory;

import io.netty.util.collection.IntObjectHashMap;
import lombok.Getter;
import lombok.Setter;
import util.mutable.ChoiceType;
import util.mutable.Mutable;

import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class Registry<F> {
    private final Map<Integer, ChoiceType<?>> choiceTypes;
    private final Map<Integer, Supplier<Mutable<?>>> constructors;

    private RegistryMapping<?> savedMapping;
    private Class<?> savedClass;

    @Getter@Setter
    private F constructorParam;

    public Registry() {
        choiceTypes = new IntObjectHashMap<>();
        constructors = new IntObjectHashMap<>();
    }

    public <M extends Mutable<M>> Registry<F> register(
            int tag,
            ChoiceType<M> type,
            Supplier<M> constructor) {
        reg(tag, type, (Supplier<Mutable<?>>) constructor);
        return this;
    }

    public <M extends Mutable<M>> Registry<F> register(
            int tag,
            ChoiceType<M> type,
            Function<F, M> constructor) {
        register(tag, type, () -> constructor.apply(constructorParam));
        return this;
    }

    private void reg(int tag,
            ChoiceType<?> type,
            Supplier<Mutable<?>> constructor) {
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

    public <T extends ChoiceType<?>, R extends T> RegistryMapping<R> mapping(Class<T> clazz) {
        if (savedClass == clazz) {
            return (RegistryMapping<R>) savedMapping;
        }

        RegistryMapping<R> res = new RegistryMapping<>(choiceTypes, constructors, (Class<R>) clazz);

        savedMapping = res;
        savedClass = clazz;

        return res;
    }

    public static <T> Registry<T> emptyRegistry() {
        return new Registry<>();
    }
}
