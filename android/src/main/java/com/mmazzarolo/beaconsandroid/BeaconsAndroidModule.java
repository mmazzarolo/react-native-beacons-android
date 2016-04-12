package com.mmazzarolo.beaconsandroid;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.telecom.Call;
import android.util.Log;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.WritableNativeArray;
import com.facebook.react.bridge.WritableNativeMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.BeaconTransmitter;
import org.altbeacon.beacon.Identifier;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconsAndroidModule extends ReactContextBaseJavaModule {

    private static final String LOG_TAG = "BeaconsAndroidModule";
    private static ReactApplicationContext reactContext;
    private static Context applicationContext;
    BeaconManager beaconManager;

    public BeaconsAndroidModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.d(LOG_TAG, "BeaconsAndroidModule - started");
        this.reactContext = reactContext;
        this.applicationContext = reactContext.getApplicationContext();
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
    }

    @Override
    public String getName() {
        return LOG_TAG;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put("SUPPORTED", BeaconTransmitter.SUPPORTED);
        constants.put("NOT_SUPPORTED_MIN_SDK", BeaconTransmitter.NOT_SUPPORTED_MIN_SDK);
        constants.put("NOT_SUPPORTED_BLE", BeaconTransmitter.NOT_SUPPORTED_BLE);
        constants.put("NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS", BeaconTransmitter.NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS);
        constants.put("NOT_SUPPORTED_CANNOT_GET_ADVERTISER", BeaconTransmitter.NOT_SUPPORTED_CANNOT_GET_ADVERTISER);
        return constants;
    }

    @ReactMethod
    public void addParser(String parser) {
        Log.d(LOG_TAG, "addParser - parser: " + parser);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(parser));
    }

    @ReactMethod
    public void checkTransmissionSupported(Callback callback) {
        int result = BeaconTransmitter.checkTransmissionSupported(reactContext);
        Log.d(LOG_TAG, "checkTransmissionSupport - result: " + result);
        callback.invoke(result);
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    /*----------------------------------------------------------------------------------------------
     | MONITORING METHODS
     ---------------------------------------------------------------------------------------------*/
    protected String monitoringRegionId;
    protected Identifier monitoringBeaconUuid;
    protected Region monitoringRegion;
    protected BeaconsAndroidConsumer monitoringConsumer;

    @ReactMethod
    public void startMonitoring(String monitoringRegionId, String monitoringBeaconUuid, Callback resolve, Callback reject) {
        Log.d(LOG_TAG, "startMonitoring, monitoringRegionId: " + monitoringRegionId + ", monitoringBeaconUuid: " + monitoringBeaconUuid);
        try {
            this.monitoringRegionId = monitoringRegionId;
            this.monitoringBeaconUuid = (monitoringBeaconUuid == null) ? null : Identifier.parse(monitoringBeaconUuid);
            this.monitoringRegion = new Region(this.monitoringRegionId, this.monitoringBeaconUuid, null, null);
            if (monitoringConsumer == null) {
                monitoringConsumer = createMonitoringConsumer();
            }
            beaconManager.bind(monitoringConsumer);
            resolve.invoke();
            Log.d(LOG_TAG, "startMonitoring, success");
        } catch (Exception e) {
            Log.e(LOG_TAG, "startMonitoring, error: ", e);
            reject.invoke(e.getMessage());
        }
    }

    private BeaconsAndroidConsumer createMonitoringConsumer() {
        return new BeaconsAndroidConsumer(applicationContext) {
            @Override
            public void onBeaconServiceConnect() {
                beaconManager.setMonitorNotifier(new BootstrapNotifier() {
                    @Override
                    public Context getApplicationContext() {
                        return applicationContext;
                    }

                    @Override
                    public void didEnterRegion(Region region) {
                        Log.d(LOG_TAG, "monitoringConsumer didEnterRegion, region: " + region.toString());
                        WritableMap map = Arguments.createMap();
                        map.putString("uuid", region.getUniqueId());
                        map.putString("major", region.getId2() != null ? region.getId2().toHexString() : "");
                        map.putString("minor", region.getId3() != null ? region.getId3().toHexString() : "");
                        sendEvent(reactContext, "regionDidEnter", map);
                    }

                    @Override
                    public void didExitRegion(Region region) {
                        Log.d(LOG_TAG, "monitoringConsumer didExitRegion, region: " + region.toString());
                        WritableMap map = Arguments.createMap();
                        sendEvent(reactContext, "regionDidExit", map);
                    }

                    @Override
                    public void didDetermineStateForRegion(int i, Region region) {
                    }
                });

                try {
                    beaconManager.startMonitoringBeaconsInRegion(monitoringRegion);
                    Log.d(LOG_TAG, "monitoringConsumer, called startMonitoringBeaconsInRegion()");
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "startMonitoringBeaconsInRegion error: ", e);
                }
            }
        };
    }

    @ReactMethod
    public void stopMonitoring(Callback resolve, Callback reject) {
        try {
            beaconManager.stopMonitoringBeaconsInRegion(monitoringRegion);
            monitoringConsumer = null;
            Log.d(LOG_TAG, "stopMonitoring, success");
            resolve.invoke();
        } catch (Exception e) {
            Log.e(LOG_TAG, "stopMonitoring, error: ", e);
            reject.invoke(e.getMessage());
        }
    }

    /*----------------------------------------------------------------------------------------------
     | RANGING METHODS
     ---------------------------------------------------------------------------------------------*/
    protected String rangingRegionId;
    protected Identifier rangingBeaconUuid;
    protected Region rangingRegion;
    protected BeaconsAndroidConsumer rangingConsumer;

    @ReactMethod
    public void startRanging(String rangingRegionId, String rangingBeaconUuid, Callback resolve, Callback reject) {
        Log.d(LOG_TAG, "startRanging, rangingRegionId: " + rangingRegionId + ", rangingBeaconUuid: " + rangingBeaconUuid);
        try {
            this.rangingRegionId = rangingRegionId;
            this.rangingBeaconUuid = (rangingBeaconUuid == null) ? null : Identifier.parse(rangingBeaconUuid);
            this.rangingRegion = new Region(this.rangingRegionId, this.rangingBeaconUuid, null, null);
            if (rangingConsumer == null) {
                rangingConsumer = createRangingConsumer();
            }
            beaconManager.bind(rangingConsumer);
            resolve.invoke();
            Log.d(LOG_TAG, "startRanging, success");
        } catch (Exception e) {
            Log.e(LOG_TAG, "startRanging, error: ", e);
            reject.invoke(e.getMessage());
        }
    }

    private WritableMap createBeaconsArray(Collection<Beacon> beacons, Region region) {
        WritableMap map = new WritableNativeMap();
        map.putString("uuid", region.getUniqueId());
        WritableArray a = new WritableNativeArray();
        for (Beacon beacon : beacons) {
            WritableMap b = new WritableNativeMap();
            b.putString("id1", beacon.getId1().toHexString());
            b.putString("id2", beacon.getId2().toHexString());
            b.putString("id3", beacon.getId3().toHexString());
            b.putInt("rssi", beacon.getRssi());
            b.putDouble("distance", beacon.getDistance());
            List<Long> dataFields = beacon.getDataFields();
            if (dataFields.size() > 0) {
                List<Long> dfs = new ArrayList<Long>();
                for (Long dataField : dataFields) {
                    dfs.add(dataField);
                }
            }
            List<Long> extraDataFields = beacon.getDataFields();
            if (extraDataFields.size() > 0) {
                List<Long> edfs = new ArrayList<Long>();
                for (Long extraDataField : extraDataFields) {
                    edfs.add(extraDataField);
                }
            }
            a.pushMap(b);
        }
        map.putArray("beacons", a);
        return map;
    }

    private BeaconsAndroidConsumer createRangingConsumer() {
        return new BeaconsAndroidConsumer(applicationContext) {
            @Override
            public void onBeaconServiceConnect() {
                beaconManager.setRangeNotifier(new RangeNotifier() {
                    @Override
                    public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                        Log.d(LOG_TAG, "rangingConsumer didRangeBeaconsInRegion, beacons: " + beacons.toString());
                        Log.d(LOG_TAG, "rangingConsumer didRangeBeaconsInRegion, region: " + region.toString());
                        if (beacons.size() > 0) {
                            WritableMap map = createBeaconsArray(beacons, region);
                            sendEvent(reactContext, "beaconsDidRange", map);
                        }
                    }
                });

                try {
                    beaconManager.startRangingBeaconsInRegion(rangingRegion);
                    Log.d(LOG_TAG, "rangingConsumer, called startRangingBeaconsInRegion()");
                } catch (RemoteException e) {
                    Log.e(LOG_TAG, "startRangingBeaconsInRegion error: ", e);
                }
            }
        };
    }

    @ReactMethod
    public void stopRanging(Callback resolve, Callback reject) {
        try {
            beaconManager.stopRangingBeaconsInRegion(rangingRegion);
            rangingConsumer = null;
            Log.d(LOG_TAG, "stopRanging, success");
            resolve.invoke();
        } catch (Exception e) {
            Log.e(LOG_TAG, "stopRanging, error: ", e);
            reject.invoke(e.getMessage());
        }
    }
}
