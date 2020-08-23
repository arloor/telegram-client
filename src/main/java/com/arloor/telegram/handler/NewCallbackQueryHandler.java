package com.arloor.telegram.handler;

import com.alibaba.fastjson.JSONObject;
import com.arloor.telegram.Telegram;
import com.arloor.telegram.handler.base.BaseHandler;
import com.arloor.telegram.vo.Op;
import org.drinkless.tdlib.TdApi;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.arloor.telegram.Telegram.defaultHandler;

public class NewCallbackQueryHandler extends BaseHandler<TdApi.UpdateNewCallbackQuery> {
    @Override
    public boolean accept(TdApi.UpdateNewCallbackQuery newCallbackQuery) {
        //获取sender User信息
        int senderID = newCallbackQuery.senderUserId;
        String sender = String.valueOf(senderID);
        TdApi.User sendUser = getOrQueryUser(senderID);
        if (Objects.nonNull(sendUser)) {
            sender = sendUser.firstName + " " + (sendUser.lastName.length() > 0 ? sendUser.lastName : "");
        }
        //获取Chat信息
        long chatId = newCallbackQuery.chatId;
        String chatName = String.valueOf(chatId);
        TdApi.Chat targetChat = getOrQueryChat(chatId);
        if (Objects.nonNull(targetChat)) {
            chatName = targetChat.title;
        }

        if (newCallbackQuery.payload instanceof TdApi.CallbackQueryPayloadData) {
            byte[] reply = ((TdApi.CallbackQueryPayloadData) newCallbackQuery.payload).data;
            String replyStr = new String(reply);
            Op op = JSONObject.parseObject(replyStr, Op.class);
            Integer opUserId = op.getUserId();
            Long opChatId = op.getChatId();


            if (Op.NOBOT.equals(op.getOp())) {
                boolean done = false;
                if (opChatId == chatId && opUserId == senderID) {
                    logger.info(String.format("解封 %s@%s %s@%s", sender, chatName, senderID, chatId)); //打印文本
                    Telegram.client.send(new TdApi.SetChatMemberStatus(newCallbackQuery.chatId, newCallbackQuery.senderUserId, new TdApi.ChatMemberStatusRestricted(true, 0, new TdApi.ChatPermissions(true, true, false, true, true, false, true, false))), null);
                    Telegram.client.send(new TdApi.AnswerCallbackQuery(newCallbackQuery.id, "您可以自由发言", true, null, 1), null);
                    done = true;
                }
                if (!done) {
                    Telegram.client.send(new TdApi.AnswerCallbackQuery(newCallbackQuery.id, "不要瞎点！", true, null, 1), null);
                }
            } else if (Op.ADMIN_PASS.equals(op.getOp())) {
                Telegram.client.send(new TdApi.GetChatAdministrators(chatId), (result) -> {
                    if (result instanceof TdApi.ChatAdministrators) {
                        TdApi.ChatAdministrators chatAdministrators = (TdApi.ChatAdministrators) result;
                        Set<Integer> adminUserIds = Arrays.stream(chatAdministrators.administrators)
                                .filter(admin -> admin.isOwner)
                                .map(admin -> admin.userId)
                                .collect(Collectors.toSet());
                        if (adminUserIds.contains(senderID)) {
                            Telegram.client.send(new TdApi.SetChatMemberStatus(newCallbackQuery.chatId, opUserId, new TdApi.ChatMemberStatusRestricted(true, 0, new TdApi.ChatPermissions(true, true, false, true, true, false, true, false))), defaultHandler);
                            Telegram.client.send(new TdApi.AnswerCallbackQuery(newCallbackQuery.id, "成功解封" + opUserId, true, null, 1), defaultHandler);
                        } else {
                            Telegram.client.send(new TdApi.AnswerCallbackQuery(newCallbackQuery.id, "再瞎点就报警了！", true, null, 1), defaultHandler);
                        }
                    }
                });
            }
        }
        return true;
    }
}
