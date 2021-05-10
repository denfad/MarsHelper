package ru.denfad.sensorproject;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;

import java.util.Arrays;
import java.util.List;

import ru.denfad.sensorproject.DAO.DbService;
import ru.denfad.sensorproject.DAO.model.Zone;

import static android.location.LocationManager.GPS_PROVIDER;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, LocationListener {

    private final String CHANNEL_ID = "Magnet";
    private final int NOTIFY_ID = 101;

    private double x = 0, y = 0;

    private DbService dbService;
    private int temp = 0;

    private MapFragment mapFragment;
    private GoogleMap map;
    private LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //верхние показателт давления, температуры и относительной влажности
        TextView pressure = findViewById(R.id.pressure);
        TextView humidity = findViewById(R.id.humidity);
        TextView temperature = findViewById(R.id.temperature);

        //создание карты
        mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.mapView);
        mapFragment.getMapAsync(this);

        //создание бд
        dbService = new DbService(getApplicationContext());

        //создание мэнеджера геолокации
        locationManager = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        assert locationManager != null;
        locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, this);


        //настройка сенсоров
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        System.out.println(Arrays.asList(sensors));

        //сенсор температуры
        Sensor tempSensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                System.out.println(Arrays.toString(event.values));
                temp = (int) event.values[0];
                temperature.setText("t: " + event.values[0] + " °C");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, tempSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //сенсор давления
        Sensor presSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                System.out.println(Arrays.toString(event.values));
                pressure.setText("P: " + event.values[0] + " hPa");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, presSensor, SensorManager.SENSOR_DELAY_NORMAL);

        //сенсор относительной влажности
        Sensor humSensor = sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY);
        sensorManager.registerListener(new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                System.out.println(Arrays.toString(event.values));
                humidity.setText("RH : " + event.values[0] + "%");
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        }, humSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onLocationChanged(Location location) {
        //6 угловых секунд
        if (Math.abs(location.getLongitude() - y) >= (6 / 3600.0) || Math.abs(location.getLatitude() - x) >= (6 / 3600.0)) {
            x = location.getLatitude();
            y = location.getLongitude();
            generateCircle(temp, x, y);
            dbService.addZone(new Zone(temp, (float) x, (float) y));
        }
    }

    public void generateAllCircles() {
        for (Zone z : dbService.getAllZones()) {
            generateCircle(z.getTemperature(), z.getX(), z.getY());
        }
    }

    public void generateCircle(int temperature, double x1, double y1) {
        CircleOptions options = new CircleOptions().center(new LatLng(x1, y1))
                .radius(90)
                .strokeWidth(0)
                .clickable(true);
        if (temperature > 0)
            options.fillColor(getResources().getColor(R.color.hot));
        else if (temperature >= -68 & temperature <= 0)
            options.fillColor(getResources().getColor(R.color.normal));
        else
            options.fillColor(getResources().getColor(R.color.cold));
        map.addCircle(options);
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        setUpMap();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    //настройки карты
    public void setUpMap() {
        map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this, R.raw.style_json));
        map.getUiSettings().setMyLocationButtonEnabled(true);
        generateAllCircles();

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        map.setMyLocationEnabled(true);


    }

}