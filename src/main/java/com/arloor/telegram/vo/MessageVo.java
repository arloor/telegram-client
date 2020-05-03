package com.arloor.telegram.vo;

import java.util.Date;

public class MessageVo {
    private int senderId;
    private long chatId;
    private long id;
    private Date time;
    private String content;


    public MessageVo(int senderId, long chatId, long id, Date time, String content) {
        this.senderId = senderId;
        this.chatId = chatId;
        this.id = id;
        this.time = time;
        this.content = content;
    }

    public int getSenderId() {
        return senderId;
    }

    public long getChatId() {
        return chatId;
    }

    public long getId() {
        return id;
    }

    public Date getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }
}
