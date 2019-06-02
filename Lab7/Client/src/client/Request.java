package client;

import fairytale.commands.CommandDescriptor;

import java.io.Serializable;

public class Request implements Serializable {

    private final String TOKEN;
    private final long ID;
    private final String LOGIN;
    private final CommandDescriptor DESCRIPTOR;

    public Request(long id, String token, String login,CommandDescriptor descriptor) {
        LOGIN = login;
        DESCRIPTOR = descriptor;
        TOKEN=token;
        ID=id;
    }

    public String getTOKEN() {
        return TOKEN;
    }

    public long getID() {
        return ID;
    }

    public String getLOGIN() {
        return LOGIN;
    }

    public CommandDescriptor getDESCRIPTOR() {
        return DESCRIPTOR;
    }
}
