package com.sucfyp.attendgo;

import java.io.Serializable;

public class AttendeeModel implements Serializable {
    private String attendeeName,attendanceDate,attendanceTime;

    private AttendeeModel() { super();}

    private AttendeeModel(String attendeeName, String attendanceDate, String attendanceTime) {
        this.attendeeName = attendeeName;
        this.attendanceDate = attendanceDate;
        this.attendanceTime = attendanceTime;
    }

    public String getAttendeeName() {
        return attendeeName;
    }
    public void setAttendeeName(String attendeeName) {
        this.attendeeName = attendeeName;
    }

    public String getAttendanceTime() {
        return attendanceDate;
    }
    public void setAttendanceTime(String attendanceDate) {
        this.attendanceDate = attendanceDate;
    }

    public String getAttendanceDate() {
        return attendanceTime;
    }
    public void setAttendanceDate(String attendanceTime) {
        this.attendanceTime = attendanceTime;
    }

}
