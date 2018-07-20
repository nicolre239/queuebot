import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.util.Comparator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.PriorityBlockingQueue;


class QueueManager {
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

    @NotNull
    String getIn(long userId, long chatId, int priority) {
        User user = new User(userId,chatId);
        Task task = new Task(user, priority,BotConstants.timeOnResource);
        return takeTask(task);
    }

    String getFirst(long userId) {
        if (myQueue.isEmpty())
            return "Nobody";
        else if (userId == myQueue.peek().user.userId)
            return "You are on, stupid monkey " + myQueue.peek().priority + " " + myQueue.peek().duration;
        else
            return myQueue.peek().user.getUserName();
    }

    String updateResources(long newResourceCount) {
        long dif = newResourceCount - resourcesCount;
        resourcesCount = newResourceCount;
        if (dif < 0) {
            while (!myResources.isEmpty() && dif < 0) {
                //returns null if this queue is empty, but upper we check: empty or not
                Task taskFromRes = myResources.poll();
                bot.sendMsg(taskFromRes.user.userId, takeTask(taskFromRes));
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

    @NotNull
    private String takeTask(Task task) {
        if (task == null) {
            return "no task ";
        }
        if(resourcesCount == 0){
            return "0 resources";
        }
        if (myResources.size() < resourcesCount) {
            myResources.add(task);
            task.timer = new Timer();
            task.timer.schedule(new MyTimerTask(task), BotConstants.timeOnResource);
            return "You are on resource";
        }
        else if(task.priority > myResources.peek().priority){
            return changeQtoR(task);
        }
        myQueue.add(task);
        return "You are on queue";
    }

    QueueManager(TelegramBot bot){
        this.bot = bot;
    }

    private long resourcesCount = BotConstants.STANDART_RESOURCES_COUNT;
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
    private void fromQueueToResource() {
        Task task = myQueue.poll();
        if (task == null) {
            return;
        }
        myResources.add(task);
        bot.sendMsg(task.user.userId, "your task on resource");
        task.timer = new Timer();
        task.timer.schedule(new MyTimerTask(task), BotConstants.timeOnResource);
    }

    private void OnResource(Task task) {
        bot.sendMsg(task.user.userId, "your task finished");
        myResources.remove(task);
        fromQueueToResource();
    }

    private String changeQtoR(Task task){
        if(myResources.isEmpty()){
            return "empty resources";
        }

        Task taskFromResource = myResources.poll();
        taskFromResource.timer.cancel();
        long dif = taskFromResource.endTime.getEpochSecond();
        dif -= Instant.now().getEpochSecond();
        taskFromResource.reSetEndTime(dif);
        myQueue.add(taskFromResource);
        bot.sendMsg(taskFromResource.user.userId, "your now is queue, your priority too low");
        return takeTask(task);
    }

    //is a threat sage and can't add null & not-comparable object
    private PriorityBlockingQueue<Task> myQueue = new PriorityBlockingQueue<>(10, maxComparator);
    private PriorityBlockingQueue<Task> myResources = new PriorityBlockingQueue<>(2, minComparator);
    private Bot bot;

}
