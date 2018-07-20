import org.jetbrains.annotations.Nullable;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.EndUser;
import org.telegram.telegrambots.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.ChatMember;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import java.util.*;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.ADMIN;
import static org.telegram.abilitybots.api.objects.Privacy.CREATOR;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;

public class TelegramBot extends AbilityBot implements Bot {

    private final QueueManager queueManager = new QueueManager(this);

    @Nullable
    public synchronized ChatMember getMember(long userId, long chatId) {
        GetChatMember chatMember = new GetChatMember();
        chatMember.setUserId((int) userId);
        chatMember.setChatId(chatId);

        try {
            return execute(chatMember);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public synchronized void sendMsg(long chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(Long.toString(chatId));
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Ability replyToGetInQ() {
        //todo check priority from 1-3
        return Ability
                .builder()
                .name("getInQ")
                .info("Set you on Queue to resource")
                .input(1)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    int priority = Integer.parseInt(ctx.arguments()[0]);
                    silent.send(queueManager.getIn(ctx.user().id(),ctx.chatId(),priority), ctx.chatId());
                })
                .build();
    }

    public Ability replyToUpdResource() {
        //todo check priority from 1-3
        return Ability
                .builder()
                .name("updR")
                .info("Set new resources count")
                .input(1)
                .locality(USER)
                .privacy(ADMIN)
                .action(ctx -> {
                    //todo check arguments
                    long resourceCount = Long.parseLong(ctx.firstArg());
                    silent.send(queueManager.updateResources(resourceCount), ctx.chatId());
                })
                .build();
    }

    public Ability replyToGetFirst() {
        return Ability
                .builder()
                .name("getF")
                .info("Tell who the first on queue now")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.send(queueManager.getFirst(ctx.user().id()), ctx.chatId()))
                .build();
    }

    public Ability replyToAdmins() {
        return Ability
                .builder()
                .name("admins")
                .info("Tell who the admins")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    //TODO make in one message
                    if (admins().isEmpty()) {
                        silent.send("no admins", ctx.chatId());
                    } else {
                        for (Integer item : admins()) {
                            silent.send("id:" + item, ctx.chatId());
                        }
                    }
                })
                .build();
    }

    public Ability replyToUsers() {
        return Ability
                .builder()
                .name("users")
                .info("Show all users")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> {
                    //TODO make in one message
                    for (Map.Entry<Integer, EndUser> item : users().entrySet()) {
                        silent.send("id:" + item.getKey() + " " + item.getValue().firstName(), ctx.chatId());
                    }
                })
                .build();
    }

    public Ability replyToAddAd() {
        return Ability
                .builder()
                .name("addAd")
                .info("Add admin")
                .locality(USER)
                .privacy(CREATOR)
                .input(1)
                .action(ctx -> {
                    if (admins().add(Integer.parseInt(ctx.arguments()[0]))) {
                        silent.send("user with this id  already have admin ", ctx.chatId());
                    } else {
                        silent.send("added", ctx.chatId());
                    }
                })
                .build();
    }

    public Ability replyToRmvAd() {
        return Ability
                .builder()
                .name("rmvAd")
                .info("remove admin")
                .locality(USER)
                .input(1)
                .privacy(CREATOR)
                .action(ctx -> {
                    if (admins().remove(Integer.parseInt(ctx.arguments()[0]))) {
                        silent.send("deleted", ctx.chatId());
                    } else {
                        silent.send("user with this Id is not admin", ctx.chatId());
                    }
                })
                .build();
    }

    TelegramBot(String botToken, String botUsername, DefaultBotOptions botOptions) {
        super(botToken, botUsername, botOptions);
    }

    public int creatorId() {
        return BotConstants.CREATOR_ID;
    }

}
