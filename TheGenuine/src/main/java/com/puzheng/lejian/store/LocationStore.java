package com.puzheng.lejian.store;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Handler;
import android.util.Pair;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.orhanobut.logger.Logger;
import com.puzheng.deferred.Deferrable;
import com.puzheng.deferred.Deferred;
import com.puzheng.deferred.LazyDeferred;
import com.puzheng.lejian.MyApp;
import com.puzheng.lejian.R;

public class LocationStore {

    public static final String LOCATION = "LOCATION";
    private static volatile LocationStore instance;
    private AMapLocationClient locationClient;
    private double lng;
    private double lat;
    private String errInfo;
    private int errCode;

    private LocationStore() {

    }

    public static synchronized LocationStore getInstance() {
        if (instance == null) {
            instance = new LocationStore();
        }
        return instance;
    }

    public void setup(Context context) {
        locationClient = new AMapLocationClient(context);
        locationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation location) {
                if (location != null) {
                    if (location.getErrorCode() == 0) {
                        lng = location.getLongitude();
                        lat = location.getLatitude();
                        // TODO: 16-1-29 should only broadcast when move in certain meters
                        Logger.i("located at " + String.format("%f,%f", lng, lat));
                        Intent intent = new Intent(String.valueOf(R.id.BROADCAST_LOCATION_ACTION));
                        intent.putExtra(LOCATION, new Location(location));
                        MyApp.getContext().sendBroadcast(intent);
                    } else {
                        //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                        errCode = location.getErrorCode();
                        errInfo = location.getErrorInfo();
                        Logger.e("AmapError" + "location Error, ErrCode:"
                                + location.getErrorCode() + ", errInfo:"
                                + location.getErrorInfo());
                    }
                }
            }
        });
        AMapLocationClientOption aMapLocationClientOption = new AMapLocationClientOption();
        aMapLocationClientOption.setInterval(30000);
        locationClient.setLocationOption(aMapLocationClientOption);
        locationClient.startLocation();
    }

    public Deferrable<Pair<Double, Double>, Pair<Integer, String>> getLocation() {
        return new LazyDeferred<Pair<Double, Double>, Pair<Integer, String>>() {
            @Override
            public void onStart() {
                if (lng != 0 && lat != 0) {
                    resolve(Pair.create(lng, lat));
                } else {
                    reject(Pair.create(errCode, errInfo));
                }
            }
        };
    }

}
