package com.sucfyp.attendgo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.Image;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageCapture;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScannerOptions;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class QRCodeScannerActivity extends AppCompatActivity {
    private static final String TAG = "ScannerActivity";
    private ListenableFuture cameraProviderFuture;
    private ExecutorService cameraExecutor;
    private PreviewView previewView;
    private MyImageAnalyzer analyzer;
    private Button closeScanner;
    String userID, username,
            eventID, eventName,
            latitude, longitude,
            locationLatitude, locationLongitude,
            userLatitude, userLongitude,
            currentDate, currentDateTime, currentDay, currentLocation,
            date, time, QRContent;
    FirebaseFirestore fStore;
    FirebaseAuth mAuth;
    FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private static final int PERMISSION_FINE_LOCATION = 99;
    private static final int REQUEST_CHECK_SETTINGS = 10001;
    public static Bundle bundle = new Bundle();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qrcode_scanner);
        getSupportActionBar().hide();

        updateGPS();

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
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
                            resolvableApiException.startResolutionForResult(QRCodeScannerActivity.this, REQUEST_CHECK_SETTINGS);
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

                updateUserLocation(locationResult.getLastLocation());
            }
        };

        mAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        currentDate = new SimpleDateFormat("yyyy.MM.dd", Locale.ENGLISH).format(new Date());
        currentDateTime = new SimpleDateFormat("yyyy.MM.dd - HH:mm", Locale.ENGLISH).format(new Date());
        date = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH).format(new Date());
        time = new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(new Date());
        currentDay = new SimpleDateFormat("E", Locale.ENGLISH).format(new Date());

        closeScanner = findViewById(R.id.closeScannerBtn);
        closeScanner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) { finish(); }
        });

        Log.d(TAG, "date: " + currentDate);

        previewView = findViewById(R.id.previewView);
        this.getWindow().setFlags(1024, 1024);

        cameraExecutor = Executors.newSingleThreadExecutor();
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        analyzer = new MyImageAnalyzer();
        cameraProviderFuture.addListener(new Runnable() {
            @Override
            public void run() {
                try {
                    if(ActivityCompat.checkSelfPermission(QRCodeScannerActivity.this, Manifest.permission.CAMERA)!=(PackageManager.PERMISSION_GRANTED)){
                        ActivityCompat.requestPermissions(QRCodeScannerActivity.this,new String[]{Manifest.permission.CAMERA},101);
                    }
                    else{
                        ProcessCameraProvider processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
                        bindPreview(processCameraProvider);
                    }
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }, ContextCompat.getMainExecutor(this));
    }  // End of OnCreate

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull @NotNull String[] permissions, @NonNull @NotNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 101 && grantResults.length > 0) {
            ProcessCameraProvider processCameraProvider = null;
            try {
                processCameraProvider = (ProcessCameraProvider) cameraProviderFuture.get();
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            assert processCameraProvider != null;
            bindPreview(processCameraProvider);
        }
    }

    private void bindPreview(ProcessCameraProvider processCameraProvider) {
        Preview preview = new Preview.Builder().build();
        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());
        ImageCapture imageCapture;
        imageCapture = new ImageCapture.Builder().build();
        ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
                .setTargetResolution(new Size(1920,1080))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build();
        imageAnalysis.setAnalyzer(cameraExecutor,analyzer);
        processCameraProvider.unbindAll();
        processCameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture, imageAnalysis);
    }

    public class MyImageAnalyzer implements ImageAnalysis.Analyzer {
        @Override
        public void analyze(@NonNull ImageProxy imageProxy) {
            scanBarcode(imageProxy);
        }

        private void scanBarcode(ImageProxy imageProxy) {
            @SuppressLint("UnsafeOptInUsageError") Image mediaImage = imageProxy.getImage();
            if (mediaImage != null) {
                InputImage inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                BarcodeScannerOptions options = new BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build();
                BarcodeScanner barcodeScanner = BarcodeScanning.getClient(options);
                Task<List<Barcode>> result = barcodeScanner.process(inputImage);
                result.addOnSuccessListener(new OnSuccessListener<List<Barcode>>() {
                    @Override
                    public void onSuccess(List<Barcode> barcodes) {
                        for (Barcode barcode : barcodes) {
                            String qrCode = barcode.getDisplayValue();
                            username = mAuth.getCurrentUser().getDisplayName();
                            userID = mAuth.getCurrentUser().getUid();
                            QRContent = qrCode;
                            CollectionReference eventRef = fStore.collection("Event");
                            Query qrCodeQuery = eventRef.whereEqualTo("QR Code", QRContent);
                            qrCodeQuery.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (DocumentSnapshot document : task.getResult()) {
                                            if (document.exists()) {
                                                Log.d(TAG, "Event exists!");
                                                eventName = document.getString("eventName");
                                                eventID = document.getString("eventID");
                                                locationLatitude = document.getString("locationLatitude");
                                                locationLongitude = document.getString("locationLongitude");
                                                startLocationUpdates();
                                                updateGPS();
                                            } else {
                                                Log.d(TAG, "Event does not exists");
                                                //Notifies the user the QR code is invalid
                                                AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeScannerActivity.this);
                                                builder.setTitle("Invalid QR code");
                                                builder.setMessage("This code is invalid.");
                                                builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        finish();
                                                    }
                                                });
                                                AlertDialog alert = builder.create();
                                                alert.show();
                                            }
                                        }
                                    }
                                }
                            });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG, "onFailure: Scanner Error, " + e.toString());
                    }
                }).addOnCompleteListener(new OnCompleteListener<List<Barcode>>() {
                    @Override
                    public void onComplete(@NonNull Task<List<Barcode>> task) {
                        mediaImage.close();
                        imageProxy.close();
                    }
                });
            }
        }
    }

    private void updateGPS() {
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(QRCodeScannerActivity.this);
        if (ActivityCompat.checkSelfPermission(QRCodeScannerActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(QRCodeScannerActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    updateUserLocation(location);
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

    private void updateUserLocation(Location location) {
        Geocoder geocoder = new Geocoder(QRCodeScannerActivity.this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            currentLocation = addresses.get(0).getAddressLine(0);
            userLatitude = String.valueOf(location.getLatitude());
            userLongitude = String.valueOf(location.getLongitude());
            Log.d(TAG, "onSuccess: Current Location: " + currentLocation);
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "onFailure: Get location failed! " + e.toString());
            showMessage("Get location failed!");
        }

        if ((userLatitude != null) && (userLongitude != null)) {
            float[] results = new float[1];
            double eventLatitude = Double.parseDouble(locationLatitude);
            double eventLongitude = Double.parseDouble(locationLongitude);
            double currentLatitude = Double.parseDouble(userLatitude);
            double currentLongitude = Double.parseDouble(userLongitude);

            Location.distanceBetween(eventLatitude, eventLongitude, currentLatitude, currentLongitude, results);
            float distanceInMeters = results[0];
            boolean isWithin50m = distanceInMeters < 100;
            ViewGroup viewGroup = findViewById(R.id.content);
            if (isWithin50m) {
                addAttendance();
                addAttendanceRecord();

                AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeScannerActivity.this);
                View successView = LayoutInflater.from(QRCodeScannerActivity.this).inflate(R.layout.dialog_attendance_success,viewGroup,false);
                builder.setView(successView);
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                if(!isFinishing())
                {
                    alert.show();
                }
                successView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopLocationUpdates();
                        finish();
                    }
                });
            } else {
                Log.d(TAG, "onFailure: You're not in this place!\nCurrent Location: " + userLatitude + ", " + userLongitude + "\nEvent Location: " + locationLatitude + ", " + locationLongitude);

                AlertDialog.Builder builder = new AlertDialog.Builder(QRCodeScannerActivity.this);
                View failureView = LayoutInflater.from(QRCodeScannerActivity.this).inflate(R.layout.dialog_attendance_failure,viewGroup,false);
                builder.setView(failureView);
                AlertDialog alert = builder.create();
                alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                if(!isFinishing())
                {
                    alert.show();
                }
                failureView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        stopLocationUpdates();
                        finish();
                    }
                });
            }
        }
    }

    // Adds attendance to the database collection where the host can see the attendance of attendee.
    private void addAttendance() {
        String attendanceID = UUID.randomUUID().toString();
        DocumentReference documentReference = fStore.collection("Event")
                .document(eventID)
                .collection("Attendance")
                .document(username + " - " + currentDateTime);
        Map<String, Object> attend = new HashMap<>();
        attend.put("attendanceID", attendanceID);
        attend.put("attendeeName", username);
        attend.put("attendanceDate", date);
        attend.put("attendanceTime", time);
        attend.put("attendanceLocation", currentLocation);
        attend.put("daysOfTheWeek", currentDay);
        attend.put("Timestamp", Timestamp.now());
        documentReference.set(attend).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Attendance ID: " + attendanceID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Take Attendance Failed! " + e.toString());
            }
        });
    }

    // Adds attendance record to attendee(user)'s personal attendance
    private void addAttendanceRecord() {
        String attRecID = UUID.randomUUID().toString();
        DocumentReference documentReference = fStore.collection("User")
                .document(userID)
                .collection("AttendanceRecord")
                .document(currentDateTime);
        Map<String, Object> attend = new HashMap<>();
        attend.put("attendanceID", attRecID);
        attend.put("attendeeName", username);
        attend.put("attendanceDate", date);
        attend.put("attendanceTime", time);
        attend.put("attendanceLocation", currentLocation);
        attend.put("attendanceEvent", eventName);
        attend.put("Timestamp", Timestamp.now());
        documentReference.set(attend).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Log.d(TAG, "onSuccess: Attendance ID: " + attRecID);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG, "onFailure: Record Attendance Failed! " + e.toString());
            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
    }
}
