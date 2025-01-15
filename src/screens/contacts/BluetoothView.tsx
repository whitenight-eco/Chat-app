import React, {useState, useEffect, useCallback} from 'react';
import {View, Text, StyleSheet, Linking} from 'react-native';

import WaringIcon from 'react-native-vector-icons/Ionicons';
import QRCode from 'react-native-qrcode-svg';
import BluetoothStateManager from 'react-native-bluetooth-state-manager';
import {useCameraDevice} from 'react-native-vision-camera';
import {Camera, CameraPermissionStatus} from 'react-native-vision-camera';

import {observer} from 'mobx-react';
import ProfileStore from 'src/store/ProfileStore';

import {Button} from 'src/components/button';
import useHNavigation from 'src/hooks/useHNavigation';

const BluetoothView = () => {
  const [loading, setLoading] = useState(false);
  const [link, setLink] = useState(ProfileStore.user?.externalLink || '');

  const [bluetoothStatus, setBluetoothStatus] = useState('');
  const [error, setError] = useState(false);
  const [errMsg, setErrMsg] = useState('');

  const navigation = useHNavigation();

  const device = useCameraDevice('back');
  const [cameraPermissionStatus, setCameraPermissionStatus] =
    useState<CameraPermissionStatus>('not-determined');

  const requestCameraPermission = useCallback(async () => {
    const permission = await Camera.requestCameraPermission();
    console.log(`Camera permission status: ${permission}`);

    if (permission === 'denied') await Linking.openSettings();
    if (permission === 'granted') {
      navigation.navigate('QrScan');
    }
    setCameraPermissionStatus(permission);
  }, []);

  const openCodeScanner = () => {
    if (cameraPermissionStatus !== 'granted') {
      requestCameraPermission();
    } else {
      navigation.navigate('QrScan');
    }
  };

  useEffect(() => {
    BluetoothStateManager.getState().then(bluetoothState => {
      switch (bluetoothState) {
        case 'Unknown':
          setErrMsg('Unknown');
          setBluetoothStatus(bluetoothState);
          break;
        case 'Resetting':
          setBluetoothStatus(bluetoothState);
          break;
        case 'Unsupported':
          setErrMsg('Bluetooth unsupported on this device');
          setBluetoothStatus(bluetoothState);
          break;
        case 'Unauthorized':
          setErrMsg('This device is Unauthorized');
          setBluetoothStatus(bluetoothState);
          break;
        case 'PoweredOff':
          setErrMsg('Make sure Bluetooth turn on in connections setting');
          setBluetoothStatus(bluetoothState);
          break;
        case 'PoweredOn':
          setBluetoothStatus(bluetoothState);
          break;
        default:
          break;
      }
    });
  }, []);

  const handleContinue = () => {
    setLoading(true);
    if (bluetoothStatus === 'PoweredOn') {
      setError(false);
      setLoading(false);
      openCodeScanner();
    } else {
      setLoading(false);
      setError(true);
    }
  };

  return (
    <>
      <View style={styles.container}>
        <Text style={styles.step}>Scan each other’s QR codes</Text>

        <View style={styles.qrcodeWrapper}>
          <QRCode
            value={link}
            size={200}
            color="black"
            backgroundColor="white"
          />
        </View>

        {error && (
          <View style={styles.errorContainer}>
            <WaringIcon name="warning-outline" size={24} color="#FF0404" />
            <Text style={styles.errorText}>{errMsg}</Text>
          </View>
        )}
      </View>
      <View style={styles.buttonWrapper}>
        <Button
          style={styles.button}
          text="Continue"
          isLoading={loading}
          onPress={handleContinue}
        />
      </View>
    </>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  step: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
  },
  qrcodeWrapper: {
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 50,
  },
  errorContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#FF0404',
    borderRadius: 10,
    paddingHorizontal: 5,
    marginTop: 100,
    flexWrap: 'wrap',
  },
  errorText: {
    color: '#FEFEFF',
    fontFamily: 'Poppins-Medium',
    textAlign: 'center',
    fontSize: 12,
    lineHeight: 19,
    padding: 10,
    flex: 1,
  },
  buttonWrapper: {
    paddingHorizontal: 20,
  },
  button: {
    backgroundColor: '#05FCFC',
  },
});

export default observer(BluetoothView);
