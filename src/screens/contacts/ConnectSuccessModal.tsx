import React from 'react';
import Modal from 'react-native-modal';
import {StyleSheet, View, Text} from 'react-native';
import Icon from 'react-native-vector-icons/Feather';

import {observer} from 'mobx-react';
import MainStore from 'src/store/MainStore';

import useHNavigation from 'src/hooks/useHNavigation';

const ConnectSuccessModal = () => {
  const {connectSuccessModalVisible} = MainStore;

  const navigation = useHNavigation();

  const hideConnectSuccessModalModal = () => {
    MainStore.hideConnectionSuccessModal();
    navigation.navigate('Contacts');
  };

  return (
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
  );
};

const styles = StyleSheet.create({
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

export default observer(ConnectSuccessModal);
