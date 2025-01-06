import React, {useState} from 'react';
import {View, Text, StyleSheet} from 'react-native';
import Modal from 'react-native-modal';
import Icon from 'react-native-vector-icons/Feather';

import Layout from 'src/screens/Layout';
import CommonHeader from 'src/components/CommonHeader';
import useHNavigation from 'src/hooks/useHNavigation';

const QrScanScreen = () => {
  const [connectSuccessModalVisible, setConnectSuccessModalVisible] =
    useState(false);

  const navigation = useHNavigation();

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
});

export default QrScanScreen;
