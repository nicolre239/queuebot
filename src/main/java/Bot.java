import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.ChatMember;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;


public class Bot extends AbilityBot {

    class  myTimerTask extends TimerTask {
        @Override
        public void run() {
            OnResource();
        }
    }

    private synchronized ChatMember getMember(long userId, long chatId) {
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

    private String getIn(MessageContext ctx) {
        if (myQueue.isEmpty()) {
            myQueue.addLast(getMember(ctx.user().id(), ctx.chatId()));
            return "You are on resource";
        } else if ((myQueue.getFirst().getUser().getId() == ctx.user().id())) {
            return "You are on now, stupid monkey";
        } else {
            myQueue.addLast(getMember(ctx.user().id(), ctx.chatId()));
            if (myQueue.size() == BotConstants.MAX_QUEUE_SIZE) {
                sendMsg(myQueue.getFirst().getUser().getId().toString(), "Out from resource in " + (BotConstants.timeOnResource / 1000) + " seconds");
                myResourceTimer.schedule(new myTimerTask(), BotConstants.timeOnResource);
            }

            return "You are in Queue";
        }
    }

    private void OnResource() {
        sendMsg(myQueue.pollFirst().getUser().getId().toString(), "You are out from resource");
        if (myQueue.size() != 0) {
            sendMsg(myQueue.getFirst().getUser().getId().toString(), "You are on resource");
            myResourceTimer.cancel();
            myResourceTimer = new Timer();
            if (myQueue.size() > 1) {
                sendMsg(myQueue.getFirst().getUser().getId().toString(), "Out from resource in " + (BotConstants.timeOnResource / 1000) + " seconds");
                myResourceTimer.schedule(new myTimerTask(), BotConstants.timeOnResource);
            }
        }
    }

    private String getFirst(MessageContext ctx) {
        if (myQueue.size() == 0)
            return "Nobody";
        else if (ctx.user().id() == myQueue.getFirst().getUser().getId())
            return "You are on, stupid monkey";
        else
            return myQueue.getFirst().getUser().getUserName();
    }

    private synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    public Ability getIn() {
        return Ability
                .builder()
                .name("getin")
                .info("Sets you in myQueue to resource")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.send(getIn(ctx), ctx.chatId()))
                .build();
    }

    public Ability whoIsOn() {
        return Ability
                .builder()
                .name("whoison")
                .info("Tells who is on resource now")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.send(getFirst(ctx), ctx.chatId()))
                .build();
    }

    public Bot(String botToken, String botUsername, DefaultBotOptions botOptions) {
        super(botToken, botUsername, botOptions);
    }

    public int creatorId() {
        return 0;
    }


    private Timer myResourceTimer = new Timer();
    private ArrayDeque<ChatMember> myQueue = new ArrayDeque<ChatMember>();
}
