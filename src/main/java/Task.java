import org.telegram.telegrambots.api.objects.ChatMember;

import java.time.Instant;
import java.util.Timer;


public class Task {
    int priority;
    final ChatMember chatMember;
    Instant endTime;
    Timer timer;
    long duration;
    Task(ChatMember chatMember,int priority,long duration){
        endTime = Instant.now();
        this.duration = duration;
        endTime = endTime.plusMillis(duration);
        this.chatMember = chatMember;

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
