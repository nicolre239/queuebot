import org.apache.commons.codec.digest.MessageDigestAlgorithms;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.ChatMember;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.validation.constraints.Null;
import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;

import static org.telegram.abilitybots.api.objects.Locality.ALL;
import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class Bot extends AbilityBot {

    private int timeOnResource = 10000;
    private int timeRepeatRequest = 15000;
    private ArrayDeque<ChatMember> queue = new ArrayDeque<ChatMember>();

    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            OnResource();
        }
    };
    Timer resourceTimer = new Timer();

    protected Bot (String botToken, String botUsername, DefaultBotOptions botOptions){
        super(botToken, botUsername, botOptions);
    }

    public synchronized void sendMsg(String chatId, String s) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);
        sendMessage.setChatId(chatId);
        sendMessage.setText(s);
        try {
            execute(sendMessage);
        } catch (TelegramApiException e) {

        }
    }

    public synchronized ChatMember getMember (long userId, long chatId){
        GetChatMember chatMember = new GetChatMember();
        chatMember.setUserId((int)userId);
        chatMember.setChatId(chatId);

        try {
            return execute(chatMember);
        } catch (TelegramApiException e) {

        }
        return null;
    }

    private String getFirst (MessageContext ctx){
        if (queue.size() == 0)
            return "Nobody";
        else
            if (ctx.user().id() == queue.getFirst().getUser().getId())
                return "You are on, stupid monkey";
            else
                return queue.getFirst().getUser().getUserName();
    }

    private void OnResource (){
        sendMsg(queue.pollFirst().getUser().getId().toString(), "You are out from resource");

        if (queue.size() != 0) {
            sendMsg(queue.getFirst().getUser().getId().toString(), "You are on resource");
            resourceTimer.cancel();
            resourceTimer = new Timer();

            if (queue.size() > 1) {
                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        OnResource();
                    }
                };
                sendMsg(queue.getFirst().getUser().getId().toString(), "Out from resource in " + (timeOnResource / 1000) + " seconds");
                resourceTimer.schedule(timerTask, timeOnResource);
            }
        }
    }

    private String getIn (MessageContext ctx){


        if ((queue.size() != 0) && ((queue.getFirst().getUser().getId() == ctx.user().id())))
            return "You are on now, stupid monkey";

        if (queue.isEmpty()) {
            queue.addLast(getMember(ctx.user().id(), ctx.chatId()));
            return "You are on resource";
        }
        else {
            queue.addLast(getMember(ctx.user().id(),ctx.chatId()));

            if (queue.size() == 2) {
                sendMsg(queue.getFirst().getUser().getId().toString() , "Out from resource in " + (timeOnResource / 1000) + " seconds");

                timerTask = new TimerTask() {
                    @Override
                    public void run() {
                        OnResource();
                    }
                };
                resourceTimer.schedule(timerTask, timeOnResource);
            }

            return "You are in queue";
        }
    }


    public int creatorId(){
        return 0;
    }

    public String Print(Update update){
        return update.getMessage().getCaption() + " is awesome" ;
    }

    public Reply sayYuckOnImage() {
        // getChatId is a public utility function in rg.telegram.abilitybots.api.util.AbilityUtils
        Consumer<Update> action = upd -> silent.send("Yuck, " + upd.getMessage().getCaption(), getChatId(upd));
        Consumer<Update> action2 = upd -> silent.send(Print(upd), getChatId(upd));

        return Reply.of(action2, Flag.PHOTO);
    }

    public Ability pingPong (){
        return Ability
                .builder()
                .name("ping")
                .info("ping pong")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.send("pong", ctx.chatId()))
                .post(ctx -> silent.send("Bye world!", ctx.chatId()))
                .build();
    }

    public Ability getToQueue (){
        return Ability
                .builder()
                .name("getin")
                .info("Sets you in queue to resource")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.send(getIn(ctx), ctx.chatId()))
                .build();
    }

    public Ability whoIsOn (){
        return Ability
                .builder()
                .name("whoison")
                .info("Tells who is on resource now")
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.send(getFirst(ctx) , ctx.chatId()))
                .build();
    }
}
