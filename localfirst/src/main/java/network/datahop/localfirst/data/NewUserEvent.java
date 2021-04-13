package network.datahop.localfirst.data;

public class NewUserEvent {
    private final String userName;

    public NewUserEvent(String userName) {
        this.userName = userName;
    }

    public String getUserName() {
        return userName;
    }
}