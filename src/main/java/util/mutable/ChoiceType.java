package util.mutable;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class ChoiceType<M extends Mutable<M>> {
    private final String name;
    private final Class<M> type;

    @Override
    public String toString() {
        return name;
    }
}
