package org.bingmaps.app;

import org.bingmaps.sdk.Coordinate;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.Toast;

import static org.bingmaps.app.Constants.PERMISSION_LOCATION_REQUEST_CODE;

public class GPSManager {
    public LocationManager _locationManager;
    private String _bestProvider = LocationManager.GPS_PROVIDER;
    private Activity _activity;
    private LocationListener _listener;
    private Location _location = null;
    private double mCustomLat = 13.0102;
    private double mCustomLng = 80.2157;

    public GPSManager(Activity activity, LocationListener listener) {
        _activity = activity;
        _listener = listener;
        _locationManager = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

        if (_bestProvider != null) {
            if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                _locationManager.requestLocationUpdates(_bestProvider, Constants.GPSTimeDelta, Constants.GPSDistanceDelta, listener);
            }
        }
    }

    public Coordinate GetCoordinate() {
        boolean isGPSEnabled = _locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = _locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (isNetworkEnabled || isGPSEnabled) {
            if (isNetworkEnabled) {
                if (ActivityCompat.checkSelfPermission(_activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(_activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(_activity,new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION},PERMISSION_LOCATION_REQUEST_CODE);
                }
                _locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, Constants.GPSTimeDelta, Constants.GPSDistanceDelta, _listener);
                Log.d("activity", "LOC Network Enabled");
                _location = _locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (_location != null) {
                    Log.d("activity", "LOC by Network");
                    double latitude = _location.getLatitude();
                    double longitude = _location.getLongitude();
                    return new Coordinate(latitude, longitude);
                } else {
                    return null;
                }
            }

            // if GPS Enabled get lat/long using GPS Services
            if (isGPSEnabled) {
                if (_location == null) {

                    _locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, Constants.GPSTimeDelta, Constants.GPSDistanceDelta, _listener);
                    Log.d("activity", "RLOC: GPS Enabled");
                    if (_locationManager != null) {
                        _location = _locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                        if (_location != null) {
                            Log.d("activity", "RLOC: loc by GPS");
                            double latitude = _location.getLatitude();
                            double longitude = _location.getLongitude();
                            return new Coordinate(latitude, longitude);
                        } else {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }


    public void refresh() {
        if (_bestProvider == null) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setCostAllowed(true);
            _bestProvider = _locationManager.getBestProvider(criteria, false);
        }
        if (_bestProvider != null) {
            if (ActivityCompat.checkSelfPermission(_activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(_activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                _locationManager.requestLocationUpdates(_bestProvider, Constants.GPSTimeDelta, Constants.GPSDistanceDelta, _listener);
            }
        }
    }
}
