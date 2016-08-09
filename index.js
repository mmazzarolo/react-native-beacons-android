const ReactNative = require('react-native')

const beaconsAndroid = ReactNative.NativeModules.BeaconsAndroidModule

const PARSER_IBEACON = 'm:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24'
const PARSER_ESTIMOTE = 'm:2-3=0215,i:4-19,i:20-21,i:22-23,p:24-24'

const tramissionSupport = {
  0: 'SUPPORTED',
  1: 'NOT_SUPPORTED_MIN_SDK',
  2: 'NOT_SUPPORTED_BLE',
  3: 'DEPRECATED_NOT_SUPPORTED_MULTIPLE_ADVERTISEMENTS',
  4: 'NOT_SUPPORTED_CANNOT_GET_ADVERTISER',
  5: 'NOT_SUPPORTED_CANNOT_GET_ADVERTISER_MULTIPLE_ADVERTISEMENTS'
}

const detectIBeacons = () => {
  beaconsAndroid.addParser(PARSER_IBEACON)
}

const detectEstimotes = () => {
  beaconsAndroid.addParser(PARSER_ESTIMOTE)
}

const detectCustomBeaconLayout = (parser) => {
  beaconsAndroid.addParser(parser)
}

const setBackgroundScanPeriod = (period) => {
  beaconsAndroid.setBackgroundScanPeriod(period)
}

const setBackgroundBetweenScanPeriod = (period) => {
  beaconsAndroid.setBackgroundBetweenScanPeriod(period)
}

const setForegroundScanPeriod = (period) => {
  beaconsAndroid.setForegroundScanPeriod(period)
}

const getRangedRegions = () => new Promise((resolve, reject) => {
  beaconsAndroid.getRangedRegions(resolve)
})

const getMonitoredRegions = () => new Promise((resolve, reject) => {
  beaconsAndroid.getMonitoredRegions(resolve)
})

const checkTransmissionSupported = () => new Promise((resolve, reject) => {
  beaconsAndroid.checkTransmissionSupported(status => resolve(tramissionSupport[status]))
})

const startMonitoringForRegion = (regionId, beaconsUUID) => new Promise((resolve, reject) => {
  beaconsAndroid.startMonitoring(regionId, beaconsUUID, resolve, reject)
})

const startRangingBeaconsInRegion = (regionId, beaconsUUID) => new Promise((resolve, reject) => {
  beaconsAndroid.startRanging(regionId, beaconsUUID, resolve, reject)
})

const stopMonitoringForRegion = (regionId, beaconsUUID) => new Promise((resolve, reject) => {
  beaconsAndroid.stopMonitoring(regionId, beaconsUUID, resolve, reject)
})

const stopRangingBeaconsInRegion = (regionId, beaconsUUID) => new Promise((resolve, reject) => {
  beaconsAndroid.stopRanging(regionId, beaconsUUID, resolve, reject)
})

module.exports = {
  detectIBeacons,
  detectEstimotes,
  detectCustomBeaconLayout,
  setBackgroundScanPeriod,
  setBackgroundBetweenScanPeriod,
  setForegroundScanPeriod,
  checkTransmissionSupported,
  getRangedRegions,
  getMonitoredRegions,
  startMonitoringForRegion,
  startRangingBeaconsInRegion,
  stopMonitoringForRegion,
  stopRangingBeaconsInRegion
}
