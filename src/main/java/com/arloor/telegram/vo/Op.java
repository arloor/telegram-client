package com.arloor.telegram.vo;

public class Op {

    public static final String NOBOT = "nobot";
    public static final String ADMIN_PASS = "admin_pass";

    private String op;
    private Integer userId;
    private Long chatId;

    public Op(String op, Integer userId, Long chatId) {
        this.op = op;
        this.userId = userId;
        this.chatId = chatId;
    }

    public String getOp() {
        return op;
    }

    public Integer getUserId() {
        return userId;
    }

    public Long getChatId() {
        return chatId;
    }
}
