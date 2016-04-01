# react-native-beacons-android
A temporary implementation of [react-native-alt-beacon](https://github.com/octavioturra/react-native-alt-beacon) of Octavio Turra.  I'm just hosting the library for testing purpose (NPM + RNPM)

--

## RNAAltBeacon

A work-in-progress lib to work with AltBeacon and React Native.

## IOS

No donuts for you. I can't make a functional ios version.

For test purpose, you can install AltBeacon for ios into your project and add iOs folder content.
It doesn't emit signal nor errors..

## Android

Can transmit and receive beacon data.

### Methods:

- checkTransmissionSupported(cb:Function)
- startTransmitting(uuid:String, params:Object, onSuccess:Function, onError:Function)
- startMonitoring(uuid:String)
- startRanging(uuid:String)

### Events

- startMonitoring
- startRanging
- didEnterRegion
- didExitRegion
- didDetermineStateForRegion
- didFoundBeacons
