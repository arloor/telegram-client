package com.arloor.telegram.handler;

import com.arloor.telegram.Telegram;
import org.drinkless.tdlib.TdApi;

public class AuthorizationStateHandler extends BaseHandler<TdApi.UpdateAuthorizationState> {
    @Override
    public boolean accept(TdApi.UpdateAuthorizationState object) {
        Telegram.onAuthorizationStateUpdated((object).authorizationState);
        return true;
    }
}
