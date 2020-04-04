package com.arloor.telegram;

public class Sender {
    private static final long author= Telegram.getChatId("878823128");
    private static final long group=Telegram.getChatId("-1001334979774");

    public static void sendAuthor(String msg){
        Telegram.sendMessage(author,msg);
    }

    public static void sendGroup(String msg){
        Telegram.sendMessage(group,msg);
    }

}
