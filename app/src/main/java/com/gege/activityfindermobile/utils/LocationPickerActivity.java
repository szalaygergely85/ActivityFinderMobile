package com.gege.activityfindermobile.utils;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.gege.activityfindermobile.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.button.MaterialButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Simple location picker activity using manual input or device location User can search for a
 * location by address/place name
 */
public class LocationPickerActivity extends AppCompatActivity {
    public static final int RESULT_LOCATION_SELECTED = 1;
    public static final int RESULT_CANCELED = 2;

    private EditText etLocationSearch;
    private MaterialButton btnUseCurrentLocation;
    private MaterialButton btnSearch;
    private MaterialButton btnCancel;
    private FusedLocationProviderClient fusedLocationClient;
    private Geocoder geocoder;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        initViews();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        geocoder = new Geocoder(this, Locale.getDefault());

        setupListeners();
    }

    private void initViews() {
        etLocationSearch = findViewById(R.id.et_location_search);
        btnUseCurrentLocation = findViewById(R.id.btn_use_current_location);
        btnSearch = findViewById(R.id.btn_search_location);
        btnCancel = findViewById(R.id.btn_cancel);

        // Set navigation icon click for back navigation
        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            // Action bar might not be available
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        setResult(RESULT_CANCELED);
        finish();
        return true;
    }

    private void setupListeners() {
        btnSearch.setOnClickListener(v -> searchLocation());
        btnUseCurrentLocation.setOnClickListener(v -> getCurrentLocation());
        btnCancel.setOnClickListener(
                v -> {
                    setResult(RESULT_CANCELED);
                    finish();
                });
    }

    private void searchLocation() {
        String searchQuery = etLocationSearch.getText().toString().trim();

        if (searchQuery.isEmpty()) {
            Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Use Geocoder to get coordinates from address
        try {
            List<Address> addresses = geocoder.getFromLocationName(searchQuery, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                returnLocationResult(
                        address.getAddressLine(0),
                        address.getAddressLine(0),
                        address.getLatitude(),
                        address.getLongitude());
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Toast.makeText(this, "Error searching location: " + e.getMessage(), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(
                    this,
                    new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            // Permission already granted
            getLocationInternal();
        }
    }

    private void getLocationInternal() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(
                            location -> {
                                if (location != null) {
                                    // Get address from coordinates using Geocoder
                                    try {
                                        List<Address> addresses =
                                                geocoder.getFromLocation(
                                                        location.getLatitude(),
                                                        location.getLongitude(),
                                                        1);

                                        if (addresses != null && !addresses.isEmpty()) {
                                            Address address = addresses.get(0);
                                            returnLocationResult(
                                                    address.getAddressLine(0),
                                                    address.getAddressLine(0),
                                                    location.getLatitude(),
                                                    location.getLongitude());
                                        } else {
                                            // No address found, use coordinates
                                            returnLocationResult(
                                                    "Current Location",
                                                    "Current Location",
                                                    location.getLatitude(),
                                                    location.getLongitude());
                                        }
                                    } catch (IOException e) {
                                        returnLocationResult(
                                                "Current Location",
                                                "Current Location",
                                                location.getLatitude(),
                                                location.getLongitude());
                                    }
                                } else {
                                    Toast.makeText(
                                                    LocationPickerActivity.this,
                                                    "Unable to get current location",
                                                    Toast.LENGTH_SHORT)
                                            .show();
                                }
                            });
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocationInternal();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void returnLocationResult(
            String placeName, String address, double latitude, double longitude) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("place_name", placeName);
        resultIntent.putExtra("address", address);
        resultIntent.putExtra("latitude", latitude);
        resultIntent.putExtra("longitude", longitude);

        setResult(RESULT_LOCATION_SELECTED, resultIntent);
        finish();
    }
}
