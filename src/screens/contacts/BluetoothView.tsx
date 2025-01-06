import React, {useState, useEffect} from 'react';
import {View, Text, StyleSheet, Alert} from 'react-native';

import WaringIcon from 'react-native-vector-icons/Ionicons';
import QRCode from 'react-native-qrcode-svg';

import {observer} from 'mobx-react';
import ProfileStore from 'src/store/ProfileStore';

import {Button} from 'src/components/Button/Button';
import useHNavigation from 'src/hooks/useHNavigation';

const BluetoothView = () => {
  const [loading, setLoading] = useState(false);
  const [link, setLink] = useState(ProfileStore.externalLink);

  const [bluetoothOn, setBluetoothOn] = useState(false);
  const [error, setError] = useState(false);

  const navigation = useHNavigation();

  const handleContinue = () => {
    setLoading(true);
    if (!bluetoothOn) {
      setLoading(false);
      setError(true);
    } else {
      setError(false);
      setLoading(false);
      navigation.navigate('QrScan');
    }
  };

  return (
    <>
      <Text style={styles.step}>Scan each other’s QR codes</Text>

      <View style={styles.qrcodeWrapper}>
        <QRCode value={link} size={200} color="black" backgroundColor="white" />
      </View>

      {error && (
        <View style={styles.errorContainer}>
          <WaringIcon name="warning-outline" size={24} color="#FF0404" />
          <Text style={styles.errorText}>
            Make sure Bluetooth turn on in connections setting
          </Text>
        </View>
      )}

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
  step: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    paddingBottom: 20,
  },
  qrcodeWrapper: {
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 50,
    marginBottom: 46,
  },
  errorContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    borderWidth: 1,
    borderColor: '#FF0404',
    borderRadius: 10,
    paddingHorizontal: 5,
    marginTop: 75,
    marginBottom: -116,
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
    marginTop: 175,
    marginBottom: 20,
    backgroundColor: '#05FCFC',
  },
});

export default observer(BluetoothView);
