package node2.in_out;

public interface Serializable {
    ChoiceType<?> getType();

    void deserialize(In<?> in);

    void serialize(Out<?> out);
}
