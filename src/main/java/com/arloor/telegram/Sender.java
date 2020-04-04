package com.arloor.telegram;

import org.drinkless.tdlib.TdApi;

import static com.arloor.telegram.Telegram.defaultHandler;

public class Sender {
    private static final long author= Telegram.getChatId("878823128");
    private static final long group=Telegram.getChatId("-1001334979774");

    public static void sendAuthor(String msg){
        Telegram.sendMessage(author,msg);
    }

    public static void sendGroup(String msg){
        Telegram.sendMessage(group,msg);
    }


    public static void onBoot(){

//                public InputMessageSticker(TdApi.InputFile sticker, TdApi.InputThumbnail thumbnail, int width, int height) {
//            this.sticker = sticker;
//            this.thumbnail = thumbnail;
//            this.width = width;
//            this.height = height;
//        }
        TdApi.InputMessageSticker sticker = new TdApi.InputMessageSticker(new TdApi.InputFileRemote("CAACAgUAAxkBAANjXoiWiurqkSc5BLK1sfO7l1EEoOEAAvAFAAL4xsUK2Geb7lWvkgMYBA"), null, 320,301);
        Telegram.client.send(new TdApi.SendMessage(author, 0, null, null, sticker), defaultHandler);
    }
}
