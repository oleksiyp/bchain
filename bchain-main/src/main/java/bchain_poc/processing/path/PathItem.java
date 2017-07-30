package bchain_poc.processing.path;

import bchain_poc.domain.Hash;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class PathItem {
    public enum Type {
        PUSH, POP
    }

    private final Type type;
    private final Hash hash;
}
