package com.arloor.telegram.handler;

import org.drinkless.tdlib.TdApi;

public class InlineQueryHandler extends BaseHandler<TdApi.UpdateNewInlineQuery> {
    @Override
    public boolean accept(TdApi.UpdateNewInlineQuery object) {
        System.out.println(object.query);
        return false;
    }
}
