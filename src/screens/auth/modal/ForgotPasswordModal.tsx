import React from 'react';
import Modal from 'react-native-modal';
import {StyleSheet, View, Text} from 'react-native';

import {observer} from 'mobx-react';
import MainStore from 'src/store/MainStore';

import Utils from 'src/utils/Utils';

import useHNavigation from 'src/hooks/useHNavigation';

const ForgotPasswordModal = () => {
  const {forgotPasswordModalVisible} = MainStore;
  const navigation = useHNavigation();

  const hideModal = () => {
    MainStore.hideForgotPasswordModal();
  };

  const handleForgotPasssword = async () => {
    await Utils.clearAll();
    MainStore.hideForgotPasswordModal();
    navigation.navigate('Dashboard');
  };

  return (
    <Modal
      isVisible={forgotPasswordModalVisible}
      backdropOpacity={1.0}
      style={styles.modal}
      animationIn="zoomInUp"
      animationOut="zoomOut">
      <View style={styles.modalContainer}>
        <Text style={styles.forgotPassHeadline}>Forgot password</Text>
        <Text style={styles.forgotPassDescription}>
          Account stored encrypted on your device. we unable to reset your
          password. Delete account and start new?{'\n\n'}Note: Your contacts,
          messages and feeds will be permanently lost and deleted.
        </Text>
        <View style={styles.forgotPassActionWrapper}>
          <Text style={styles.cancelButton} onPress={hideModal}>
            Cancel
          </Text>
          <Text
            style={styles.deleteButton}
            onPress={() => handleForgotPasssword()}>
            Delete
          </Text>
        </View>
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
  forgotPassHeadline: {
    fontFamily: 'PublicSans-Bold',
    fontSize: 19,
    color: '#FFF',
    marginBottom: 30,
  },
  forgotPassDescription: {
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
    color: '#FFF',
    lineHeight: 21,
  },
  forgotPassActionWrapper: {
    flexDirection: 'row',
    gap: 10,
    paddingHorizontal: 12,
    marginTop: 30,
  },
  cancelButton: {
    flex: 3,
    textAlign: 'center',
    color: '#FFF',
    padding: 10,
    backgroundColor: '#414141',
    borderRadius: 8,
  },
  deleteButton: {
    flex: 3,
    textAlign: 'center',
    color: '#FF3B30',
    padding: 10,
    backgroundColor: '#131212',
    borderColor: '#94A3B8',
    borderWidth: 1,
    borderRadius: 8,
  },
});

export default observer(ForgotPasswordModal);
