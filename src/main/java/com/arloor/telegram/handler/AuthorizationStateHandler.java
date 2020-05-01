package com.arloor.telegram.handler;

import com.arloor.telegram.Telegram;
import org.drinkless.tdlib.TdApi;

public class AuthorizationStateHandler extends BaseHandler{
    @Override
    public void accept(TdApi.Object object) {
        Telegram.onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
    }
}
