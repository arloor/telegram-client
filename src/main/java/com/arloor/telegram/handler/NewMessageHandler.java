package com.arloor.telegram.handler;

import com.arloor.telegram.handler.chain.MessageAdminHandler;
import com.arloor.telegram.handler.chain.MessageContentHandler;
import org.drinkless.tdlib.TdApi;

public class NewMessageHandler extends BaseChainHandler<TdApi.UpdateNewMessage> {

    public NewMessageHandler() {
        chain.add(new MessageAdminHandler());
        chain.add(new MessageContentHandler());
    }
}
