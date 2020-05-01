package com.arloor.telegram;

import com.arloor.telegram.handler.AuthorizationStateHandler;
import com.arloor.telegram.handler.BaseHandler;
import com.arloor.telegram.handler.NewCallbackQueryHandler;
import com.arloor.telegram.handler.NewMessageHandler;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Handler;
import java.util.stream.Collectors;

import static com.arloor.telegram.Telegram.defaultHandler;
import static com.arloor.telegram.Telegram.getChatId;

public class UpdatesHandler implements Client.ResultHandler {
    private static final Map<Integer, BaseHandler> lookup=new HashMap<>();

    static {
        lookup.put(TdApi.UpdateNewCallbackQuery.CONSTRUCTOR, new NewCallbackQueryHandler());
        lookup.put(TdApi.UpdateNewMessage.CONSTRUCTOR, new NewMessageHandler());
        lookup.put(TdApi.UpdateAuthorizationState.CONSTRUCTOR, new AuthorizationStateHandler());
    }

    @Override
    public void onResult(TdApi.Object object) {
        Optional.ofNullable(lookup.get(object.getConstructor())).ifPresent(consumer->{
            consumer.accept(object);
        });
    }


}
