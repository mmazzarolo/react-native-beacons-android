package com.mmazzarolo.beaconsandroid;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
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
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BeaconsAndroidModule extends ReactContextBaseJavaModule {
    @Override
    public String getName() {
        return "BeaconsAndroidModule";
    }

    private static final String LOG_TAG = "BeaconsAndroid";
    private static final String BEACON_SPEC = "m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24";
    private static final String SUPPORTED = "SUPPORTED";
    private static final String NOT_SUPPORTED_MIN_SDK = "NOT_SUPPORTED_MIN_SDK";
    private static final String NOT_SUPPORTED_BLE = "NOT_SUPPORTED_BLE";
    private static final String NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS";
    private static final String NOT_SUPPORTED_CANNOT_GET_ADVERTISER = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER";
    private static ReactApplicationContext context;
    private static android.content.Context applicationContext;

    BeaconManager beaconManager;

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

    public BeaconsAndroidModule(ReactApplicationContext reactContext, android.content.Context applicationContext) {
        super(reactContext);
        Log.v(LOG_TAG, "BeaconsAndroidModule - started");
        context = reactContext;
        this.applicationContext = applicationContext;
    }

    @ReactMethod
    public void checkTransmissionSupported(Callback cb) {
        int result = BeaconTransmitter.checkTransmissionSupported(context);
        cb.invoke(result);
    }

    @ReactMethod
    public void startTransmitting(String uuid, ReadableMap params, final Callback onSuccess, final Callback onError) {
        String minor = params.isNull("minor") ? "1" : params.getString("minor");
        String major = params.isNull("major") ? "2" : params.getString("major");
        int manufacturer = params.isNull("manufacturer") ? 0x0118 : params.getInt("manufacturer");

        List<Long> data = Arrays.asList(new Long[]{0l});
        if (params.isNull("data") == false) {
            ReadableArray dataParams = params.getArray("data");
            for (int i = dataParams.size(); i > 0; i -= 1) {
                data.add((long) dataParams.getInt(i - 1));
            }
        }

        Beacon beacon = new Beacon.Builder()
                .setId1(uuid)
                .setId2(minor)
                .setId3(major)
                .setManufacturer(manufacturer)
                .setTxPower(-59)
                .setDataFields(data)
                .build();
        BeaconParser beaconParser = new BeaconParser()
                .setBeaconLayout("m:2-3=beac,i:4-19,i:20-21,i:22-23,p:24-24,d:25-25");
        BeaconTransmitter beaconTransmitter = new BeaconTransmitter(context, beaconParser);
        beaconTransmitter.startAdvertising(beacon, new AdvertiseCallback() {

            @Override
            public void onStartFailure(int errorCode) {
                onError.invoke(errorCode);
            }

            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                onSuccess.invoke();
            }
        });
    }

    private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    /**
     * MONITORING METHODS
     */
    protected String uuid = null;
    protected String beaconUuid = null;

    @ReactMethod
    public void startMonitoring(String uuid, @Nullable String beaconUuid) {
        this.uuid = uuid;
        this.beaconUuid = beaconUuid;
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
        beaconManager.bind(monitoringConsumer);
        sendEvent(context, "startMonitoring", null);
    }

    private BeaconConsumer monitoringConsumer = new BeaconConsumer() {
        @Override
        public void onBeaconServiceConnect() {
            Log.v(LOG_TAG, "monitoringConsumer: onBeaconServiceConnect");
            beaconManager.getBeaconParsers().clear();
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_SPEC));

            try {
                Identifier beaconId = (rangingBeaconUuid == null) ? null : Identifier.parse(beaconUuid);
                beaconManager.startMonitoringBeaconsInRegion(new Region(uuid, beaconId, null, null));
            } catch (RemoteException e) {
            }
            sendEvent(context, "beaconServiceConnect", null);
            beaconManager.setMonitorNotifier(new BootstrapNotifier() {
                @Override
                public Context getApplicationContext() {
                    return applicationContext;
                }

                @Override
                public void didEnterRegion(Region region) {
                    Log.v(LOG_TAG, "monitoringConsumer: didEnterRegion");
                    WritableMap map = Arguments.createMap();
                    sendEvent(context, "didEnterRegion", map);
                }

                @Override
                public void didExitRegion(Region region) {
                    Log.v(LOG_TAG, "monitoringConsumer: didExitRegion");
                    WritableMap map = Arguments.createMap();
                    sendEvent(context, "didExitRegion", map);
                }

                @Override
                public void didDetermineStateForRegion(int i, Region region) {
                    Log.v(LOG_TAG, "monitoringConsumer: didDetermineStateForRegion");
                    WritableMap map = Arguments.createMap();
                    sendEvent(context, "didDetermineStateForRegion", map);
                }
            });
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
    protected String rangingUuid = null;
    protected String rangingBeaconUuid = null;

    @ReactMethod
    public void startRanging(String rangingUuid, @Nullable String rangingBeaconUuid) {
        this.rangingUuid = rangingUuid;
        this.rangingBeaconUuid = rangingBeaconUuid;
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
        beaconManager.bind(rangingConsumer);
        sendEvent(context, "startRanging", null);
    }

    private BeaconConsumer rangingConsumer = new BeaconConsumer() {
        @Override
        public void onBeaconServiceConnect() {
            Log.v(LOG_TAG, "rangingConsumer: onBeaconServiceConnect");
            beaconManager.getBeaconParsers().clear();
            beaconManager.getBeaconParsers().add(new BeaconParser().setBeaconLayout(BEACON_SPEC));
            beaconManager.setRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
                    Log.v(LOG_TAG, "rangingConsumer: didRangeBeaconsInRegion");
                    WritableMap map = new WritableNativeMap();
                    map.putString("uuid", region.getUniqueId());
                    if (beacons.size() > 0) {
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
                        sendEvent(context, "didFoundBeacons", map);
                    }
                }
            });

            try {
                Identifier beaconId = (rangingBeaconUuid == null) ? null : Identifier.parse(rangingBeaconUuid);
                beaconManager.startRangingBeaconsInRegion(new Region(rangingUuid, beaconId, null, null));
            } catch (RemoteException e) {
            }
        }

        @Override
        public Context getApplicationContext() {
            return applicationContext;
        }

        @Override
        public void unbindService(ServiceConnection serviceConnection) {
            Log.v(LOG_TAG, "rangingConsumer: unbindService");
            applicationContext.unbindService(serviceConnection);
        }

        @Override
        public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
            Log.v(LOG_TAG, "rangingConsumer: bindService");
            return applicationContext.bindService(intent, serviceConnection, i);
        }
    };

    @ReactMethod
    public void unbind() {
        Log.v(LOG_TAG, "unbind");
        try {
            beaconManager.unbind(monitoringConsumer);
        } catch (Exception e) {
        }
        try {
            beaconManager.unbind(rangingConsumer);
        } catch (Exception e) {
        }
    }
}
