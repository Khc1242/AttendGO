package com.sucfyp.attendgo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class CreateEventActivity extends AppCompatActivity {
    //Initialize variables
    private static final String TAG = "CreateEventActivity";
    private TextInputEditText EventName, EventOrg, EventHost, HostEmail, EventDesc;
    private ProgressBar createProgress;
    private Button createBtn;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FusedLocationProviderClient fusedLocationProviderClient;
    String userID, userName, latitude, longitude;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int REQUEST_CHECK_SETTINGS = 10001;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private TextView timePickerTV, timeTV, datePickerTV, dateTV, locationPickerTV, locationTV;
    private DatePickerDialog datePD;
    private TimePickerDialog timePD;
    private ImageButton datePickerButton, timePickerButton, locationPickerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        getSupportActionBar().hide();

        updateGPS();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext()).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // Toast.makeText(MainActivity.this, "GPS is already turned on", Toast.LENGTH_SHORT).show();
                } catch (ApiException e) {
                    if (e.getStatusCode() == LocationSettingsStatusCodes.RESOLUTION_REQUIRED) {
                        try {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            resolvableApiException.startResolutionForResult(CreateEventActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException ex) {
                            ex.printStackTrace();
                            Log.d(TAG, "onFailure: " + ex.toString());
                        }
                    }
                }
            }
        });

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);
                updateAddressText(locationResult.getLastLocation());
            }
        };

        EventName = findViewById(R.id.editUserName);
        EventOrg = findViewById(R.id.eventOrgName);
        EventHost = findViewById(R.id.eventHostName);
        HostEmail = findViewById(R.id.eventHostEmail);
        EventDesc = findViewById(R.id.eventDesc);

        createProgress = findViewById(R.id.submitPB);
        createProgress.setVisibility(View.INVISIBLE);

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        userID = mAuth.getCurrentUser().getUid();
        userName = mAuth.getCurrentUser().getDisplayName();

        datePickerTV = findViewById(R.id.datePickerTV);
        datePickerTV.setPaintFlags(datePickerTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        dateTV = findViewById(R.id.dateTV);
        datePickerButton = findViewById(R.id.datePickerButton);
        datePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int dayInt = calendar.get(Calendar.DAY_OF_MONTH);
                int monthInt = calendar.get(Calendar.MONTH);
                int yearInt = calendar.get(Calendar.YEAR);

                datePD = new DatePickerDialog(CreateEventActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        int selectedMonth = month + 1;
                        dateTV.setText(year + "." + selectedMonth + "." + dayOfMonth);
                    }
                }, yearInt, monthInt, dayInt);
                datePD.show();
            }
        });

        timePickerTV = findViewById(R.id.timePickerTV);
        timePickerTV.setPaintFlags(timePickerTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        timeTV = findViewById(R.id.timeTV);
        timePickerButton = findViewById(R.id.timePickerButton);
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int hourInt = calendar.get(Calendar.HOUR_OF_DAY);
                int minuteInt = calendar.get(Calendar.MINUTE);

                timePD = new TimePickerDialog(CreateEventActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        timeTV.setText(String.format("%02d:%02d", hourOfDay, minute));
                    }
                }, hourInt, minuteInt, true);
                timePD.show();
            }
        });

        ImageView createClosePage = findViewById(R.id.createCloseIV);
        createClosePage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        locationPickerTV = findViewById(R.id.locationPickerTV);
        locationPickerTV.setPaintFlags(locationPickerTV.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
        locationTV = findViewById(R.id.locationTV);
        locationPickerButton = findViewById(R.id.locationPickerButton);
        locationPickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startLocationUpdates();
                updateGPS();
            }
        });

        createBtn = findViewById(R.id.eventBtn);
        createBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createEvent();
                stopLocationUpdates();
            }
        });
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(CreateEventActivity.this);
        if (ActivityCompat.checkSelfPermission(CreateEventActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(CreateEventActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateAddressText(location);
                }
            });
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_FINE_LOCATION);
            }
        }
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest,locationCallback,null);
        updateGPS();
    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private void updateAddressText(Location location) {
        Geocoder geocoder = new Geocoder(CreateEventActivity.this, Locale.getDefault());
        try {
            // Initialize geocoder
            // Initialize address list
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            // Set address
            locationTV.setText(addresses.get(0).getAddressLine(0));
            latitude = String.valueOf(location.getLatitude());
            longitude = String.valueOf(location.getLongitude());
            Log.d(TAG, "onSuccess: Attendance Location: " + locationTV);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onFailure: " + e.toString());
            showMessage("Failed to save location! Please enable your GPS!");
            locationTV.setText("Unable to get the address");
        }
    }

    private void createEvent(){
        createBtn.setVisibility(View.INVISIBLE);
        createProgress.setVisibility(View.VISIBLE);
        final String eventName = EventName.getText().toString();
        final String eventOrganization = EventOrg.getText().toString();
        final String hostName = EventHost.getText().toString();
        final String hostEmail = HostEmail.getText().toString();
        final String description = EventDesc.getText().toString();
        final String eventTime = timeTV.getText().toString();
        final String eventDate = dateTV.getText().toString();
        final String eventAddress = locationTV.getText().toString();
        final String locationLatitude = latitude;
        final String locationLongitude = longitude;

        if (eventName.isEmpty() || hostName.isEmpty() || hostEmail.isEmpty() || description.isEmpty() ||eventTime.isEmpty() || eventDate.isEmpty() || eventAddress.isEmpty()) {
            // something goes wrong : all fields must be filled
            // we need to display an error message
            showMessage("Please fill in all fields!");
            createBtn.setVisibility(View.VISIBLE);
            createProgress.setVisibility(View.INVISIBLE);
        } else {
            createBtn.setVisibility(View.VISIBLE);
            createProgress.setVisibility(View.INVISIBLE);

            //Upload data to FireStore
            String eventID = UUID.randomUUID().toString();
            String qrCode = UUID.randomUUID().toString();
            DocumentReference documentReference = fStore.collection("Event").document(eventID);
            Map<String, Object> event = new HashMap<>();
            event.put("eventName", eventName);
            event.put("eventID", eventID);
            event.put("eventOrganization", eventOrganization);
            event.put("hostName", hostName);
            event.put("hostEmail", hostEmail);
            event.put("description", description);
            event.put("eventTime", eventTime);
            event.put("eventDate", eventDate);
            event.put("eventAddress", eventAddress);
            event.put("locationLatitude", locationLatitude);
            event.put("locationLongitude", locationLongitude);
            event.put("QR Code", qrCode);
            // Insert timestamps for sorting more easily in Firebase
            event.put("Timestamp", Timestamp.now());
            event.put("Creator", userName);
            event.put("CreatorID", userID);
            documentReference.set(event).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.d(TAG, "onSuccess: Event Profile is created for " + eventName);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d(TAG, "onFailure: " + e.toString());
                }
            });

            Intent intent = new Intent(CreateEventActivity.this, QRCodeActivity.class);
            intent.putExtra("EVENT_ID", eventID);
            intent.putExtra("EVENT_NAME", eventName);
            intent.putExtra("QR_CODE", qrCode);
            startActivity(intent);
            finish();
        }
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}