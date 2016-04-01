package com.mmazzarolo.beaconsandroid;

import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.RemoteException;
import android.support.annotation.Nullable;

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
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Region;
import org.altbeacon.beacon.startup.BootstrapNotifier;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeaconsAndroidModule extends ReactContextBaseJavaModule {
    @Override
    public String getName() {
        return "BeaconsAndroidModule";
    }

    private static final String SUPPORTED = "SUPPORTED";
    private static final String NOT_SUPPORTED_MIN_SDK = "NOT_SUPPORTED_MIN_SDK";
    private static final String NOT_SUPPORTED_BLE = "NOT_SUPPORTED_BLE";
    private static final String NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS";
    private static final String NOT_SUPPORTED_CANNOT_GET_ADVERTISER = "NOT_SUPPORTED_CANNOT_GET_ADVERTISER";
    private static ReactApplicationContext context;
    private static android.content.Context applicationContext;

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

    private void sendEvent(ReactContext reactContext,
                           String eventName,
                           @Nullable WritableMap params) {
        reactContext
                .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
                .emit(eventName, params);
    }

    String uuid = null;
    String rangingUuid = null;
    BeaconManager beaconManager;
    private BeaconConsumer monitoringConsumer = new BeaconConsumer() {
        @Override
        public void onBeaconServiceConnect() {

            try {
                beaconManager.startMonitoringBeaconsInRegion(new Region(uuid, null, null, null));
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
                    WritableMap map = Arguments.createMap();
                    sendEvent(context, "didEnterRegion", map);
                }

                @Override
                public void didExitRegion(Region region) {
                    WritableMap map = Arguments.createMap();
                    sendEvent(context, "didExitRegion", map);
                }

                @Override
                public void didDetermineStateForRegion(int i, Region region) {
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
            applicationContext.unbindService(serviceConnection);
        }

        @Override
        public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
            return applicationContext.bindService(intent, serviceConnection, i);
        }
    };
    private BeaconConsumer rangingConsumer = new BeaconConsumer() {
        @Override
        public void onBeaconServiceConnect() {
            beaconManager.setRangeNotifier(new RangeNotifier() {
                @Override
                public void didRangeBeaconsInRegion(Collection<Beacon> beacons, Region region) {
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
                beaconManager.startRangingBeaconsInRegion(new Region(rangingUuid, null, null, null));
            } catch (RemoteException e) {
            }
        }

        @Override
        public Context getApplicationContext() {
            return applicationContext;
        }

        @Override
        public void unbindService(ServiceConnection serviceConnection) {
            applicationContext.unbindService(serviceConnection);
        }

        @Override
        public boolean bindService(Intent intent, ServiceConnection serviceConnection, int i) {
            return applicationContext.bindService(intent, serviceConnection, i);
        }
    };


    @ReactMethod
    public void startMonitoring(String uuid) {
        this.uuid = uuid;
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
        beaconManager.bind(monitoringConsumer);
        sendEvent(context, "startMonitoring", null);
    }

    @ReactMethod
    public void startRanging(String rangingUuid) {
        this.rangingUuid = rangingUuid;
        beaconManager = BeaconManager.getInstanceForApplication(applicationContext);
        beaconManager.bind(rangingConsumer);
        sendEvent(context, "startRanging", null);
    }

    @ReactMethod
    public void unbind() {
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
