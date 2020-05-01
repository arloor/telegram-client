package com.arloor.telegram.handler;


import com.arloor.telegram.Telegram;
import com.arloor.telegram.UpdatesHandler;
import org.drinkless.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public abstract class BaseHandler implements Consumer<TdApi.Object> {

    static final ConcurrentMap<Integer, TdApi.User> users = new ConcurrentHashMap<Integer, TdApi.User>();
    static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    static final String newLine = System.getProperty("line.separator");
    static final Logger logger = LoggerFactory.getLogger(UpdatesHandler.class);

    static final String adminChatId = Telegram.CONFIG.getProperty("mygroup");

    static final int bootUnixTime= (int) (System.currentTimeMillis() / 1000L);

    static TdApi.User getOrQueryUser(int userID) {
        if (userID == 0) {
            return null;
        }
        TdApi.User sendUser = users.get(userID);
        if (Objects.nonNull(sendUser)) {
            return sendUser;
        } else {
            Telegram.client.send(new TdApi.GetUser(userID), (object) -> {
                if (object instanceof TdApi.Chat) {
                    TdApi.Chat chat = (TdApi.Chat) object;
                    chats.put(chat.id, chat);
                }
            });
            return null;
        }
    }

    static TdApi.Chat getOrQueryChat(long chatId) {
        TdApi.Chat targetChat = chats.get(chatId);
        if (Objects.isNull(targetChat)) {
            Telegram.client.send(new TdApi.GetChat(chatId), (object)->{
                if (object instanceof TdApi.User) {
                    TdApi.User user = (TdApi.User) object;
                    users.put(user.id, user);
                }
            });
            return null;
        } else {
            return targetChat;
        }
    }
}
