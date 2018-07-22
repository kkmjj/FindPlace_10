package com.hanium.findplace.findplace_10.navermap;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.hanium.findplace.findplace_10.R;
import com.nhn.android.maps.NMapCompassManager;
import com.nhn.android.maps.NMapContext;
import com.nhn.android.maps.NMapController;
import com.nhn.android.maps.NMapLocationManager;
import com.nhn.android.maps.NMapView;
import com.nhn.android.maps.maplib.NGeoPoint;
import com.nhn.android.maps.nmapmodel.NMapError;
import com.nhn.android.mapviewer.overlay.NMapMyLocationOverlay;
import com.nhn.android.mapviewer.overlay.NMapOverlayManager;

public class MyNaverMapFragment extends NMapFragment implements NMapView.OnMapStateChangeListener {

    private static final String CLIENT_ID = "uNwZ0jwOscgSGh264TFc";
    private NMapController nMapController;
    private NMapLocationManager nMapLocationManager;
    private NMapCompassManager nMapCompassManager;
    private NMapMyLocationOverlay nMapMyLocationOverlay;
    private NMapView nMapView;
    private NMapContext nMapContext;
    private NaverPointToAddress naverPointToAddress;

    private int LocationCounting = 10;


    public MyNaverMapFragment() {
        // Required empty public constructor
        naverPointToAddress = new NaverPointToAddress();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        nMapContext =  new NMapContext(super.getActivity());
        nMapContext.onCreate();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        nMapView = (NMapView) getView().findViewById(R.id.naver_mapView);
        nMapView.setClientId(CLIENT_ID);
        nMapView.setClickable(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //NaverMap 띄우기.
        View baseView = inflater.inflate(R.layout.fragment_naver_map_fragment, container, false);

        return baseView;
    }

    @Override
    public void onStart(){
        super.onStart();
        //MapSetting
        nMapView.setOnMapStateChangeListener(this);
        nMapView.setScalingFactor(5, true);
        nMapView.setAutoRotateEnabled(true, true);
        nMapController = nMapView.getMapController();
        startMyLocation();
        nMapLocationManager.setOnLocationChangeListener(onMyLocationChangeListener);
        markingMyLocation();
    }

    @Override
    public void onMapInitHandler(NMapView nMapView, NMapError nMapError) {
        if(nMapError == null){

        }
    }

    private void startMyLocation(){
        nMapLocationManager = new NMapLocationManager(getActivity());
        boolean isMyLocationEnabled = nMapLocationManager.enableMyLocation(true);       //내 위치 탐색
        //nMapLocationmanager 에 firstSetupLocationChangeListener(처음 위치찾기 고정만) / onMyLocationChangeListener 세팅
        nMapLocationManager.setOnLocationChangeListener(firstSetupLocationChangeListener);
        if(!isMyLocationEnabled){ // 내 위치 인식 못할때
            Toast.makeText(getActivity(), "Please enable a MyLocation source in system settings", Toast.LENGTH_SHORT).show();
        }else{

        }

    }

    private void markingMyLocation(){
        //---------------------------------------------------------------여기서부터작업!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        nMapCompassManager = new NMapCompassManager(getActivity());
        NMapOverlayManager nMapOverlayManager = new NMapOverlayManager(getActivity(),nMapView, new NaverMapResource(getActivity()));
        nMapMyLocationOverlay = nMapOverlayManager.createMyLocationOverlay(nMapLocationManager, nMapCompassManager);
        nMapMyLocationOverlay.setCompassHeadingVisible(true);
    }

    private NMapLocationManager.OnLocationChangeListener firstSetupLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {
        @Override
        public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint myLocation) {

            if(nMapController != null){
                //처음 지도 띄울때 좌표고정.
                nMapController.animateTo(myLocation);
            }
            //맨 처음 한번만 위치찾기 호출하고 중단
            return false;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {

        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {

        }
    };

    private final NMapLocationManager.OnLocationChangeListener onMyLocationChangeListener = new NMapLocationManager.OnLocationChangeListener() {
        @Override
        public boolean onLocationChanged(NMapLocationManager nMapLocationManager, NGeoPoint myLocation) {

            if(nMapController != null){
                //위치가 변화할때 이 메서드가 호출됨
                //LoginStatus_Main.mapView.setText("현재위치좌표 : "+myLocation.toString());
                if(LocationCounting >= 10){
                    new NaverPointToAddress(myLocation.getLongitude(), myLocation.getLatitude()).execute();
                    LocationCounting = 0;
                }else{
                    LocationCounting++;
                }

            }
            Log.d("myLog", "myLocation lat : "+myLocation.getLatitude());
            Log.d("myLog", "myLocation lng : "+myLocation.getLongitude());

            return true;
        }

        @Override
        public void onLocationUpdateTimeout(NMapLocationManager nMapLocationManager) {

        }

        @Override
        public void onLocationUnavailableArea(NMapLocationManager nMapLocationManager, NGeoPoint nGeoPoint) {

        }
    };

    @Override
    public void onMapCenterChange(NMapView nMapView, NGeoPoint nGeoPoint) {

    }

    @Override
    public void onMapCenterChangeFine(NMapView nMapView) {

    }

    @Override
    public void onZoomLevelChange(NMapView nMapView, int i) {

    }

    @Override
    public void onAnimationStateChange(NMapView nMapView, int i, int i1) {

    }

}
