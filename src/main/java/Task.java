import org.telegram.telegrambots.api.objects.ChatMember;

public class Task {
    int priority;
    final ChatMember chatMember;
    Task(ChatMember chatMember,int priority){
        this.priority = priority;
        this.chatMember = chatMember;
    }

}
