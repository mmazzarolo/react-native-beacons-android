<img src="http://insideoutdoor.com/wp-content/uploads/2016/02/beacon-example31.png" width="130" align="left"> 
# react-native-beacons-android
A React-Native library for handling beacons on Android.  
<br/>
<br/>


This library works only on Android, for iOS you should use  [react-native-ios](https://www.npmjs.com/package/react-native-ibeacon) (I tried to name the events/method like react-native-ibeacon for semplifying Android/iOS code sharing).  
Many thanks to Octavio Turra and its awesome [react-native-alt-beacon lib](https://github.com/octavioturra/react-native-alt-beacon) that I used as a starting point for this module (and that still is the foundation of most of the code of this library).

## Setup  
1. The library is available on npm, install it with: `npm install --save react-native-beacons-android`.  
2. Link the library with your project:
Install [rnpm](https://github.com/rnpm/rnpm) with the command `npm install -g rnpm` and then link the library with the command `rnpm link` 
3. You're done!  

## A simple example
The following example will start detecting all the near iBeacons.  
```javascript
import { DeviceEventEmitter } from 'react-native'
import Beacons from 'react-native-beacons-android'

// Tells the library to detect iBeacons
Beacons.detectIBeacons()

// Start detecting all iBeacons in the nearby
Beacons.startRangingForRegion('REGION1')
  .then(() => console.log(`Beacons monitoring started succesfully!`)
  .catch(error => console.log(`Beacons monitoring not started, error: ${error}`)

// Print a log of the detected iBeacons (evert 1 second)
DeviceEventEmitter.addListener('beaconsDidRange', (data) => {
  console.log('Found beacons!', data)
})
```

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
Starts ranging:  
```javascript
Beacons.startRangingForRegion('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  .then(() => console.log(`Beacons ranging started succesfully`)
  .catch(error => console.log(`Beacons ranging not started, error: ${error}`)
```
Starts monitoring:  
```javascript
Beacons.startMonitoringForRegion('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  .then(() => console.log(`Beacons monitoring started succesfully`)
  .catch(error => console.log(`Beacons monitoring not started, error: ${error}`)
```
The parameter `REGION1` that I'm using is an identifier for the scanned region (use whatever you like).  
The parameter `2ba8e073-b782-3957-0947-268e3850lopd` is optional, and is used for limiting the detected beacons to the beacons with that specific UUID (if the parameter is omitted the library will detect any beacons).  

P.S.: You can stop ranging/monitoring by calling `Beacons.stopRangingForRegion()` and `Beacons.stopMonitoringForRegion()`

**4. Do something when the beacons are detected!**  
After the ranging/monitoring starts you can get the information of the detected region and beacons using React-Native `DeviceEventEmitter`.  
Ranging will emit a `beaconsDidRange` event, while monitoring will emit a `regionDidEnter`/`regionDidExit` event.  
```javascript
import { DeviceEventEmitter } from 'react-native'
DeviceEventEmitter.addListener('beaconsDidRange', (data) => {
  console.log('Found beacons!', data)
})
DeviceEventEmitter.addListener('regionDidEnter', (region) => {
  console.log('Entered new beacons region!', region)
})
DeviceEventEmitter.addListener('regionDidExit', (region) => {
  console.log('Exited beacons region!', region)
})
```

## API docs
##### Beacons.addParser(parser: string):  
Adds a parser for a specific beacons specification.     
For example `Beacons.addParser('m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24')` allows you to detect iBeacons beacons.  
Add any beacon specification you need to detect.  

##### Beacons.checkTransmissionSupported(): promise:  
Checks if the device can use the Bluetooth to detect the beacons.  
This method returns a promise.  
Example usage (with `.then` and `.catch`):
```javascript
Beacons.checkTransmissionSupported()
  .then(result => console.log(`TransmissionSupport status: ${result}`)
  .catch(error => console.log(`TransmissionSupport error: ${error}`)
```
Example usage (with `async/await`):
```javascript
try {
  const transmissionSupport = await Beacons.checkTransmissionSupported()
  console.log(`TransmissionSupport status: ${transmissionSupport}`)
} catch (error) {
  console.log(`TransmissionSupport error: ${error}`)
}
``` 

##### Beacons.startMonitoring(regionId: string, beaconsUUID: string): promise:  
Starts monitoring for beacons.  
The parameter `regionId` must be an unique ID.  
The parameter `beaconsUUID` is optional, it allows you to detect only the beacons with a specific UUID (if `null` every beacon will be detected).  

This method returns a promise.  
Example usage (with `.then` and `.catch`):
```javascript
Beacons.startMonitoring('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  .then(() => console.log(`Beacons monitoring started succesfully`)
  .catch(error => console.log(`Beacons monitoring not started, error: ${error}`)
```
Example usage (with `async/await`):
```javascript
try {
  await Beacons.startMonitoring('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  console.log(`Beacons ranging started succesfully`)
} catch (error) {
  console.log(`Beacons ranging not started, error: ${error}`)
}
``` 

##### Beacons.startRanging(regionId: string, beaconsUUID: string): promise:  
Starts range scan for beacons.  
The parameter `regionId` must be an unique ID.  
The parameter `beaconsUUID` is optional, it allows you to detect only the beacons with a specific UUID (if `null` every beacon will be detected).  

This method returns a promise.  
Example usage (with `.then` and `.catch`):
```javascript
Beacons.startRanging('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  .then(() => console.log(`Beacons ranging started succesfully`)
  .catch(error => console.log(`Beacons ranging not started, error: ${error}`)
```
Example usage (with `async/await`):
```javascript
try {
  await Beacons.startRanging('REGION1', '2ba8e073-b782-3957-0947-268e3850lopd')
  console.log(`Beacons ranging started succesfully`)
} catch (error) {
  console.log(`Beacons ranging not started, error: ${error}`)
}
``` 

##### Beacons.stopMonitoring(): promise:  
Stops the monitoring for beacons.  

This method returns a promise.  
Example usage (with `.then` and `.catch`):
```javascript
Beacons.stopRanging()
  .then(() => console.log(`Beacons monitoring stopped succesfully`)
  .catch(error => console.log(`Beacons monitoring stopped with an error: ${error}`)
```
Example usage (with `async/await`):
```javascript
try {
  await Beacons.stopRanging()
  console.log(`Beacons monitoring stopped succesfully`)
} catch (error) {
  console.log(`Beacons monitoring stopped with an error: ${error}`)
}
``` 

##### Beacons.stopRanging(): promise:  
Stops the range scan for beacons.  

This method returns a promise.  
Example usage (with `.then` and `.catch`):
```javascript
Beacons.stopRanging()
  .then(() => console.log(`Beacons ranging stopped succesfully`)
  .catch(error => console.log(`Beacons ranging stopped with an error: ${error}`)
```
Example usage (with `async/await`):
```javascript
try {
  await Beacons.stopRanging()
  console.log(`Beacons ranging stopped succesfully`)
} catch (error) {
  console.log(`Beacons ranging stopped with an error: ${error}`)
}
``` 



