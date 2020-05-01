package com.arloor.telegram;

import org.drinkless.tdlib.TdApi;

import static com.arloor.telegram.Telegram.defaultHandler;

public class Sender {
    private static final long author= Telegram.getChatId(Telegram.CONFIG.getProperty("author"));
    private static final long group=Telegram.getChatId(Telegram.CONFIG.getProperty("mygroup"));

    public static void sendAuthor(String msg){
        Telegram.sendMessage(author,msg);
    }

    public static void sendGroup(String msg){
        Telegram.sendMessage(group,msg);
    }


    public static void onBoot(){
        TdApi.InputMessageSticker sticker = new TdApi.InputMessageSticker(new TdApi.InputFileRemote("CAACAgUAAxkBAANjXoiWiurqkSc5BLK1sfO7l1EEoOEAAvAFAAL4xsUK2Geb7lWvkgMYBA"), null, 320,301);
        Telegram.client.send(new TdApi.SendMessage(author, 0, null, null, sticker), defaultHandler);
    }
}
