package com.arloor.telegram.handler;

import org.drinkless.tdlib.TdApi;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class BaseChainHandler<T extends TdApi.Object> extends BaseHandler<T> {

    List<BaseHandler<T>> chain = new ArrayList<>();

    @Override
    public boolean accept(T object) {
        Iterator<BaseHandler<T>> iterator = chain.iterator();
        while (iterator.hasNext()) {
            BaseHandler<T> handler = iterator.next();
            if (handler.accept(object)) {
                break;
            }
        }
        return true;
    }
}
