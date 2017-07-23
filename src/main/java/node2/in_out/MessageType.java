package node2.in_out;

public class MessageType<T extends Serializable> extends ChoiceType<T> {
    public MessageType(String name, Class<T> type) {
        super(name, type);
    }
}
