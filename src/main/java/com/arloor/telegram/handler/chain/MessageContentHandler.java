package com.arloor.telegram.handler.chain;

import com.alibaba.fastjson.JSONObject;
import com.arloor.telegram.handler.BaseHandler;
import com.arloor.telegram.vo.MessageVo;
import org.drinkless.tdlib.TdApi;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;

import java.io.IOException;
import java.util.Date;
import java.util.Objects;

import static com.arloor.telegram.Telegram.es;

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
            Date time = new Date(message.message.date);
            MessageVo messageVo = new MessageVo(senderID, chatId, message.message.id, time, formattedText.text);
            IndexRequest request = new IndexRequest(
                    "telegram",
                    "_doc",
                    chatId + "_" + message.message.id);
            String jsonString = JSONObject.toJSONString(messageVo);
            request.source(jsonString, XContentType.JSON);
            try {
                IndexResponse indexResponse = es.index(request, RequestOptions.DEFAULT);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            //打印其他类型
//                        System.out.println(JSONObject.toJSON(messageContent));
        }
        return false;
    }
}
