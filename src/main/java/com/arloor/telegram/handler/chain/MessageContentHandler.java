package com.arloor.telegram.handler.chain;

import com.alibaba.fastjson.JSONObject;
import com.arloor.telegram.Telegram;
import com.arloor.telegram.handler.BaseHandler;
import com.arloor.telegram.vo.MessageVo;
import org.drinkless.tdlib.TdApi;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.script.ScriptType;
import org.elasticsearch.script.mustache.SearchTemplateRequest;
import org.elasticsearch.script.mustache.SearchTemplateResponse;
import org.elasticsearch.search.SearchHit;

import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.arloor.telegram.Telegram.client;
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

            if (message.message.chatId > 0 && System.currentTimeMillis() > bootUnixTime) {
                String query = formattedText.text;
                if (query.length() != 0) {
                    SearchTemplateRequest request = new SearchTemplateRequest();
                    request.setRequest(new SearchRequest("telegram"));

                    request.setScriptType(ScriptType.INLINE);
                    request.setScript(
                            "{\"from\":0,\"size\":100,\"min_score\":16,\"query\":{\"function_score\":{\"query\":{\"match_all\":{}},\"boost_mode\":\"replace\",\"functions\":[{\"script_score\":{\"script\":{\"source\":\"term_score\",\"lang\":\"expert_scripts\",\"params\":{\"field\":[\"content^1\"],\"query\":\"{{query}}\"}}}}]}}}");

                    Map<String, Object> scriptParams = new HashMap<>();
                    scriptParams.put("query", query);
                    request.setScriptParams(scriptParams);
                    try {
                        SearchTemplateResponse response = es.searchTemplate(request, RequestOptions.DEFAULT);
                        SearchHit[] hits = response.getResponse().getHits().getHits();
                        if (hits.length != 0) {
                            TdApi.InputMessageSticker sticker = new TdApi.InputMessageSticker(new TdApi.InputFileRemote("CAACAgUAAxkBAANjXoiWiurqkSc5BLK1sfO7l1EEoOEAAvAFAAL4xsUK2Geb7lWvkgMYBA"), null, 320, 301);
                            Telegram.client.send(new TdApi.SendMessage(senderID, 0, null, null, sticker), result -> {
                                for (int i = 0; i < hits.length; i++) {
                                    Map<String, Object> doc = hits[i].getSourceAsMap();
                                    System.out.println(JSONObject.toJSONString(doc));
                                    client.send(new TdApi.ForwardMessages(senderID, (Long) doc.get("chatId"), new long[]{Long.parseLong(String.valueOf(doc.get("id")))}, null, false, false, false), null);
                                }
                            });
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                return true;
            }

            if (message.message.forwardInfo == null && message.message.chatId < 0) {
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
            }

        } else {
            //打印其他类型
//                        System.out.println(JSONObject.toJSON(messageContent));
        }
        return false;
    }
}
