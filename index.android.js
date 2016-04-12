import { NativeModules } from 'react-native'

const beaconsAndroid = NativeModules.BeaconsAndroidModule

export const parsers = {
  PARSER_IBEACON = 'm:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24'
}

const checkTransmissionSupported = () =>
  beaconsAndroid.checkTransmissionSupported()

const startMonitoring = (regionId, beaconsUUID) =>
  beaconsAndroid.startMonitoring(regionId, beaconsUUID)

const startRanging = (regionId, beaconsUUID) =>
  beaconsAndroid.startRanging(regionId, beaconsUUID)

export default {
  checkTransmissionSupported,
  startMonitoring,

}
