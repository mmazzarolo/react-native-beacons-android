# react-native-beacons-android
A React-Native library I'm using in production for handling beacons on Android.  
This library works only on Android, for iOS you should use  [react-native-ios](https://www.npmjs.com/package/react-native-ibeacon); I tried to name the events/method like react-native-ibeacon for semplifying Android/iOS code sharing.  
Many thanks to Octavio Turra and its awesome [react-native-alt-beacon lib](https://github.com/octavioturra/react-native-alt-beacon) that I used as a starting point for this module (and that still is the foundation of most of the code of this library).

## Setup  
1. The library is available on npm, install it with: `npm install --save react-native-beacons-android`.  
2. Link the library with your project:
Install [rnpm](https://github.com/rnpm/rnpm) with the command `npm install -g rnpm` and then link the library with the command `rnpm link` 
3. You're done!  

## Usage  
First of all import the library where needed:
```
import Beacons from 'react-native-beacons-android'
```

You can then interact with almost every type of beacons using Beacons methods and events.
  
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
  console.log(`Beacons monitoring started succesfully`)
} catch (error) {
  console.log(`Beacons monitoring not started, error: ${error}`)
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



