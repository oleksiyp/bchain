package bchain.processing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

public class ProcessorBuilder<F, T> {
    private final List<BiFunction<F, T, T>> constructorList;

    public ProcessorBuilder() {
        constructorList = new ArrayList<>();
    }

    public ProcessorBuilder<F, T> add(BiFunction<F, T, T> constructor) {
        constructorList.add(constructor);
        return this;
    }

    public T build(F arg) {
        Collections.reverse(constructorList);
        T res = null;
        for (BiFunction<F, T, T> constructor : constructorList) {
            res = constructor.apply(arg, res);
        }
        Collections.reverse(constructorList);
        return res;
    }
}
