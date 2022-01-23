package com.sucfyp.attendgo;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AttendanceDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_attendance_details);
        getSupportActionBar().hide();

        AttendanceModel attModel = (AttendanceModel) getIntent().getSerializableExtra("model");

        TextView attendanceDate = findViewById(R.id.attendanceDate);
        TextView attendanceTime = findViewById(R.id.attendanceTime);
        TextView attendanceID = findViewById(R.id.attendanceID);
        TextView attendeeName = findViewById(R.id.attendeeName);
        TextView attendanceLocation = findViewById(R.id.attendanceLocation);
        TextView attendanceEvent = findViewById(R.id.attendanceEvent);
        ImageView attendanceClose = findViewById(R.id.attdDetailsCloseIV);

        attendanceDate.setText(attModel.getAttendanceDate());
        attendanceTime.setText(attModel.getAttendanceTime());
        attendanceID.setText(attModel.getAttendanceID());
        attendeeName.setText(attModel.getAttendeeName());
        attendanceLocation.setText(attModel.getAttendanceLocation());
        attendanceEvent.setText(attModel.getAttendanceEvent());

        attendanceClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}