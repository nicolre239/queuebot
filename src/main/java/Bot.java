import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.api.methods.groupadministration.GetChatMember;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.ChatMember;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.exceptions.TelegramApiException;

import javax.naming.Context;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;


public class Bot extends AbilityBot {
    class MyTimerTask extends TimerTask {
        final Task task;

        MyTimerTask(Task task) {
            this.task = task;
        }

        @Override
        public void run() {
            OnResource(task);
        }
    }

    private int resourcesCount = BotConstants.STANDART_RESOURCES_COUNT;
    private static Comparator<Task> maxComparator = new Comparator<Task>() {
        @Override
        public int compare(Task t1, Task t2) {
            return t1.priority - t2.priority;
        }
    };
    private static Comparator<Task> minComparator = new Comparator<Task>() {
        @Override
        public int compare(Task t1, Task t2) {
            return t2.priority - t1.priority;
        }
    };
    //is a threat sage and can't add null & not-comparable object
    private PriorityBlockingQueue<Task> myQueue = new PriorityBlockingQueue<>(10, maxComparator);
    private PriorityBlockingQueue<Task> myResources = new PriorityBlockingQueue<>(2, minComparator);

    @Nullable
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

    private void fromQueueToResource(){
        Task task = myQueue.poll();
        if(task == null){
            return;
        }
        myResources.add(task);
        sendMsg(task.chatMember.getUser().getId().toString(), "your task on resource");
        Timer timer = new Timer();
        timer.schedule(new MyTimerTask(task), BotConstants.timeOnResource);
    }

    private void OnResource(Task task) {
        sendMsg(task.chatMember.getUser().getId().toString(), "your task finished");
        myResources.remove(task);
        fromQueueToResource();
    }

    @NotNull
    private String takeTask(Task task) {
        if(task == null){
            return "no task";
        }
        if (myResources.size() < resourcesCount) {
            myResources.add(task);
            Timer myTimer = new Timer();
            myTimer.schedule(new MyTimerTask(task), BotConstants.timeOnResource);
            return "You are on resource";
        }
        myQueue.add(task);
        return "You are on queue";
    }

    @NotNull
    private String getIn(MessageContext ctx) {
        ChatMember chatMember = getMember(ctx.user().id(), ctx.chatId());
        if (chatMember == null) {
            return "no member";
        }
        int priority = Integer.parseInt(ctx.arguments()[0]);
        Task task = new Task(chatMember, priority);
        return takeTask(task);
    }

    private String getFirst(MessageContext ctx) {
        if (myQueue.isEmpty())
            return "Nobody";
        else if (ctx.user().id() == myQueue.peek().chatMember.getUser().getId())
            return "You are on, stupid monkey";
        else
            return myQueue.peek().chatMember.getUser().getUserName();
    }

    private String updateResources(MessageContext ctx){
        int newResourceCount = Integer.parseInt(ctx.arguments()[0]);
        int dif = newResourceCount - resourcesCount;
        resourcesCount = newResourceCount;
       if(dif < 0){
           while (!myResources.isEmpty() && dif < 0){
               //returns null if this queue is empty.
               Task task = myResources.poll();
               sendMsg(task.chatMember.getUser().getId().toString(),takeTask(task));
               ++dif;
           }
       }else if(dif > 0){
           while(!myQueue.isEmpty() && dif > 0){
               --dif;
               fromQueueToResource();
           }
       }
       return "Resources update";
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
                .action(ctx -> silent.send(getIn(ctx), ctx.chatId()))
                .build();
    }

    public Ability replyToupdR() {
        //todo check priority from 1-3
        return Ability
                .builder()
                .name("updR")
                .info("Set new resources count")
                .input(1)
                .locality(USER)
                .privacy(PUBLIC)
                .action(ctx -> silent.send(updateResources(ctx), ctx.chatId()))
                .build();
    }

    public Ability replyToGetFirst() {
        return Ability
                .builder()
                .name("getF")
                .info("Tell who the first on queue now")
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
}
