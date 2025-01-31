package com.mindhub.email_service.events;

public class EmailEvent {

    private String to;
    private String subject;
    private String body;
    private String username;

    public EmailEvent() {}

    public EmailEvent(String to, String subject, String body, String username) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.username = username;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
