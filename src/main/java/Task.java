import java.time.Instant;
import java.util.Timer;

class Task {
    int priority;
    final User user;
    Instant endTime;
    Timer timer;
    long duration;
    Task(User user,int priority,long duration){
        endTime = Instant.now();
        this.duration = duration;
        endTime = endTime.plusMillis(duration);
        this.user = user;

        if (priority > 3)
            priority = 3;

        if (priority < 0)
            priority = 0;

        this.priority = priority;
    }

    Instant reSetEndTime(long duration){
        endTime = Instant.now();
        this.duration = duration;
        return endTime = endTime.plusMillis(duration);
    }

}
