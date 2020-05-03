package com.arloor.telegram;

import com.arloor.telegram.handler.*;
import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class UpdatesHandler implements Client.ResultHandler {
    private static final Map<Integer, BaseHandler> lookup = new HashMap<>();

    static {
        lookup.put(TdApi.UpdateNewCallbackQuery.CONSTRUCTOR, new NewCallbackQueryHandler());
        lookup.put(TdApi.UpdateNewMessage.CONSTRUCTOR, new NewMessageHandler());
        lookup.put(TdApi.UpdateAuthorizationState.CONSTRUCTOR, new AuthorizationStateHandler());
        lookup.put(TdApi.UpdateNewInlineQuery.CONSTRUCTOR, new InlineQueryHandler());
    }

    @Override
    public void onResult(TdApi.Object object) {
        Optional.ofNullable(lookup.get(object.getConstructor())).ifPresent(consumer -> {
            consumer.accept(object);
        });
    }


}
