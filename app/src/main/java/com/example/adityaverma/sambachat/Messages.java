package com.example.adityaverma.sambachat;

public class Messages {

    private String Message;
    private String Type;
    private long Time;
    private boolean Seen;
    private String From;

    public Messages() {

    }

    public Messages(String message, String type, long time, boolean seen , String from) {
        Message = message;
        Type = type;
        Time = time;
        Seen = seen;
        From = from;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String message) {
        Message = message;
    }

    public String getType() {
        return Type;
    }

    public void setType(String type) {
        Type = type;
    }

    public long getTime() {
        return Time;
    }

    public void setTime(long time) {
        Time = time;
    }

    public boolean isSeen() {
        return Seen;
    }

    public void setSeen(boolean seen) {
        Seen = seen;
    }

    public String getFrom() {
        return From;
    }

    public void setFrom(String from) {
        From = from;
    }

}
