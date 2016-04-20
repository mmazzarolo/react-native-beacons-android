import { NativeModules } from 'react-native'

const beaconsAndroid = NativeModules.BeaconsAndroidModule

const PARSER_IBEACON= 'm:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24'

const tramissionSupport = {
  0: 'SUPPORTED',
  1: 'NOT_SUPPORTED_MIN_SDK',
  2: 'NOT_SUPPORTED_BLE',
  3: 'DEPRECATED_NOT_SUPPORTED_MULTIPLE_ADVERTISEMENTS',
  4: 'NOT_SUPPORTED_CANNOT_GET_ADVERTISER',
  5: 'NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS'
}

const detectIBeacons = () => beaconsAndroid.addParser(PARSER_IBEACON)

const detectCustomBeaconLayout = (parser) => beaconsAndroid.addParser(parser)

const checkTransmissionSupported = () => new Promise((resolve, reject) => {
  beaconsAndroid.checkTransmissionSupported(status => resolve(tramissionSupport[status]))
})

const startMonitoringForRegion = (regionId, beaconsUUID) => new Promise((resolve, reject) => {
  beaconsAndroid.startMonitoring(regionId, beaconsUUID, resolve, reject)
})

const startRangingBeaconsInRegion = (regionId, beaconsUUID) => new Promise((resolve, reject) => {
  beaconsAndroid.startRanging(regionId, beaconsUUID, resolve, reject)
})

const stopMonitoringForRegion = () => new Promise(beaconsAndroid.stopRanging)

const stopRangingBeaconsInRegion = () => new Promise(beaconsAndroid.stopMonitoring)

export default {
  detectIBeacons,
  detectCustomBeaconLayout,
  checkTransmissionSupported,
  startMonitoringForRegion,
  startRangingBeaconsInRegion,
  stopMonitoringForRegion,
  stopRangingBeaconsInRegion
}
