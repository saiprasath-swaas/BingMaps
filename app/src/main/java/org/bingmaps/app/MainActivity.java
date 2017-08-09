package org.bingmaps.app;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import org.bingmaps.sdk.BingMapsView;
import org.bingmaps.sdk.Coordinate;
import org.bingmaps.sdk.EntityClickedListener;
import org.bingmaps.sdk.EntityLayer;
import org.bingmaps.sdk.MapLoadedListener;
import org.bingmaps.sdk.MapMovedListener;
import org.bingmaps.sdk.MapStyles;
import org.bingmaps.sdk.Point;
import org.bingmaps.sdk.Polyline;
import org.bingmaps.sdk.PolylineOptions;
import org.bingmaps.sdk.Pushpin;
import org.bingmaps.sdk.PushpinOptions;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.bingmaps.app.Constants.PERMISSION_LOCATION_REQUEST_CODE;

public class MainActivity extends AppCompatActivity implements MapMovedListener {

    CharSequence[] _dataLayers;
    boolean[] _dataLayerSelections;
    private BingMapsView bingMapsView;
    private GPSManager _GPSManager;
    private EntityLayer _gpsLayer;
    private ProgressDialog _loadingScreen;
    private double mCustomLat = 13.0102;
    private double mCustomLng = 80.2157;
    private Button editbtn;
    DecimalFormat decimalFormat = new DecimalFormat("#.000000");
    /**
     * Handler for loading Screen
     */
    protected Handler loadingScreenHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.arg1 == 0) {
                _loadingScreen.hide();
            } else {
                _loadingScreen.show();
            }
        }
    };
    private Activity _baseActivity;
    private TextView lat;
    private TextView lng;
    private ImageButton btnZoomIn;
    private ImageButton btnZoomOut;
    private ImageButton btnMyLocation;
    LocationManager locationManager;
    String _bestProvider = LocationManager.GPS_PROVIDER;
    public Location mLocation= null;

    public static boolean checkPermission(final Context context) {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable throwable) {
                throwable.printStackTrace();
            }
        });

        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        // OPTION Lock map orientation
        // setRequestedOrientation(1);

        setContentView(R.layout.map);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        lat = (TextView)findViewById(R.id.lat);
        lng = (TextView)findViewById(R.id.lng);

        btnZoomIn = (ImageButton) findViewById(R.id.btnZoomIn);
        btnZoomIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bingMapsView.zoomIn();
            }
        });

        btnZoomOut = (ImageButton) findViewById(R.id.btnZoomOut);
        btnZoomOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bingMapsView.zoomOut();
            }
        });

        Initialize();

        editbtn = (Button)findViewById(R.id.editbtn);
        editbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bingMapsView.setMapMovedListener(new MapMovedListener() {
                    @Override
                    public void onAvailableChecked() {
                        _gpsLayer = new EntityLayer(Constants.DataLayers.GPS);
                        bingMapsView.getLayerManager().addLayer(_gpsLayer);
                        bingMapsView.setFitsSystemWindows(true);
                        bingMapsView.setMapStyle(MapStyles.StreetSide);
                        UpdateGPSPin();
                        updateMarker();
                    }
                });

            }
        });

