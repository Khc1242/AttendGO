package com.sucfyp.attendgo;

import java.io.Serializable;

public class AttendanceModel implements Serializable {
    private String attendeeName,attendanceID,attendanceDate,attendanceTime,attendanceLocation, attendanceEvent;

    private AttendanceModel() { super(); }

    private AttendanceModel(String attendeeName, String attendanceID, String attendanceDate, String attendanceTime, String attendanceLocation, String attendanceEvent) {
        this.attendeeName = attendeeName;
        this.attendanceID = attendanceID;
        this.attendanceDate = attendanceDate;
        this.attendanceTime = attendanceTime;
        this.attendanceLocation = attendanceLocation;
        this.attendanceEvent = attendanceEvent;
    }

    public String getAttendeeName() {
        return attendeeName;
    }
    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public String getAttendanceID() { return attendanceID; }
    public void setAttendanceID(String attendanceID) {
        this.attendanceID = attendanceID;
    }

    public String getAttendanceDate() {
        return attendanceDate;
    }
    public void setAttendanceDate(String attendanceDate) { this.attendanceDate = attendanceDate;}

    public String getAttendanceTime() {
        return attendanceTime;
    }
    public void setAttendanceTime(String attendanceTime) {
        this.attendanceTime = attendanceTime;
    }

    public String getAttendanceLocation() { return attendanceLocation; }
    public void setAttendanceLocation(String attendanceLocation) { this.attendanceLocation = attendanceLocation;}

    public String getAttendanceEvent() { return attendanceEvent; }
    public void setAttendanceEvent(String attendanceEvent) { this.attendanceEvent = attendanceEvent;}
}
