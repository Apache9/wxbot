package com.github.apache9.wxbot;

/**
 * @author Apache9
 */
public class Message {

    private final String messageId;

    private final String type;

    private final String content;

    private final String member;

    public Message(String messageId, String type, String content, String member) {
        this.messageId = messageId;
        this.type = type;
        this.content = content;
        this.member = member;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getType() {
        return type;
    }

    public String getContent() {
        return content;
    }

    public String getMember() {
        return member;
    }

    @Override
    public String toString() {
        return "Message [messageId=" + messageId + ", type=" + type + ", content=" + content + ", member=" + member
                + "]";
    }
}
