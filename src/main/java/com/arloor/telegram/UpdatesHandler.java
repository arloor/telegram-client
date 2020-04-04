package com.arloor.telegram;

import org.drinkless.tdlib.Client;
import org.drinkless.tdlib.TdApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

import static com.arloor.telegram.Telegram.defaultHandler;
import static com.arloor.telegram.Telegram.getChatId;

public class UpdatesHandler implements Client.ResultHandler {

    private static final ConcurrentMap<Integer, TdApi.User> users = new ConcurrentHashMap<Integer, TdApi.User>();
    private static final ConcurrentMap<Long, TdApi.Chat> chats = new ConcurrentHashMap<Long, TdApi.Chat>();
    private static final String newLine = System.getProperty("line.separator");
    private static final Logger logger = LoggerFactory.getLogger(UpdatesHandler.class);

    private static final String adminChatId = System.getProperty("admin.chat.id");

    private static final int bootUnixTime= (int) (System.currentTimeMillis() / 1000L);

    @Override
    public void onResult(TdApi.Object object) {
        switch (object.getConstructor()) {
            case TdApi.UpdateNewCallbackQuery.CONSTRUCTOR: {
                TdApi.UpdateNewCallbackQuery newCallbackQuery = (TdApi.UpdateNewCallbackQuery) object;
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

                    if (replyStr.startsWith("nobot")) {
                        boolean done = false;
                        String[] split = replyStr.split("\\^");
                        if (split.length == 2) {
                            String userIdAndChatId = split[1];
                            String[] userIdChatIdSplit = userIdAndChatId.split("@");
                            if (userIdChatIdSplit.length == 2) {
                                String replyMarkUserId = userIdChatIdSplit[0];
                                String replyMarkChatId = userIdChatIdSplit[1];
                                if (getChatId(replyMarkChatId) == chatId && Integer.parseInt(replyMarkUserId) == senderID) {
                                    logger.info(String.format("解封 %s@%s %s@%s", sender, chatName, senderID, chatId)); //打印文本
                                    Telegram.client.send(new TdApi.SetChatMemberStatus(newCallbackQuery.chatId, newCallbackQuery.senderUserId, new TdApi.ChatMemberStatusRestricted(true, 0, new TdApi.ChatPermissions(true, true, false, true, true, false, true, false))), defaultHandler);
                                    Telegram.client.send(new TdApi.AnswerCallbackQuery(newCallbackQuery.id, "您可以自由发言", true, null, 1), defaultHandler);
                                    done = true;
                                }
                            }
                        }
                        if (!done) {
                            Telegram.client.send(new TdApi.AnswerCallbackQuery(newCallbackQuery.id, "不要瞎点！", true, null, 1), defaultHandler);
                        }
                    } else if (replyStr.startsWith("admin_pass")) {
                        String[] split = replyStr.split("\\^");
                        if (split.length == 2) {
                            String userIdAndChatId = split[1];
                            String[] userIdChatIdSplit = userIdAndChatId.split("@");
                            if (userIdChatIdSplit.length == 2) {
                                String replyMarkUserId = userIdChatIdSplit[0];
                                String replyMarkChatId = userIdChatIdSplit[1];
                                if (getChatId(replyMarkChatId) == chatId) {
                                    Telegram.client.send(new TdApi.GetChatAdministrators(chatId), (result) -> {
                                        if (result instanceof TdApi.ChatAdministrators) {
                                            TdApi.ChatAdministrators chatAdministrators = (TdApi.ChatAdministrators) result;
                                            Set<Integer> adminUserIds = Arrays.stream(chatAdministrators.administrators)
                                                    .filter(admin -> admin.isOwner)
                                                    .map(admin -> admin.userId)
                                                    .collect(Collectors.toSet());
                                            if (adminUserIds.contains(senderID)) {
                                                Telegram.client.send(new TdApi.SetChatMemberStatus(newCallbackQuery.chatId, Integer.parseInt(replyMarkUserId), new TdApi.ChatMemberStatusRestricted(true, 0, new TdApi.ChatPermissions(true, true, false, true, true, false, true, false))), defaultHandler);
                                                Telegram.client.send(new TdApi.AnswerCallbackQuery(newCallbackQuery.id, "成功解封" + replyMarkUserId, true, null, 1), defaultHandler);
                                            } else {
                                                Telegram.client.send(new TdApi.AnswerCallbackQuery(newCallbackQuery.id, "再瞎点就报警了！", true, null, 1), defaultHandler);
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    }
                }
                break;
            }
            case TdApi.UpdateNewMessage.CONSTRUCTOR: {//todo：接受消息
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
                break;
            }
            case TdApi.UpdateAuthorizationState.CONSTRUCTOR:
                Telegram.onAuthorizationStateUpdated(((TdApi.UpdateAuthorizationState) object).authorizationState);
                break;
            default:
//                     print("Unsupported update:" + newLine + object);
        }
    }

    private static TdApi.User getOrQueryUser(int userID) {
        if (userID == 0) {
            return null;
        }
        TdApi.User sendUser = users.get(userID);
        if (Objects.nonNull(sendUser)) {
            return sendUser;
        } else {
            Telegram.client.send(new TdApi.GetUser(userID), (object) -> {
                if (object instanceof TdApi.Chat) {
                    TdApi.Chat chat = (TdApi.Chat) object;
                    chats.put(chat.id, chat);
                }

            });
            return null;
        }
    }

    private static TdApi.Chat getOrQueryChat(long chatId) {
        TdApi.Chat targetChat = chats.get(chatId);
        if (Objects.isNull(targetChat)) {
            Telegram.client.send(new TdApi.GetChat(chatId), (object)->{
                if (object instanceof TdApi.User) {
                    TdApi.User user = (TdApi.User) object;
                    users.put(user.id, user);
                }
            });
            return null;
        } else {
            return targetChat;
        }
    }
}
