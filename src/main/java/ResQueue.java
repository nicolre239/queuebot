import org.jetbrains.annotations.NotNull;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.api.objects.ChatMember;

import java.time.Instant;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;


public class ResQueue {

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

    private Bot parentBot = null;

    ResQueue (Bot parentBot){
        this.parentBot = parentBot;
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

    private void fromQueueToResource() {
        Task task = myQueue.poll();
        if (task == null) {
            return;
        }
        myResources.add(task);
        parentBot.sendMsg(task.chatMember.getUser().getId().toString(), "your task on resource");
        task.timer = new Timer();
        task.timer.schedule(new MyTimerTask(task), BotConstants.timeOnResource);
    }

    public void OnResource(Task task) {
        parentBot.sendMsg(task.chatMember.getUser().getId().toString(), "your task finished");
        myResources.remove(task);
        fromQueueToResource();
    }

    @NotNull
    public String getIn(MessageContext ctx) {
        ChatMember chatMember = parentBot.getMember(ctx.user().id(), ctx.chatId());
        if (chatMember == null) {
            return "no member";
        }
        int priority = Integer.parseInt(ctx.arguments()[0]);
        Task task = new Task(chatMember, priority,BotConstants.timeOnResource);
        return takeTask(task);
    }

    @NotNull
    private String takeTask(Task task) {
        if (task == null) {
            return "no task";
        }
        if (myResources.size() < resourcesCount) {
            myResources.add(task);
            task.timer = new Timer();
            task.timer.schedule(new MyTimerTask(task), BotConstants.timeOnResource);
            return "You are on resource";
        }
        else if(task.priority > myResources.peek().priority){
            return changeQR(task);
        }
        myQueue.add(task);
        return "You are on queue";
    }

    public String getFirst(MessageContext ctx) {
        if (myQueue.isEmpty())
            return "Nobody";
        else if (ctx.user().id() == myQueue.peek().chatMember.getUser().getId())
            return "You are on, stupid monkey " + myQueue.peek().priority + " " + myQueue.peek().duration;
        else
            return myQueue.peek().chatMember.getUser().getUserName();
    }

    public String updateResources(MessageContext ctx) {
        int newResourceCount = Integer.parseInt(ctx.arguments()[0]);
        int dif = newResourceCount - resourcesCount;
        resourcesCount = newResourceCount;
        if (dif < 0) {
            while (!myResources.isEmpty() && dif < 0) {
                //returns null if this queue is empty.
                Task task = myResources.poll();
                parentBot.sendMsg(task.chatMember.getUser().getId().toString(), takeTask(task));
                ++dif;
            }
        } else if (dif > 0) {
            while (!myQueue.isEmpty() && dif > 0) {
                --dif;
                fromQueueToResource();
            }
        }
        return "Resources updated";
    }

    private String changeQR(Task task){
        if(myResources.isEmpty()){
            return "empty resources";
        }

        Task tmp = myResources.poll();
        tmp.timer.cancel();
        long dif = tmp.endTime.getEpochSecond();
        dif -= Instant.now().getEpochSecond();
        tmp.reSetEndTime(dif);
        myQueue.add(tmp);
        parentBot.sendMsg(tmp.chatMember.getUser().getId().toString(), "your now is queue, your priority too low");
        return takeTask(task);
    }

}
