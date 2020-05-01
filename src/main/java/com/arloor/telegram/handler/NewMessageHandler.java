package com.arloor.telegram.handler;

import com.arloor.telegram.Telegram;
import org.drinkless.tdlib.TdApi;

import java.util.Arrays;
import java.util.Objects;

import static com.arloor.telegram.Telegram.defaultHandler;
import static com.arloor.telegram.Telegram.getChatId;

public class NewMessageHandler extends BaseHandler{
    @Override
    public void accept(TdApi.Object object) {
        TdApi.UpdateNewMessage message = (TdApi.UpdateNewMessage) object;

        //获取sender User信息
        int senderID = message.message.senderUserId;
        String sender = String.valueOf(senderID);
        TdApi.User sendUser = getOrQueryUser(senderID);
        if (Objects.nonNull(sendUser)) {
            sender = sendUser.firstName + " " + (sendUser.lastName.length() > 0 ? sendUser.lastName : "");
        }
        //获取Chat信息
        long chatId = message.message.chatId;
        String chatName = String.valueOf(chatId);
        TdApi.Chat targetChat = getOrQueryChat(chatId);
        if (Objects.nonNull(targetChat)) {
            chatName = targetChat.title;
        }

        TdApi.MessageContent messageContent = message.message.content;
        if (messageContent instanceof TdApi.MessageText) {
            TdApi.MessageText textMessage = (TdApi.MessageText) messageContent;
            TdApi.FormattedText formattedText = textMessage.text;
            String log = String.format("(%s) @%s (%s)@%s\n%s\n", sender, chatName, senderID, chatId, formattedText.text);
            logger.info("\n" + log); //打印文本
        } else {
            //打印其他类型
//                        System.out.println(JSONObject.toJSON(messageContent));
        }

        if (adminChatId != null && chatId == getChatId(adminChatId)) {//管理群组的消息做特殊处理
            // 本群组的所有消息类型都日志记录
            logger.info(newLine + "(" + sender + ")@" + chatName + " " + senderID + "@" + chatId + newLine + object);
            if (message.message.content instanceof TdApi.MessageChatAddMembers && message.message.date > bootUnixTime) {
                int[] memberUserIds = ((TdApi.MessageChatAddMembers) message.message.content).memberUserIds;
                String finalChatName = chatName;
                Arrays.stream(memberUserIds).forEach(newMemberId -> {

                    String newMemberStr = String.valueOf(newMemberId);
                    TdApi.User newMemberUser = getOrQueryUser(newMemberId);
                    if (Objects.nonNull(newMemberUser)) {
                        newMemberStr = newMemberUser.firstName + " " + (newMemberUser.lastName.length() > 0 ? newMemberUser.lastName : "");
                    }

                    String msg = String.format("欢迎[新朋友](tg://user?id=%s)来到本群组！", newMemberId) + newLine;

                    TdApi.ReplyMarkupInlineKeyboard replyMarkup = null;
                    if (Telegram.me != null && Telegram.me.type instanceof TdApi.UserTypeBot) {//如果是bot，则增加防bot设置
                        TdApi.InlineKeyboardButton[] rowBlogAndGithub = {new TdApi.InlineKeyboardButton("arloor.com", new TdApi.InlineKeyboardButtonTypeUrl("http://arloor.com")), new TdApi.InlineKeyboardButton("Github", new TdApi.InlineKeyboardButtonTypeUrl("https://github.com/arloor"))};
                        TdApi.InlineKeyboardButton[] notBot = {new TdApi.InlineKeyboardButton("我不是机器人", new TdApi.InlineKeyboardButtonTypeCallback(String.format("nobot^%s@%s", newMemberId, chatId).getBytes()))};
                        TdApi.InlineKeyboardButton[] adminPass = {new TdApi.InlineKeyboardButton("PASS[管理员]", new TdApi.InlineKeyboardButtonTypeCallback(String.format("admin_pass^%s@%s", newMemberId, chatId).getBytes()))};
                        replyMarkup = new TdApi.ReplyMarkupInlineKeyboard(new TdApi.InlineKeyboardButton[][]{notBot, rowBlogAndGithub});
                        logger.info(String.format("封禁新加群的%s@%s %s@%s", newMemberStr, finalChatName, newMemberId, chatId)); //打印文本
                        Telegram.client.send(new TdApi.SetChatMemberStatus(chatId, newMemberId, new TdApi.ChatMemberStatusRestricted(true, 0, new TdApi.ChatPermissions(false, false, false, false, false, false, false, false))), defaultHandler);
                        msg += "请点击*我不是机器人*获取发言权限";
                    } else {
                        msg += "博客地址：http://arloor.com" + newLine
                                + "Github：https://github.com/arloor";
                    }

                    TdApi.ReplyMarkupInlineKeyboard finalReplyMarkup = replyMarkup;
                    String finalMsg = msg;
                    Telegram.client.send(new TdApi.ParseTextEntities(msg, new TdApi.TextParseModeMarkdown(2)), (formattedText) -> {
                        if (formattedText instanceof TdApi.FormattedText) {
                            TdApi.InputMessageContent content = new TdApi.InputMessageText((TdApi.FormattedText) formattedText, false, true);
                            Telegram.client.send(new TdApi.SendMessage(chatId, message.message.id, null, finalReplyMarkup, content), defaultHandler);
                        } else {
                            TdApi.InputMessageContent content = new TdApi.InputMessageText(new TdApi.FormattedText(finalMsg, null), false, true);
                            Telegram.client.send(new TdApi.SendMessage(chatId, message.message.id, null, finalReplyMarkup, content), defaultHandler);
                        }
                    });
                });

            }
        }
    }
}
