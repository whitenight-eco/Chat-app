import React, {useState, useCallback, useEffect} from 'react';
import {View, Text, StyleSheet} from 'react-native';
import Modal from 'react-native-modal';
import Icon from 'react-native-vector-icons/Feather';
import PowerOffIcon from 'react-native-vector-icons/FontAwesome';

import Layout from 'src/screens/Layout';
import CommonHeader from 'src/components/CommonHeader';
import useHNavigation from 'src/hooks/useHNavigation';

import ContactsStore from 'src/store/ContactsStore';

import {
  Code,
  useCameraDevice,
  useCodeScanner,
} from 'react-native-vision-camera';
import {Camera} from 'react-native-vision-camera';

const QrScanScreen = () => {
  const [connectSuccessModalVisible, setConnectSuccessModalVisible] =
    useState(false);
  const [error, setError] = useState(false);
  const [errMsg, setErrMsg] = useState('');

  const navigation = useHNavigation();

  const device = useCameraDevice('back');

  useEffect(() => {
    if (!device) {
      setError(true);
      setErrMsg('Your phone does not have a Camera.');
    } else {
      setError(false);
      setErrMsg('');
    }
  }, [device]);

  const checkConnection = async (link: any) => {
    const result = await ContactsStore.checkLink(link);
    if (result.success) {
      setError(false);
      setConnectSuccessModalVisible(true);
    } else {
      setError(true);
      setErrMsg(result.error || '');
    }
  };

  const onCodeScanned = useCallback((codes: Code[]) => {
    const value = codes[0]?.value;
    console.log(
      JSON.stringify(
        [`Scanned ${codes.length} codes:${codes}`, `value: ${value}`],
        null,
        2,
      ),
    );

    if (value == null) {
      return;
    }
    checkConnection(value);
  }, []);

  // Initialize the Code Scanner to scan QR code
  const codeScanner = useCodeScanner({
    codeTypes: ['qr'],
    onCodeScanned: onCodeScanned,
  });

  const hideConnectSuccessModalModal = () => {
    setConnectSuccessModalVisible(false);
    navigation.navigate('ContactsMain');
  };

  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader name="QR Camera" iconExist={true} />
        <Text style={styles.subtitle}>Let's connect</Text>
        <Text style={styles.description}>
          Scan QR code with another cypher member
        </Text>
        {device != null ? (
          <View style={styles.cameraWrapper}>
            <View style={styles.icon}>
              <PowerOffIcon name="power-off" size={40} color="#CF3010" />
            </View>
            <View style={styles.cameraOutline}>
              <Camera
                device={device}
                style={styles.camera}
                isActive={true}
                codeScanner={codeScanner}
                orientation={'portrait'} //export type Orientation = 'portrait' | 'portrait-upside-down' | 'landscape-left' | 'landscape-right'
              />
            </View>
          </View>
        ) : (
          error && (
            <View style={styles.emptyContainer}>
              <Text style={styles.text}>{errMsg}</Text>
            </View>
          )
        )}
      </View>

      {/* Connect Success Modal */}
      <Modal
        isVisible={connectSuccessModalVisible}
        onBackdropPress={hideConnectSuccessModalModal}
        backdropOpacity={1.0}
        style={styles.modal}
        animationIn="zoomInUp"
        animationOut="zoomOut">
        <View style={styles.modalContainer}>
          <View style={styles.modalIconWrapper}>
            <Icon name="check" size={70} color="#FFFDFD" />
          </View>
          <Text style={styles.modalHealine} numberOfLines={2}>
            You're now{'\n'}Connected!
          </Text>
        </View>
      </Modal>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    flexDirection: 'column',
  },
  subtitle: {
    color: '#FFF',
    fontFamily: 'Poppins-SemiBold',
    fontSize: 20,
    textAlign: 'center',
    marginTop: 30,
  },
  description: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
    textAlign: 'center',
    paddingBottom: 100,
  },
  cameraWrapper: {
    position: 'relative',
    alignItems: 'center',
    justifyContent: 'center',
  },
  icon: {
    position: 'absolute',
    top: -30,
    zIndex: 10,
    alignItems: 'center',
    justifyContent: 'center',
    width: 60,
    height: 60,
    backgroundColor: '#FFF',
    borderRadius: 30,
  },
  cameraOutline: {
    width: 300,
    height: 300,
    borderRadius: 40,
    borderWidth: 40,
    borderColor: 'white',
  },
  camera: {
    width: '100%',
    height: '100%',
  },
  modal: {
    justifyContent: 'center',
    alignItems: 'center',
    margin: 0, // Removes default margin around modal
  },
  modalContainer: {
    width: '80%',
    height: '35%',
    backgroundColor: '#131212',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalIconWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 100,
    height: 100,
    backgroundColor: '#000',
    borderRadius: 50,
    borderWidth: 3,
    borderColor: '#25FFAE',
    marginBottom: 20,
  },
  modalHealine: {
    fontFamily: 'Poppins-Bold',
    fontSize: 24,
    color: '#FFF',
    marginBottom: 15,
    textAlign: 'center',
  },
  text: {
    color: 'red',
    fontFamily: 'Poppins-Bold',
    fontSize: 24,
    textAlign: 'center',
  },
  emptyContainer: {
    justifyContent: 'center',
    alignItems: 'center',
  },
});

export default QrScanScreen;
