package com.arloor.telegram.handler;

import com.arloor.telegram.handler.base.BaseChainHandler;
import com.arloor.telegram.handler.base.BaseHandler;
import com.arloor.telegram.handler.chain.MessageAdminHandler;
import com.arloor.telegram.handler.chain.MessageContentHandler;
import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.List;

public class NewMessageHandler extends BaseChainHandler<TdApi.UpdateNewMessage> {

    @Override
    public List<BaseHandler<TdApi.UpdateNewMessage>> init() {
        List<BaseHandler<TdApi.UpdateNewMessage>> chain =new ArrayList<>();
        chain.add(new MessageAdminHandler());
        chain.add(new MessageContentHandler());
        return chain;
    }
}
