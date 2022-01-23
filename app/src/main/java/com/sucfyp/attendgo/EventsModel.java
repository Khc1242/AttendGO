package com.sucfyp.attendgo;

import java.io.Serializable;

public class EventsModel implements Serializable {
    private String eventName,eventID,eventOrganization,hostName,hostEmail,description,eventTime,eventDate,eventAddress;

    private EventsModel() { super();}

    private EventsModel(String eventName, String eventID, String eventOrganization, String hostName, String hostEmail, String description, String eventTime, String eventDate, String eventAddress) {
        this.eventName = eventName;
        this.eventID = eventID;
        this.eventOrganization = eventOrganization;
        this.hostName = hostName;
        this.hostEmail = hostEmail;
        this.description = description;
        this.eventDate = eventDate;
        this.eventTime = eventTime;
        this.eventAddress = eventAddress;
    }

    public String getEventName() {
        return eventName;
    }
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public String getEventID() {
        return eventID;
    }
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    public String getEventOrganization() {
        return eventOrganization;
    }
    public void setEventOrganization(String eventOrganization) {
        this.eventOrganization = eventOrganization;
    }

    public String getHostName() {
        return hostName;
    }
    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public String getHostEmail() {
        return hostEmail;
    }
    public void setHostEmail(String hostEmail) {
        this.hostEmail = hostEmail;
    }

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public String getEventTime() {
        return eventTime;
    }
    public void setEventTime(String eventTime) {
        this.eventTime = eventTime;
    }

    public String getEventDate() {
        return eventDate;
    }
    public void setEventDate(String eventDate) {
        this.eventDate = eventDate;
    }

    public String getEventAddress() {
        return eventAddress;
    }
    public void setEventAddress(String eventAddress) {
        this.eventAddress = eventAddress;
    }
}
