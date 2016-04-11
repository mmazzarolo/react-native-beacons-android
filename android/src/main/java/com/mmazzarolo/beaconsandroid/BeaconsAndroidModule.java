package com.mmazzarolo.beaconsandroid;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.annotation.Nullable;
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
    private static final String IBEACON_SPEC = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String SUPPORTED = "SUPPORTED";
    private static final String NOT_SUPPORTED_MIN_SDK = "NOT_SUPPORTED_MIN_SDK";
    private static final String NOT_SUPPORTED_BLE = "NOT_SUPPORTED_BLE";
    private static final String NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS";
    private static final String NOT_SUPPORTED_CANNOT_GET_ADVERTISER = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER";
    private static ReactApplicationContext reactContext;
    private static android.content.Context applicationContext;
    BeaconManager beaconManager;

    public BeaconsAndroidModule(ReactApplicationContext reactContext) {
        super(reactContext);
        Log.v(LOG_TAG, "BeaconsAndroidModule - started");
        this.reactContext = reactContext;
        this.applicationContext = reactContext.getApplicationContext();
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
        beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(IBEACON_SPEC));
    }

    @Override
    public String getName() {
        return LOG_TAG;
    }

    @Override
    public Map<String, Object> getConstants() {
        final Map<String, Object> constants = new HashMap<>();
        constants.put(SUPPORTED, BeaconTransmitter.SUPPORTED);
        constants.put(NOT_SUPPORTED_MIN_SDK, BeaconTransmitter.NOT_SUPPORTED_MIN_SDK);
        constants.put(NOT_SUPPORTED_BLE, BeaconTransmitter.NOT_SUPPORTED_BLE);
        constants.put(NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS, BeaconTransmitter.NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS);
        constants.put(NOT_SUPPORTED_CANNOT_GET_ADVERTISER, BeaconTransmitter.NOT_SUPPORTED_CANNOT_GET_ADVERTISER);
        return constants;
    }


    @ReactMethod
    public void checkTransmissionSupport(Callback cb) {
        int result = BeaconTransmitter.checkTransmissionSupported(reactContext);
        Log.v(LOG_TAG, "checkTransmissionSupport - result: " + result);
        cb.invoke(result);
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    /**
     * MONITORING METHODS
     */
    protected String monitoringRegionId = null;
    protected String monitoringBeaconUuid = null;

    @ReactMethod
    public void startMonitoring(String monitoringRegionId, String monitoringBeaconUuid) {
        Log.v(LOG_TAG, "startMonitoring - monitoringRegionId: " + monitoringRegionId);
        Log.v(LOG_TAG, "startMonitoring - rangingBeaconUuid: " + rangingBeaconUuid);
        this.monitoringRegionId = monitoringRegionId;
        this.monitoringBeaconUuid = monitoringBeaconUuid;
        beaconManager.bind(monitoringConsumer);
        sendEvent(reactContext, "startMonitoring", null);
    }

    private BeaconConsumer monitoringConsumer = new BeaconConsumer() {
        @Override
        public void onBeaconServiceConnect() {
            Log.v(LOG_TAG, "monitoringConsumer - onBeaconServiceConnect");
            sendEvent(reactContext, "beaconServiceConnect", null);
            beaconManager.setMonitorNotifier(new BootstrapNotifier() {
                @Override
                public Context getApplicationContext() {
                    return applicationContext;
                }

                @Override
                public void didEnterRegion(Region region) {
                    Log.v(LOG_TAG, "monitoringConsumer - didEnterRegion");
                    WritableMap map = Arguments.createMap();
                    sendEvent(reactContext, "didEnterRegion", map);
                }

                @Override
                public void didExitRegion(Region region) {
                    Log.v(LOG_TAG, "monitoringConsumer - didExitRegion");
                    WritableMap map = Arguments.createMap();
                    sendEvent(reactContext, "didExitRegion", map);
                }

                @Override
                public void didDetermineStateForRegion(int i, Region region) {
                    Log.v(LOG_TAG, "monitoringConsumer - didDetermineStateForRegion");
                    WritableMap map = Arguments.createMap();
                    sendEvent(reactContext, "didDetermineStateForRegion", map);
                }
            });

            try {
                Identifier beaconId = (rangingBeaconUuid == null) ? null : Identifier.parse(monitoringBeaconUuid);
                beaconManager.startMonitoringBeaconsInRegion(new Region(monitoringRegionId, beaconId, null, null));
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "startMonitoringBeaconsInRegion error: ", e);
            }
        }

        @Override
        public Context getApplicationContext() {
            return applicationContext;
        }

        @Override
        public void unbindService(ServiceConnection serviceConnection) {
            Log.v(LOG_TAG, "monitoringConsumer: unbindService");
            applicationContext.unbindService(serviceConnection);
        }

        @Override
        public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
            Log.v(LOG_TAG, "monitoringConsumer: bindService");
            return applicationContext.bindService(intent, serviceConnection, i);
        }
    };

    /**
     * RANGING METHODS
     */
    protected String rangingRegionId = null;
    protected String rangingBeaconUuid = null;

    @ReactMethod
    public void startRanging(String rangingRegionId, String rangingBeaconUuid) {
        Log.v(LOG_TAG, "startRanging - rangingRegionId: " + rangingRegionId);
        Log.v(LOG_TAG, "startRanging - rangingBeaconUuid: " + rangingBeaconUuid);
        this.rangingRegionId = rangingRegionId;
        this.rangingBeaconUuid = rangingBeaconUuid;
        beaconManager.bind(rangingConsumer);
        sendEvent(reactContext, "startRanging", null);
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

    private BeaconConsumer rangingConsumer = new BeaconConsumer() {
        @Override
        public void onBeaconServiceConnect() {
            Log.v(LOG_TAG, "rangingConsumer - onBeaconServiceConnect");
            beaconManager.setRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    Log.v(LOG_TAG, "rangingConsumer - didRangeBeaconsInRegion, beacons: " + beacons.toString());
                    Log.v(LOG_TAG, "rangingConsumer - didRangeBeaconsInRegion, region: " + region.toString());
                    if (beacons.size() > 0) {
                        WritableMap map = createBeaconsArray(beacons, region);
                        sendEvent(reactContext, "didFoundBeacons", map);
                    }
                }
            });

            try {
                Identifier beaconId = (rangingBeaconUuid == null) ? null : Identifier.parse(rangingBeaconUuid);
                beaconManager.startRangingBeaconsInRegion(new Region(rangingRegionId, null, null, null));
            } catch (RemoteException e) {
                Log.e(LOG_TAG, "startRangingBeaconsInRegion error: ", e);
            }
        }

        @Override
        public Context getApplicationContext() {
            return applicationContext;
        }

        @Override
        public void unbindService(ServiceConnection serviceConnection) {
            Log.v(LOG_TAG, "rangingConsumer - unbindService");
            applicationContext.unbindService(serviceConnection);
        }

        @Override
        public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
            Log.v(LOG_TAG, "rangingConsumer - bindService");
            return applicationContext.bindService(intent, serviceConnection, i);
        }
    };

    @ReactMethod
    public void unbind() {
        Log.v(LOG_TAG, "unbind");
        try {
            beaconManager.unbind(monitoringConsumer);
        } catch (Exception e) {
            Log.e(LOG_TAG, "monitoringConsumer unbind error: ", e);
        }
        try {
            beaconManager.unbind(rangingConsumer);
        } catch (Exception e) {
            Log.e(LOG_TAG, "rangingConsumer unbind error: ", e);
        }
    }
}
