package com.gege.activityfindermobile.utils;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.gege.activityfindermobile.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MapPickerActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private Geocoder geocoder;
    private LatLng selectedLatLng = null;
    private String selectedPlaceName = "";
    private SearchView searchView;
    private FloatingActionButton fabMyLocation;
    private MaterialButton btnConfirm;
    private LocationManager locationManager;

    public static final String EXTRA_PLACE_NAME = "place_name";
    public static final String EXTRA_LATITUDE = "latitude";
    public static final String EXTRA_LONGITUDE = "longitude";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_picker);

        // Initialize geocoder and location manager
        geocoder = new Geocoder(this, Locale.getDefault());
        locationManager = new LocationManager(this);

        // Get map fragment
        SupportMapFragment mapFragment =
                (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Initialize views
        searchView = findViewById(R.id.search_view);
        fabMyLocation = findViewById(R.id.fab_my_location);
        btnConfirm = findViewById(R.id.btn_confirm);

        setupSearchView();
        setupListeners();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Set default center (example: San Francisco)
        LatLng defaultLocation = new LatLng(37.7749, -122.4194);
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f));

        // Enable map interactions
        googleMap.setOnMapClickListener(
                latLng -> {
                    addMarker(latLng);
                });

        // Long click for address
        googleMap.setOnMapLongClickListener(
                latLng -> {
                    addMarker(latLng);
                });
    }

    private void setupSearchView() {
        searchView.setOnQueryTextListener(
                new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        searchLocation(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
    }

    private void setupListeners() {
        // Set icon for FAB using system drawable
        fabMyLocation.setImageResource(android.R.drawable.ic_dialog_map);
        fabMyLocation.setOnClickListener(v -> goToMyLocation());
        btnConfirm.setOnClickListener(v -> confirmSelection());
    }

    private void searchLocation(String locationName) {
        if (locationName.isEmpty()) {
            Toast.makeText(this, "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            List<Address> addresses = geocoder.getFromLocationName(locationName, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());
                addMarker(latLng);

                // Update search result text
                String addressText = getAddressText(address);
                selectedPlaceName = addressText;

                // Move camera to location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f));
            } else {
                Toast.makeText(this, "Location not found", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            Log.e("MapPicker", "Geocoding error", e);
            Toast.makeText(this, "Error searching location", Toast.LENGTH_SHORT).show();
        }
    }

    private void addMarker(LatLng latLng) {
        if (googleMap == null) return;

        // Clear previous markers
        googleMap.clear();

        // Add new marker
        googleMap.addMarker(
                new MarkerOptions().position(latLng).title("Selected Location").draggable(true));

        selectedLatLng = latLng;

        // Get address from coordinates
        try {
            List<Address> addresses =
                    geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                selectedPlaceName = getAddressText(addresses.get(0));
            } else {
                selectedPlaceName = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
            }
        } catch (IOException e) {
            Log.e("MapPicker", "Reverse geocoding error", e);
            selectedPlaceName = "Lat: " + latLng.latitude + ", Lng: " + latLng.longitude;
        }

        // Update UI
        updateSelectionDisplay();
    }

    private void goToMyLocation() {
        locationManager.getCurrentLocation(
                this,
                new LocationManager.LocationCallback() {
                    @Override
                    public void onLocationReceived(double latitude, double longitude) {
                        LatLng myLatLng = new LatLng(latitude, longitude);
                        addMarker(myLatLng);
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(myLatLng, 15f));
                        Toast.makeText(
                                        MapPickerActivity.this,
                                        "Current location found",
                                        Toast.LENGTH_SHORT)
                                .show();
                    }

                    @Override
                    public void onError(String errorMessage) {
                        Toast.makeText(MapPickerActivity.this, errorMessage, Toast.LENGTH_SHORT)
                                .show();
                    }
                });
    }

    private void updateSelectionDisplay() {
        // Show current selection info
        if (selectedLatLng != null) {
            btnConfirm.setEnabled(true);
            btnConfirm.setText(
                    "Confirm: "
                            + selectedPlaceName.substring(
                                    0, Math.min(20, selectedPlaceName.length())));
        } else {
            btnConfirm.setEnabled(false);
            btnConfirm.setText("Select Location");
        }
    }

    private void confirmSelection() {
        if (selectedLatLng == null) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return result with selected location
        Intent resultIntent = new Intent();
        resultIntent.putExtra(EXTRA_PLACE_NAME, selectedPlaceName);
        resultIntent.putExtra(EXTRA_LATITUDE, selectedLatLng.latitude);
        resultIntent.putExtra(EXTRA_LONGITUDE, selectedLatLng.longitude);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private String getAddressText(Address address) {
        StringBuilder sb = new StringBuilder();

        if (address.getThoroughfare() != null) {
            sb.append(address.getThoroughfare()).append(", ");
        }
        if (address.getLocality() != null) {
            sb.append(address.getLocality()).append(", ");
        }
        if (address.getCountryName() != null) {
            sb.append(address.getCountryName());
        }

        return sb.toString().trim();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!isChangingConfigurations()) {
            setResult(RESULT_CANCELED);
        }
    }
}