//        bingMapsView.setMapMovedListener(new MapMovedListener() {
//            @Override
//            public void onAvailableChecked() {
//
////                Coordinate mUpdatedCoord = bingMapsView.getCenter();
////                PushpinOptions pin = new PushpinOptions();
////                pin.Icon = Constants.PushpinIcons.GPS;
////                Pushpin pushpin1 = new Pushpin(mUpdatedCoord, pin);
////                if (pushpin1.Location != null) {
////                    lat.setText(String.valueOf(decimalFormat.format(mUpdatedCoord.Latitude)));
////                    lng.setText(String.valueOf(decimalFormat.format(mUpdatedCoord.Longitude)));
////                    bingMapsView.loadMap(Constants.BingMapsKey,mUpdatedCoord,Constants.DefaultGPSZoomLevel);
////                }
//
//            }
//        });
        btnMyLocation = (ImageButton) findViewById(R.id.btnMyLocation);
        btnMyLocation.setTag(String.valueOf("Get Your Location"));
        btnMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Coordinate coord = _GPSManager.GetCoordinate();
                if (coord != null) {
                    // Center on users GPS location
                    bingMapsView.setCenterAndZoom(coord,
                            Constants.DefaultGPSZoomLevel);
                    lat.setText(String.valueOf(decimalFormat.format(coord.Latitude)));
                    lng.setText(String.valueOf(decimalFormat.format(coord.Longitude)));

                    Toast.makeText(_baseActivity, "Your Current Location:"+decimalFormat.format(coord.Latitude)+","+decimalFormat.format(coord.Longitude), Toast.LENGTH_SHORT).show();
                }
            }
        });

        }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PERMISSION_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                _GPSManager.refresh();
            }
        }
    }

    private void Initialize() {
        _baseActivity = this;

        if (!checkPermission(this)) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSION_LOCATION_REQUEST_CODE);
        }

        _GPSManager = new GPSManager(this, new GPSLocationListener());

        // Add more data layers here
        _dataLayers = new String[]{getString(R.string.traffic)};
        _dataLayerSelections = new boolean[_dataLayers.length];

        _loadingScreen = new ProgressDialog(this);
        _loadingScreen.setCancelable(false);
        _loadingScreen.setMessage(this.getString(R.string.loading) + "...");

        bingMapsView = (BingMapsView) findViewById(R.id.mapView);

        // Create handler to switch out of Splash screen mode
        final Handler viewHandler = new Handler() {
            public void handleMessage(Message msg) {
//                Toast.makeText(_baseActivity, String.valueOf(msg), Toast.LENGTH_SHORT).show();
            }
        };

        // Add a map loaded event handler
        bingMapsView.setMapLoadedListener(new MapLoadedListener() {
            @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            public void onAvailableChecked() {
                // hide splash screen and go to map
                viewHandler.sendEmptyMessage(0);

                // Add GPS layer
                _gpsLayer = new EntityLayer(Constants.DataLayers.GPS);
                bingMapsView.getLayerManager().addLayer(_gpsLayer);
                bingMapsView.setFitsSystemWindows(true);
                UpdateGPSPin();
                updateMarker();
//                if (bingMapsView.isInEditMode()){
//                    bingMapsView.setEnabled(tr);
//                }

            }
        });


        // Add a entity clicked event handler
        bingMapsView.setEntityClickedListener(new EntityClickedListener() {
            public void onAvailableChecked(String layerName, int entityId) {
                HashMap<String, Object> metadata = bingMapsView
                        .getLayerManager().GetMetadataByID(layerName, entityId);
                DialogLauncher.LaunchEntityDetailsDialog(_baseActivity,
                        metadata);
            }
        });

        // Load the map
        bingMapsView.loadMap(Constants.BingMapsKey,
                _GPSManager.GetCoordinate(), Constants.DefaultGPSZoomLevel);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        int selectedId = item.getItemId();

        // Map Mode menu items
        switch (selectedId) {
            case R.id.roadBtn:
                bingMapsView.setMapStyle(MapStyles.Road);
                item.setChecked(!item.isChecked());
                return true;
            case R.id.aerialBtn:
                bingMapsView.setMapStyle(MapStyles.Aerial);
                item.setChecked(!item.isChecked());
                return true;
            case R.id.streetSideBtn:
                bingMapsView.setMapStyle(MapStyles.StreetSide);
                item.setChecked(!item.isChecked());
                return true;
            case R.id.ordnanceSurveyBtn:
                bingMapsView.setMapStyle(MapStyles.OrdnanceSurvey);
                item.setChecked(!item.isChecked());
                return true;
            case R.id.canvasDarkBtn:
                bingMapsView.setMapStyle(MapStyles.CanvasDark);
                item.setChecked(!item.isChecked());
                return true;
            case R.id.canvasLightBtn:
                bingMapsView.setMapStyle(MapStyles.CanvasLight);
                item.setChecked(!item.isChecked());
                return true;
            case R.id.grayscaleBtn:
                bingMapsView.setMapStyle(MapStyles.Grayscale);
                item.setChecked(!item.isChecked());
                return true;
            case R.id.mercatorBtn:
                bingMapsView.setMapStyle(MapStyles.Mercator);
                item.setChecked(!item.isChecked());
                return true;
        }
        // More option items
        if (selectedId == R.id.aboutMenuBtn) {
            DialogLauncher.LaunchAboutDialog(this);
            return true;
        }

        if (selectedId == R.id.clearMapMenuBtn) {
            bingMapsView.getLayerManager().clearLayer(null);

            // unselect all layers
            for (int i = 0; i < _dataLayerSelections.length; i++) {
                _dataLayerSelections[i] = false;
            }

            // re-add GPS layer
            bingMapsView.getLayerManager().clearLayer(Constants.DataLayers.GPS);
            UpdateGPSPin();
            return true;
        }

        // Search Menu Item
        if (selectedId == R.id.searchMenuBtn) {
            DialogLauncher.LaunchSearchDialog(this, bingMapsView,
                    loadingScreenHandler);
            return true;
        }
        // Directions Menu Item
        if (selectedId == R.id.directionsMenuBtn) {
            DialogLauncher.LaunchDirectionsDialog(this, bingMapsView,
                    loadingScreenHandler);
            return true;
        }

        if (selectedId == R.id.overrideCultureBtn) {
            DialogLauncher.LaunchOverrideCultureDialog(this, bingMapsView);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void UpdateGPSPin() {
        Coordinate coordinate = bingMapsView.getCenter();
        PushpinOptions opt = new PushpinOptions();
        opt.Icon = Constants.PushpinIcons.GPS;
        Pushpin p = new Pushpin(coordinate, opt);
        if (p.Location != null && _gpsLayer != null) {
            _gpsLayer.clear();
            _gpsLayer.add(p);
            _gpsLayer.updateLayer();
        }
        if (coordinate != null) {
            lat.setText(String.valueOf(decimalFormat.format(coordinate.Latitude)));
            lng.setText(String.valueOf(decimalFormat.format(coordinate.Longitude)));
        }
    }

    public void updateMarker() {
        List<Coordinate> listCoord = new ArrayList<>();
        // EntityLayer is used for map overlay
        EntityLayer entityLayer = (EntityLayer) bingMapsView.getLayerManager()
                .getLayerByName(Constants.DataLayers.Search);
        if (entityLayer == null) {
            entityLayer = new EntityLayer(Constants.DataLayers.Search);
        }
        entityLayer.clear();
        // Use Pushpin to mark on the map
        // PushpinOptions is used to set attributes for Pushpin
        // opt.Icon - The icon of PushPin, opt.Anchor - The position to display Pushpin
        PushpinOptions opt = new PushpinOptions();
        opt.Icon = Constants.PushpinIcons.RedFlag;
        opt.Width = 22;
        opt.Height = 35;
        opt.Anchor = new Point(11, 10);

        // Add the entityLayer to mapView's LayerManager
        bingMapsView.getLayerManager().addLayer(entityLayer);
        entityLayer.updateLayer();

        // set the center location and zoom level of map
        Coordinate coordinate = bingMapsView.getCenter();
        bingMapsView.setCenterAndZoom(coordinate, Constants.DefaultSearchZoomLevel);
        if (coordinate!=null){
            lat.setText(String.valueOf(decimalFormat.format(coordinate.Latitude)));
            lng.setText(String.valueOf(decimalFormat.format(coordinate.Longitude)));
        }

        // Polyline used to draw lines on the MapView
        // PolylineOptions have multiple attributes for the line
        // polylineOptions.StrokeThickness
        // polylineOptions.StrokeColor
        Polyline routeLine = new Polyline(listCoord);
        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.StrokeThickness = 3;
        routeLine.Options = polylineOptions;
        entityLayer.add(routeLine);
    }

    @Override
    public void onAvailableChecked() {

    }

    public class GPSLocationListener implements LocationListener {
        public void onLocationChanged(Location arg0) {
            UpdateGPSPin();
            _GPSManager.refresh();
            updateMarker();

        }

        public void onProviderDisabled(String arg0) {
        }

        public void onProviderEnabled(String arg0) {
        }

        public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        bingMapsView.setCenterAndZoom(_GPSManager.GetCoordinate(),Constants.DefaultGPSZoomLevel);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Coordinate coordinate = bingMapsView.getCenter();
        bingMapsView.setCenterAndZoom(coordinate,Constants.DefaultGPSZoomLevel);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Coordinate coordinate = bingMapsView.getCenter();
        bingMapsView.setCenterAndZoom(coordinate,Constants.DefaultGPSZoomLevel);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bingMapsView.stopLoading();
    }
}