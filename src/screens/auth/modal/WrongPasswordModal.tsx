import React from 'react';
import Modal from 'react-native-modal';
import {StyleSheet, View, Text} from 'react-native';
import Icon from 'react-native-vector-icons/Feather';

import {observer} from 'mobx-react';
import MainStore from 'src/store/MainStore';

const WrongPasswordModal = () => {
  const {wrongPasswordModalVisible, timeRemaining} = MainStore;

  const hideModal = () => {
    MainStore.hideWrongPasswordModal();
  };

  return (
    <Modal
      isVisible={wrongPasswordModalVisible}
      onBackdropPress={hideModal}
      backdropOpacity={1.0}
      style={styles.modal}
      animationIn="zoomInUp"
      animationOut="zoomOut">
      <View style={styles.modalContainer}>
        <View style={styles.modalIconWrapper}>
          <Icon name="x" size={70} color="#FFFDFD" />
        </View>
        <Text style={styles.modalHealine}>Incorrect password</Text>
        <Text style={styles.modalDescription}>
          Try again after {timeRemaining} minutes
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
    borderColor: '#FF3F25',
    marginBottom: 20,
  },
  modalHealine: {
    fontFamily: 'Poppins-Bold',
    fontSize: 24,
    color: '#FFF',
    marginBottom: 15,
  },
  modalDescription: {
    fontFamily: 'Poppins-SemiBold',
    fontSize: 14,
    color: '#FF0606',
    marginBottom: 30,
  },
});

export default observer(WrongPasswordModal);
