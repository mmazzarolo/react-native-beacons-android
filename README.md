<img src="http://insideoutdoor.com/wp-content/uploads/2016/02/beacon-example31.png" width="130" align="left"> 
# react-native-beacons-android
A React-Native library for handling beacons on Android.  
<br/>
<br/>

This library works only on Android.  
On iOS you should use  [react-native-ibeacon](https://www.npmjs.com/package/react-native-ibeacon) (I tried to name the events/method like react-native-ibeacon for semplifying Android/iOS code sharing).  

<br/>

## Setup  
1. The library is available on npm, install it with: `npm install --save react-native-beacons-android`.  
2. Link the library with your project:  
If you're using React-Native < 0.29 install [rnpm](https://github.com/rnpm/rnpm) with the command `npm install -g rnpm` and then link the library with the command `rnpm link`.  
If you're using React-Native >= 0.29 just link the library with the command `react-native link`.
3. You're done!  

<br/>

## Beta release 
If you're interested you can find even more features (not documented in the README yet) in the new [2.0.0-beta release](
https://github.com/mmazzarolo/react-native-beacons-android/issues/20)

<br/>

## A simple example
The following example will start detecting all the close iBeacons.  
```javascript
import { DeviceEventEmitter } from 'react-native'
import Beacons from 'react-native-beacons-android'

// Tells the library to detect iBeacons
Beacons.detectIBeacons()

// Start detecting all iBeacons in the nearby
Beacons.startRangingBeaconsInRegion('REGION1')
  .then(() => console.log(`Beacons ranging started succesfully!`))
  .catch(error => console.log(`Beacons ranging not started, error: ${error}`))

// Print a log of the detected iBeacons (1 per second)
DeviceEventEmitter.addListener('beaconsDidRange', (data) => {
  console.log('Found beacons!', data.beacons)
})
```

<br/>

## Example project
You can find an example project using `react-native-ibeacon` + `react-native-beacons-android` [here] (https://github.com/MacKentoch/reactNativeBeaconExample) (thanks to [MacKentoch](https://github.com/MacKentoch)).    

<br/>

## Usage on Android 6 (Marshmallow)
Detecting beacons on Android 6 requires runtime permission, which currently aren't supported natively by React-Native.  
While waiting for an official solution you can use [react-native-permissions](https://github.com/yonahforst/react-native-permissions) (more information regarding this topic [here](https://github.com/mmazzarolo/react-native-beacons-android/issues/15)). Thanks to [@alessandro-bottamedi](https://github.com/alessandro-bottamedi) for investigating on it.

<br/>

## Usage details
**1. Import the library**
```javascript
import Beacons from 'react-native-beacons-android'
```

**2. Detect a custom beacon layout (optional)**   
A beacon layout is a string (for example: `m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24`) that tells the library what kind of beacons you want to detect (iBeacons, altBeacons, etc...).  
By default the library can detect only [AltBeacons](http://altbeacon.org/) but you can add any kind of beacon layout you want (you can find the layout of your beacons on Google).  
You can detect a custom beacon layout with:  
```javascript
Beacons.detectCustomBeaconLayout('m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24') // iBeacons layout
```
For sake of simplicity I also added an utility method that you can use for detecting iBeacons:
```javascript
Beacons.detectIBeacons()
```
**3. Start ranging/monitoring for beacons**  
You can use this library for both region monitoring and region ranging.  
If you don't know the difference between monitoring and ranging you can find some informations [here](https://community.estimote.com/hc/en-us/articles/203356607-What-are-region-Monitoring-and-Ranging-).  
```javascript
// Ranging
Beacons.startRangingBeaconsInRegion('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  .then(() => console.log(`Beacons ranging started succesfully`))
  .catch(error => console.log(`Beacons ranging not started, error: ${error}`))

// Monitoring
Beacons.startMonitoringForRegion('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  .then(() => console.log(`Beacons monitoring started succesfully`))
  .catch(error => console.log(`Beacons monitoring not started, error: ${error}`))
```
The parameter `REGION1` that I'm using is an identifier for the scanned region (use whatever you like).  
The parameter `2ba8e073-b782-3957-0947-268e3850lopd` is optional, and is used for limiting the detected beacons to the beacons with that specific UUID (if the parameter is omitted the library will detect any beacons).  

P.S.: You can stop ranging/monitoring by calling `Beacons.stopRangingBeaconsInRegion()` and `Beacons.stopMonitoringForRegion()`

**4. Do something when the beacons are detected!**  
After the ranging/monitoring starts you can get the information of the detected region and beacons using React-Native `DeviceEventEmitter`.  
Ranging will emit a `beaconsDidRange` event, while monitoring will emit a `regionDidEnter`/`regionDidExit` event.  
```javascript
import { DeviceEventEmitter } from 'react-native'

DeviceEventEmitter.addListener('beaconsDidRange', (data) => {
  console.log('Found beacons!', data) // Result of ranging
})
DeviceEventEmitter.addListener('regionDidEnter', (region) => {
  console.log('Entered new beacons region!', region) // Result of monitoring
})
DeviceEventEmitter.addListener('regionDidExit', (region) => {
  console.log('Exited beacons region!', region) // Result of monitoring
})
```
<br/>

## API docs
##### Beacons.detectCustomBeaconLayout(parser: string): void  
Allows the detection of a custom beacon layout.     
For example `Beacons.detectCustomBeaconLayout('m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24')` allows you to detect iBeacons beacons.  
<br />
##### Beacons.detectIBeacons(): void  
Allows the detection of iBeacons.  
It's just like calling `detectCustomBeaconLayout` with the iBeacons layout.  
<br />
##### Beacons.checkTransmissionSupported(): promise  
Checks if the device can use the Bluetooth to detect the beacons.  
```javascript
// Example using then/catch
Beacons.checkTransmissionSupported()
  .then(result => console.log(`TransmissionSupport status: ${result}`))
  .catch(error => console.log(`TransmissionSupport error: ${error}`))

// Example using async/await
try {
  const transmissionSupport = await Beacons.checkTransmissionSupported()
  console.log(`TransmissionSupport status: ${transmissionSupport}`)
} catch (error) {
  console.log(`TransmissionSupport error: ${error}`)
}
``` 
<br />

##### Beacons.startMonitoringForRegion(regionId: string, beaconsUUID: string): promise  
Starts monitoring for beacons.  
The parameter `regionId` must be an unique ID.  
The parameter `beaconsUUID` is optional, it allows you to detect only the beacons with a specific UUID (if `null` every beacon will be detected).  
```javascript
// Example using then/catch
Beacons.startMonitoringForRegion('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  .then(() => console.log(`Beacons monitoring started succesfully`))
  .catch(error => console.log(`Beacons monitoring not started, error: ${error}`))

// Example using async/await
try {
  await Beacons.startMonitoringForRegion('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  console.log(`Beacons monitoring started succesfully`)
} catch (error) {
  console.log(`Beacons monitoring not started, error: ${error}`)
}
``` 
<br />

##### Beacons.startRangingBeaconsInRegion(regionId: string, beaconsUUID: string): promise    
Starts range scan for beacons.  
The parameter `regionId` must be an unique ID.  
The parameter `beaconsUUID` is optional, it allows you to detect only the beacons with a specific UUID (if `null` every beacon will be detected).   
```javascript
// Example using then/catch
Beacons.startRangingBeaconsInRegion('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  .then(() => console.log(`Beacons ranging started succesfully`))
  .catch(error => console.log(`Beacons ranging not started, error: ${error}`))

// Example using async/await
try {
  await Beacons.startRangingBeaconsInRegion('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  console.log(`Beacons ranging started succesfully`)
} catch (error) {
  console.log(`Beacons ranging not started, error: ${error}`)
}
``` 
<br />

##### Beacons.stopMonitoringForRegion(): promise  
Stops the monitoring for beacons.  
```javascript
// Example using then/catch
Beacons.stopMonitoringForRegion()
  .then(() => console.log(`Beacons monitoring stopped succesfully`))
  .catch(error => console.log(`Beacons monitoring stopped with an error: ${error}`))

// Example using async/await
try {
  await Beacons.stopMonitoringForRegion()
  console.log(`Beacons monitoring stopped succesfully`)
} catch (error) {
  console.log(`Beacons monitoring stopped with an error: ${error}`)
}
``` 
<br />

##### Beacons.stopRangingBeaconsInRegion(): promise  
Stops the range scan for beacons. 
```javascript
// Example using then/catch
Beacons.stopRangingBeaconsInRegion()
  .then(() => console.log(`Beacons ranging stopped succesfully`))
  .catch(error => console.log(`Beacons ranging stopped with an error: ${error}`))

// Example using async/await
try {
  await Beacons.stopRangingBeaconsInRegion()
  console.log(`Beacons ranging stopped succesfully`)
} catch (error) {
  console.log(`Beacons ranging stopped with an error: ${error}`)
}
``` 
<br/>

##### Beacons.setForegroundScanPeriod(period: number): void  
Sets the duration in milliseconds of each Bluetooth LE scan cycle to look for beacons (in foreground).  
For more info [take a look at the official docs](https://altbeacon.github.io/android-beacon-library/javadoc/index.html)
<br/>
<br/>
  
##### Beacons.setBackgroundScanPeriod(period: number): void  
Sets the duration in milliseconds of each Bluetooth LE scan cycle to look for beacons (in background).  
For more info [take a look at the official docs](https://altbeacon.github.io/android-beacon-library/javadoc/index.html)
<br/>
<br/>

##### Beacons.setBackgroundBetweenScanPeriod(period: number): void  
Sets the duration in milliseconds spent not scanning between each Bluetooth LE scan cycle when no ranging/monitoring clients are in the foreground.  
For more info [take a look at the official docs](https://altbeacon.github.io/android-beacon-library/javadoc/index.html)
<br/>
<br/>
