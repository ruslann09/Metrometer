package com.proger.ruslan.advancedact;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Text;

public class GPSLocation extends FragmentActivity implements OnMapReadyCallback {

    //основные переменные для работы с интерактивной картой GoogleMap

    public static final String TYPE = "LOCATION";

    SupportMapFragment mapFragment;
    private TreeMap<String, LatLng> places = new TreeMap<>();
    private List<Marker> positions;
    private Location location;
    private LocationListener locationListener;
    private GoogleMap googleMap;
    private PolylineOptions line;
    private Polyline curPolyLine;

    private double latitude, longitude;
    private long perimeter, area;
    private boolean isStart, isAutoMeasuring;
    private int dotNums;
    private boolean setDestination;

    private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;
    private static final long MIN_TIME_BW_UPDATES = 5000;
    protected LocationManager locationManager;

    private Button fix, stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpslocation);

        //получения интента с запросом на редактирование

        if (!isStart) {
            try {
                Intent intent = getIntent();

                if (intent != null && intent.hasExtra("Matches")) {
                    DataMatches matches = (DataMatches) getIntent().getSerializableExtra("Matches");
                    String dotsRelative[] = matches.getDotsRelatives().split(" ");

                    for (int i = 0; i < dotsRelative.length - 4; i += 2)
                        places.put(String.valueOf (dotNums++), new LatLng(Double.parseDouble(dotsRelative[i]), Double.parseDouble(dotsRelative[i + 1])));
                }

                positions = new LinkedList<>();
                isStart = true;
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show ();
            }
        }

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //создание объекта изменения позиций
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        //инициализация объекта слушателя смены позиций
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (isAutoMeasuring) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        places.put(String.valueOf(dotNums++), new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude()));
                        addMarker(String.valueOf(dotNums - 1), (dotNums - 1) + " in fixes", places.get(String.valueOf(dotNums - 1)),
                                googleMap, true, BitmapDescriptorFactory.HUE_CYAN);
                    }

                    //расчет данных


                    buildDestination();
                    getPerimeter();
                    getArea();
                }
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

        //кнопки для работы и управления изменением позиций

        fix = (Button) findViewById(R.id.check_pos);
        fix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setConclusionPoint();
            }
        });

        stop = (Button) findViewById(R.id.stop_pos);

        stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDestination = true;

                setConclusionPoint();
            }
        });

        //проверка возможности появления инструкции по работе с картой в приложении (только один раз за использование)

        if (getSharedPreferences(getApplicationContext().getString(R.string.APP_PREFERENCES), Context.MODE_PRIVATE).getBoolean("isInitMapMeasure", true)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(GPSLocation.this);
            builder.setTitle(R.string.gpslocation).setMessage(R.string.gpslocation_dialog)
                    .setCancelable(false)
                    .setNegativeButton(R.string.understand, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            SharedPreferences.Editor editor = getSharedPreferences(getApplicationContext().getString (R.string.APP_PREFERENCES), Context.MODE_PRIVATE).edit();
                            editor.putBoolean ("isInitMapMeasure", false);
                            editor.commit();

                            dialog.cancel();
                        }
                    }).create().show();
        }
    }


    //расчет назначения позиций карты

    private void buildDestination () {
        if (positions.size() > 1) {
            line = new PolylineOptions();
            LatLngBounds.Builder latLngBuilder = new LatLngBounds.Builder();
            line.width(10f).color(R.color.colorPrimaryDark);

            for (Marker marker : positions) {
                line.add(marker.getPosition());
                latLngBuilder.include(marker.getPosition());
            }

            //возможность выгрузки данных для отправке в главную активность

            if (setDestination) {
                try {
                    line.add(positions.get(0).getPosition());
                    fix.setEnabled(false);

                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            DataMatches matches;

                            String dotsRelative = "";

                            for (Marker marker : positions)
                                dotsRelative += (double) marker.getPosition().latitude + " " + (double) marker.getPosition().longitude + " ";

                            final Date timeNow = new Date();
                            final SimpleDateFormat dateFormatStamp = new SimpleDateFormat("yyyy.MM.dd hh.mm.ss.SSS");

                            double quality_percent = ((positions.size() - 2) + perimeter
                                    + area)/Math.ceil((positions.size() - 2) + perimeter + area)*100;

                            Intent intent = new Intent();

                            intent.putExtra("Matches", new DataMatches(-1, area, perimeter, positions.size() - 2,
                                    dotsRelative, dateFormatStamp.format(timeNow), TYPE, "", "", quality_percent));

                            setResult(RESULT_OK, intent);

                            finish();
                        }
                    }, 3500);
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), getString(R.string.unknown_wrong), Toast.LENGTH_SHORT).show();
                }
            }

            line.geodesic(true);

            if (curPolyLine != null)
                curPolyLine.remove();

            curPolyLine = googleMap.addPolyline(line);

            if (places.size() > 3) {
                int size = getResources().getDisplayMetrics().widthPixels;
                LatLngBounds latLngBounds = latLngBuilder.build();
                CameraUpdate track = CameraUpdateFactory.newLatLngBounds(latLngBounds, size, size, 10);
                googleMap.moveCamera(track);
            }
        }
    }

    //расчет периметра по формуле

    private void getPerimeter () {
        LatLng lastPos = null;

        perimeter = 0;

        for (Marker marker : positions) {
            if (lastPos == null) {
                perimeter += distBetween(positions.get(positions.size() - 1).getPosition(),
                        marker.getPosition());
            } else
                perimeter += distBetween(lastPos, marker.getPosition());

            lastPos = marker.getPosition();
        }
    }

    //расчет площади по формуле

    private void getArea () {
        double plus = 0, minus = 0;
        area = 0;

        for (int i = 0; i < positions.size(); i++) {
            if (i == positions.size() - 1) plus += positions.get (i).getPosition().latitude * positions.get (0).getPosition().longitude*1000000000;
            else plus += positions.get (i).getPosition().latitude * positions.get (i + 1).getPosition().longitude*1000000000;

            if (i == 0) minus += positions.get (i).getPosition().latitude * positions.get (positions.size() - 1).getPosition().longitude*1000000000;
            else minus += positions.get (i).getPosition().latitude * positions.get (i - 1).getPosition().longitude*1000000000;
        }

        area = (long)(Math.abs (plus - minus));
    }

    //расчет расстояния между точками

    public static float distBetween(LatLng pos1, LatLng pos2) {
        return distBetween(pos1.latitude, pos1.longitude, pos2.latitude,
                pos2.longitude);
    }

    //переопределение методы расчета расстояния между позициями

    public static float distBetween(double lat1, double lng1, double lat2, double lng2) {
        double earthRadius = 3958.75;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLng = Math.toRadians(lng2 - lng1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLng / 2)
                * Math.sin(dLng / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double dist = earthRadius * c;

        int meterConversion = 1609;

        return (float) (dist * meterConversion);
    }


    //проверка доступности отслеживания

    @Override
    protected void onResume() {
        super.onResume();
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(getApplicationContext(), R.string.checkTheLocationPermissions, Toast.LENGTH_SHORT).show();
            startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            GPSLocation.this.finish();
            return;
        } else
            setLocaleManager();
    }

    public static long TIME_INTERVAL_FOR_EXIT = 1500;
    private long lastTimeBackPressed;

    //защита от случайного оканчания сессиии

    @Override
    public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
        if(pKeyCode == KeyEvent.KEYCODE_BACK && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
            if(System.currentTimeMillis() - lastTimeBackPressed < TIME_INTERVAL_FOR_EXIT) {
                finish();
            }
            else {
                lastTimeBackPressed = System.currentTimeMillis();
                Toast.makeText(getApplicationContext(), R.string.tapAgainToExit, Toast.LENGTH_SHORT).show ();
            }
            return true;
        } else {
            return super.onKeyDown(pKeyCode, pEvent);
        }
    }

    //установка параметров для управления картой

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

        places.put(getString(R.string.ImHere), new LatLng(latitude, longitude));
    }

    //наствройка самой карты для комфортной работы
    //и реализация контекстного меню для настройки управления

    private void setVisibleState() {
        final String[] mChooseCats = {getString(R.string.layers), getString(R.string.satillite),
                getString(R.string.satelliteAndInfo), getString(R.string.autoMeasuring)
         + (isAutoMeasuring ? getString(R.string.takeOff) : getString(R.string.takeOn)), getString(R.string.get_actual_data)};
        AlertDialog.Builder builder = new AlertDialog.Builder(GPSLocation.this);
        builder.setTitle(R.string.layersSystem)
                .setCancelable(false)

                // добавляем одну кнопку для закрытия диалога
                .setNeutralButton(R.string.cancel,
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
                                    case 3:
                                        isAutoMeasuring = !isAutoMeasuring;

                                        if (isAutoMeasuring)
                                            fix.setEnabled(false);
                                        else
                                            fix.setEnabled(true);
                                        break;
                                    case 4:
                                        Toast.makeText(getApplicationContext(), getString (R.string.perimeter_is) + " "
                                                + String.valueOf(perimeter), Toast.LENGTH_LONG).show ();
                                        Toast.makeText(getApplicationContext(), getString (R.string.square_is) + " "
                                                + String.valueOf(area), Toast.LENGTH_LONG).show ();
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

    //добавление новой позиции на интерактивную карту

    private void addMarker(String name, String snippet, LatLng pos, GoogleMap googleMap, boolean isDraggable, float color) {
        MarkerOptions option = new MarkerOptions().position(pos).title(name).draggable(isDraggable).alpha(0.5f).snippet(snippet).icon(
                BitmapDescriptorFactory.defaultMarker(color)).zIndex(1f);
        positions.add(googleMap.addMarker(option));
    }

    //установка полного определения карты

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

        googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                marker.setSnippet("X: " + marker.getPosition().latitude + "\nY: " + marker.getPosition().longitude);

                if (marker.getTitle().equals(positions.get(0).getTitle()))
                    setDestination = true;
                else
                    setDestination = false;

                return false;
            }
        });

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {
                //Toast.makeText(getApplicationContext(), R.string.moveTheMarker, Toast.LENGTH_SHORT).show ();
            }

            @Override
            public void onMarkerDrag(Marker marker) {
                //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(curDragPosition, 17f));
            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                buildDestination();
                //Toast.makeText(getApplicationContext(), marker.getPosition().latitude + " " + marker.getPosition().longitude, Toast.LENGTH_SHORT).show ();
                getPerimeter();
                getArea();
            }
        });

        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                setDestination = false;

                setConclusionPoint();
            }
        });

        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                setVisibleState();
            }
        });

        googleMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                positions.get(0).setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
            }
        });
    }

    //основная обработка карты
    @Override
    public void onMapReady(final GoogleMap googleMap) {
        this.googleMap = googleMap;

        setAllProperties();

        if (places.containsKey(getString(R.string.ImHere))) {
            addMarker(getString(R.string.ImHere), getString(R.string.yourCurrentLocation), places.get(getString(R.string.ImHere)),
                    googleMap, false, BitmapDescriptorFactory.HUE_ORANGE);

            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(places.get(getString(R.string.ImHere)), 17f));

            showAlertOnPoint(positions.get(0), 5000);

            places.remove(getString(R.string.ImHere));
        }

        for (Map.Entry<String, LatLng> place : places.entrySet())
            addMarker(place.getKey(), "", place.getValue(), googleMap, false, BitmapDescriptorFactory.HUE_CYAN);

        buildDestination();
        getPerimeter();
        getArea();
    }

    //открытие окна отображения доступности точек на карте

    private void showAlertOnPoint (final Marker marker, final long timeToTime) {
        marker.showInfoWindow();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                marker.hideInfoWindow();
            }
        }, timeToTime);
    }

    //заключение позиции на карту

    private void setConclusionPoint () {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            places.put(String.valueOf(dotNums++), new LatLng(googleMap.getMyLocation().getLatitude(), googleMap.getMyLocation().getLongitude()));
            addMarker(String.valueOf(dotNums - 1), (dotNums - 1) + " in fixes", places.get(String.valueOf(dotNums - 1)),
                    googleMap, true, BitmapDescriptorFactory.HUE_CYAN);
        }

        buildDestination();
        getPerimeter();
        getArea();
    }
}
