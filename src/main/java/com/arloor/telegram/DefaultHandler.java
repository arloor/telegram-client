package com.arloor.telegram;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;

public class DefaultHandler implements Client.ResultHandler {
    @Override
    public void onResult(TdApi.Object object) {
        System.out.println(object.toString());
    }
}
