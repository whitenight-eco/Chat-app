import React from 'react';
import Modal from 'react-native-modal';
import {StyleSheet, View, Text} from 'react-native';
import Icon from 'react-native-vector-icons/Feather';

import {observer} from 'mobx-react';
import MainStore from 'src/store/MainStore';

import useHNavigation from 'src/hooks/useHNavigation';

const LoginSuccessModal = () => {
  const {loginSuccessModalVisible} = MainStore;

  const navigation = useHNavigation();

  const hideModal = () => {
    MainStore.hideLoginSuccessModal();
    navigation.navigate('Home');
  };

  return (
    <Modal
      isVisible={loginSuccessModalVisible}
      onBackdropPress={hideModal}
      backdropOpacity={1.0}
      style={styles.modal}
      animationIn="zoomInUp"
      animationOut="zoomOut">
      <View style={styles.modalContainer}>
        <View style={styles.modalIconWrapper}>
          <Icon name="check" size={70} color="#FFFDFD" />
        </View>
        <Text style={styles.modalHealine}>Login Success</Text>
        <Text style={styles.modalDescription}>Welcome to Cypher!</Text>
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
  },
  modalDescription: {
    fontFamily: 'Poppins-SemiBold',
    fontSize: 14,
    color: '#F2F2F2',
    marginBottom: 30,
  },
});

export default observer(LoginSuccessModal);
