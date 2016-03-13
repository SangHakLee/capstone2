package com.capstone2.capstone2;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.skp.Tmap.TMapMarkerItem;
import com.skp.Tmap.TMapView;

public class MainActivity extends AppCompatActivity{

    private static final String TAG = "Main";

    TMapView mapView;
    LocationManager locationManager;
    Location location;
    String mProvider = LocationManager.GPS_PROVIDER;

    private static final String API_KEY = "458a10f5-c07e-34b5-b2bd-4a891e024c2a";


    private static final double LAT = 37.316953; // 반월터널 정보
    private static final double LON = 126.830440; // 반월터널 정보
    private static final double MEASURE_DIST = 700; // 차와 터널 사이에 측정 기준 거리

    /* 차량 연결시 메세지 */ private static final String connectCar= "연결";
    /* 차량 해제시 메세지 */ private static final String disconnectCar= "해제";


    // 아두이노 블루투스 연결
    private BluetoothService btService = null;
    // Intent request code
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;


    private final Handler mHandler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }

    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mapView = (TMapView) findViewById(R.id.mapView);
        mapView.setOnApiKeyListener(new TMapView.OnApiKeyListenerCallback() {
            @Override
            public void SKPMapApikeySucceed() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        setupMap();
                    }
                });
            }

            @Override
            public void SKPMapApikeyFailed(String s) {

            }
        });
        mapView.setSKPMapApiKey(API_KEY);


        locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
        if(locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
//            Toast.makeText(this, "GPS 사용가능", Toast.LENGTH_SHORT).show();
        }else{
            startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), 999);
//            Toast.makeText(this, "GPS 사용불가", Toast.LENGTH_SHORT).show();

        }


        // BluetoothService 클래스 생성
        if(btService == null) {
            btService = new BluetoothService(this, mHandler);
        }else{
            if(btService.getDeviceState()) {
                // 블루투스가 지원 가능한 기기일 때
                btService.enableBluetooth();
            } else {
                finish();
            }
        }

    }



    // 블루투스 하고 넘어 온 값 콜백
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 1: // REQUEST_ENABLE_BT
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // 확인 눌렀을 때
                    //Next Step
                } else {
                    // 취소 눌렀을 때
                    Log.d(TAG, "Bluetooth is not enabled");
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    int setupMapCnt=0;

    // 티맵 이니셜
    boolean isInitialized = false;
    private void setupMap() {
        Toast.makeText(this, "맵 활성화", Toast.LENGTH_SHORT).show();
        Log.v(TAG, " setupMap 호출 :" + (++setupMapCnt) );

        isInitialized = true;
        mapView.setOnCalloutRightButtonClickListener(new TMapView.OnCalloutRightButtonClickCallback() {
            @Override
            public void onCalloutRightButton(TMapMarkerItem tMapMarkerItem) {
                Toast.makeText(MainActivity.this, "marker : " + tMapMarkerItem.getID(), Toast.LENGTH_SHORT).show();

            }
        });
        if (cacheLocation != null) { // 캐쉬 된게 있음
            moveMap(cacheLocation);
            setMyLocation(cacheLocation);
            cacheLocation = null;
        }else{
            // 처음
//            mapView.setLocationPoint(lat, lon); // 현재위치
//            mapView.setCenterPoint(lat, lon);
            Log.v(TAG, " 캐쉬 된게 업음");

        }
        mapView.setSightVisible(true);
//        mapView.setCompassMode(true);

    }


    // 티맵 위치정보에 따른 변화
    private void moveMap(double lat, double lng) {
        mapView.setCenterPoint(lng, lat);
        mapView.setZoomLevel(17);
    }
    private void moveMap(Location location) {
        moveMap(location.getLatitude(), location.getLongitude());
    }

    private void setMyLocation(Location location) {
        mapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        Bitmap icon = ((BitmapDrawable)(ContextCompat.getDrawable(this, R.mipmap.ic_launcher))).getBitmap();
        mapView.setIcon(icon);
        mapView.setIconVisibility(true);
    }

    // 지역정보
    Location cacheLocation;
    LocationListener mListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {

            Log.v(TAG, "onLocationChanged");
            Toast.makeText(MainActivity.this, "위치변견", Toast.LENGTH_SHORT).show();


            if (!isInitialized) { // 처음이 아니면
                Log.v(TAG, "처음이 아니면");
                cacheLocation = location;
                return;
            }
            Log.v("Main", "처음");
            cacheLocation = null;
            moveMap(location);
            setMyLocation(location);
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
    };

    // 환경변수 상수 값
    private static final int RC_PERMISSION = 1;

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        getLocation();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.removeUpdates(mListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, mListener); // 마시멜로 버전은 무조건 실행되지 않고 버전 체크 해야한다.
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            return;
        }
        locationManager.removeUpdates(mListener);
    }

    private void getLocation() {
        Log.v(TAG, "getLocation()");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    !ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSION);
                return;
            }
            Snackbar.make(mapView, "location permission", Snackbar.LENGTH_INDEFINITE)
                    .setAction("OK", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, RC_PERMISSION);
                        }
                    }).show();
            return;
        }
        Log.v(TAG, "getLocation() 2");
        Location location = locationManager.getLastKnownLocation(mProvider);
        if (location != null) {
            Log.v(TAG, "location != null");
            moveMap(location);
        }else{
            Log.v(TAG, "location == null");
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    public int calDistance(double lat1, double lon1, double lat2, double lon2){

        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(deg2rad(lat1)) * Math.sin(deg2rad(lat2)) + Math.cos(deg2rad(lat1))
                * Math.cos(deg2rad(lat2)) * Math.cos(deg2rad(theta));
        dist = Math.acos(dist);
        dist = rad2deg(dist);

        dist = dist * 60 * 1.1515;
        dist = dist * 1.609344;    // 단위 mile 에서 km 변환.
        dist = dist * 1000.0;      // 단위  km 에서 m 로 변환

//        return (int)dist;
        return (int)Math.round(dist);
    }
    // 주어진 도(degree) 값을 라디언으로 변환
    private double deg2rad(double deg){
        return (double)(deg * Math.PI / (double)180d);
    }

    // 주어진 라디언(radian) 값을 도(degree) 값으로 변환
    private double rad2deg(double rad){
        return (double)(rad * (double)180d / Math.PI);
    }
}
