package node.discovery;

import node.Message;

public class IntroduceMessage extends Message {
    private final long hash;
    private final long mask;

    public IntroduceMessage() {
        super();
        hash = mask = 0;
    }

    public IntroduceMessage(long hash, long mask) {
        super();
        this.hash = hash;
        this.mask = mask;
    }

    public long getHash() {
        return hash;
    }

    public long getMask() {
        return mask;
    }
}
