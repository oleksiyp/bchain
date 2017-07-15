package node.ledger;

import io.netty.channel.ChannelId;
import node.Message;

public class ProcessMessageEvent {
    private boolean active;
    private Message message;
    private String receiveChannelId;

    public void activate(Message message, String receiveChannelId) {
        this.active = true;
        this.message = message;
        this.receiveChannelId = receiveChannelId;
    }

    public void clear() {
        this.active = false;
        this.message = null;
        this.receiveChannelId = null;
    }

    public boolean isActive() {
        return active;
    }

    public Message getMessage() {
        return message;
    }

    public String getReceiveChannelId() {
        return receiveChannelId;
    }

    public void copyTo(ProcessMessageEvent otherEvent) {
        otherEvent.active = this.active;
        otherEvent.message = this.message;
        otherEvent.receiveChannelId = this.receiveChannelId;
    }

    @Override
    public String toString() {
        return "ProcessMessageEvent{" +
                "activeChoice=" + active +
                ", message=" + message +
                ", receiveChannelId=" + receiveChannelId +
                '}';
    }
}
