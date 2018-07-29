package com.proger.ruslan.advancedact;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.nfc.cardemulation.HostNfcFService;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.ContextMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ThemedSpinnerAdapter;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    //это аналог GPSLocation но предназначен только для отслеживания собственного положения
    //это очень полезнай режим так как позволяет легко и быстро найти себя с указанием улиц и городов

    SupportMapFragment mapFragment;
    private TreeMap<String, LatLng> places = new TreeMap<>();
    private List<Marker> markers;
    private Location location;
    private LocationListener locationListener;
    private GoogleMap googleMap;

    private double latitude, longitude;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 500;
    protected LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        if (mapFragment == null) {
            finish();
            return;
        }

        mapFragment.getMapAsync(this);

        setVisibleState();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        markers = new ArrayList<>();

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
    }

    private void setVisibleState() {
        final String[] mChooseCats = {"Разметка", "Спутник", "Снимки со спутника и информация"};
        AlertDialog.Builder builder = new AlertDialog.Builder(MapActivity.this);
        builder.setTitle("Выберите принцип оторажения")
                .setCancelable(false)

                // добавляем одну кнопку для закрытия диалога
                .setNeutralButton("Назад",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                dialog.cancel();
                            }
                        })

                // добавляем переключатели
                .setSingleChoiceItems(mChooseCats, -1,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog,
                                                int item) {
                                dialog.cancel();

                                switch (item) {
                                    case 0:
                                        googleMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                                        break;
                                    case 1:
                                        googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                                        break;
                                    case 2:
                                        googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                                        break;
                                    default:
                                        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                                        break;
                                }
                            }
                        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(getApplicationContext(), R.string.checkTheLocationPermissions, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            MapActivity.this.finish();
            return;
        } else
            setLocaleManager();
    }

    private void setLocaleManager() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener);
            locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener);

        }

        if (locationManager != null) {
            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } else if (locationManager != null && location == null) {
            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        }

        places.put("I'm here!", new LatLng(latitude, longitude));
    }

    private void addMarker(String name, String snippet, LatLng pos, GoogleMap googleMap, boolean isDraggable, float color) {
        MarkerOptions option = new MarkerOptions().position(pos).title(name).draggable(isDraggable).alpha(0.5f).snippet(snippet).icon(
                BitmapDescriptorFactory
                        .defaultMarker(color));
        markers.add(googleMap.addMarker(option));
    }

    private void setAllProperties() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            finish();
        }

        UiSettings mapSettings = googleMap.getUiSettings();

        googleMap.setMyLocationEnabled(true);

        googleMap.setBuildingsEnabled(true);
        mapSettings.setCompassEnabled(true);
        mapSettings.setMyLocationButtonEnabled(true);
        mapSettings.setAllGesturesEnabled(true);
        mapSettings.setZoomControlsEnabled(true);
        mapSettings.setZoomGesturesEnabled(true);

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setVisibleState();
            }
        });

        googleMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
            @Override
            public boolean onMyLocationButtonClick() {
                markers.get(0).setPosition(new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude()));

                return false;
            }
        });

        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                markers.get(0).setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        setAllProperties();

        if (places.containsKey("I'm here!")) {
            addMarker("I'm here!", "This is your current position", places.get("I'm here!"),
                    googleMap, false, BitmapDescriptorFactory.HUE_ORANGE);

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(places.get("I'm here!"), 17f));

            showAlertOnPoint(markers.get(0), 5000);

            places.remove("I'm here!");
        }

        for (Map.Entry<String, LatLng> place : places.entrySet())
            addMarker(place.getKey(), "", place.getValue(), googleMap, false, BitmapDescriptorFactory.HUE_CYAN);
    }

    private void showAlertOnPoint (final Marker marker, final long timeToTime) {
        marker.showInfoWindow();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                marker.hideInfoWindow();
            }
        }, timeToTime);
    }
}
