package com.arloor.telegram.handler.base;

import org.drinkless.tdlib.TdApi;

import java.util.Iterator;
import java.util.List;

public abstract class BaseChainHandler<T extends TdApi.Object> extends BaseHandler<T> {

    private List<BaseHandler<T>> chain;


    public BaseChainHandler() {
        this.chain = init();
    }

    public abstract List<BaseHandler<T>>  init();

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
