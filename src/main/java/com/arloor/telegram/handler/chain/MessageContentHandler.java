package com.arloor.telegram.handler.chain;

import com.arloor.telegram.handler.BaseHandler;
import org.drinkless.tdlib.TdApi;

import java.util.Objects;

/**
 * 处理content类型的message
 */
public class MessageContentHandler extends BaseHandler<TdApi.UpdateNewMessage> {

    @Override
    public boolean accept(TdApi.UpdateNewMessage message) {
        //获取sender User信息
        int senderID = message.message.senderUserId;
        String sender = String.valueOf(senderID);
        TdApi.User sendUser = getOrQueryUser(senderID);
        if (Objects.nonNull(sendUser)) {
            sender = sendUser.firstName + " " + (sendUser.lastName.length() > 0 ? sendUser.lastName : "");
        }
        //获取Chat信息
        long chatId = message.message.chatId;
        String chatName = String.valueOf(chatId);
        TdApi.Chat targetChat = getOrQueryChat(chatId);
        if (Objects.nonNull(targetChat)) {
            chatName = targetChat.title;
        }
        TdApi.MessageContent messageContent = message.message.content;
        if (messageContent instanceof TdApi.MessageText) {
            TdApi.MessageText textMessage = (TdApi.MessageText) messageContent;
            TdApi.FormattedText formattedText = textMessage.text;
            String log = String.format("(%s) @%s (%s)@%s\n%s\n", sender, chatName, senderID, chatId, formattedText.text);
            logger.info("\n" + log); //打印文本
        } else {
            //打印其他类型
//                        System.out.println(JSONObject.toJSON(messageContent));
        }
        return false;
    }
}
