package bchain.domain;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode
@ToString(includeFieldNames = false)
public class Hash {
    private String str;

    public Hash(String str) {
        this.str = str;
    }

    public static Hash of(String str) {
        return new Hash(str);
    }
}
