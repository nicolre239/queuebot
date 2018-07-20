public class User {
    final long userId;
    final long chatId;
    private String userName = "unknown";
    User(long userId,long chatId){
        this.userId = userId;
        this.chatId = chatId;
    }

    User(long userId,long chatId, String userName){
        this.userId = userId;
        this.chatId = chatId;
        this.userName = userName;
    }

    String getUserName(){
        return userName;
    }


}
