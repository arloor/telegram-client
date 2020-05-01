package com.arloor.telegram.handler;

import com.arloor.telegram.handler.chain.MessageAdminHandler;
import com.arloor.telegram.handler.chain.MessageContentHandler;
import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


public class NewMessageHandler extends BaseHandler<TdApi.UpdateNewMessage> {


    private static final List<BaseHandler<TdApi.UpdateNewMessage>> chain = new ArrayList<>();

    static {
        chain.add(new MessageAdminHandler());
        chain.add(new MessageContentHandler());
    }

    /**
     * 遍历chain
     *
     * @param message
     * @return
     */
    @Override
    public boolean accept(TdApi.UpdateNewMessage message) {
        Iterator<BaseHandler<TdApi.UpdateNewMessage>> iterator = chain.iterator();
        while (iterator.hasNext()) {
            BaseHandler<TdApi.UpdateNewMessage> handler = iterator.next();
            if (handler.accept(message)) {
                break;
            }
        }
        return true;
    }

}
