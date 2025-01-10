import React from 'react';
import {Modal, Text, TouchableOpacity, View, StyleSheet} from 'react-native';
import MainStore from 'src/store/MainStore';
import {observer} from 'mobx-react';

const ActionSheetModal = () => {
  const {visible, buttons, onCancel} = MainStore.actionsheet;

  const hideModal = (cb?: () => void) => {
    MainStore.hideActionSheet();
    setTimeout(() => cb?.(), 300);
  };

  return (
    <Modal visible={visible} transparent animationType="fade">
      <View style={styles.container}>
        <View style={styles.buttonList}>
          {buttons?.map((button, index) => (
            <TouchableOpacity
              key={index}
              activeOpacity={0.9}
              style={[
                styles.actionButton,
                index !== 0 && styles.actionButtonBorder,
              ]}
              onPress={() => hideModal(button.onPress)}>
              <Text
                style={[styles.buttonText, {color: button.color || '#1967d2'}]}>
                {button.text}
              </Text>
            </TouchableOpacity>
          ))}
        </View>
        <TouchableOpacity
          activeOpacity={0.9}
          style={styles.cancelButton}
          onPress={() => hideModal(onCancel)}>
          <Text style={styles.buttonText}>Cancel</Text>
        </TouchableOpacity>
      </View>
    </Modal>
  );
};

export default observer(ActionSheetModal);

const styles = StyleSheet.create({
  container: {
    flex: 1,
    justifyContent: 'flex-end',
    padding: 10,
    backgroundColor: 'rgba(0, 0, 0, 0.3)',
  },
  buttonList: {
    borderRadius: 10,
    backgroundColor: 'rgba(255, 255, 255, 0.95)',
  },
  actionButton: {
    alignItems: 'center',
    justifyContent: 'center',
    height: 48,
  },
  actionButtonBorder: {
    borderTopWidth: 1,
    borderTopColor: 'rgb(220, 220, 220)',
  },
  buttonText: {
    fontSize: 16,
    fontWeight: '600',
    fontFamily: 'futura',
  },
  cancelButton: {
    borderRadius: 10,
    backgroundColor: 'white',
    height: 48,
    alignItems: 'center',
    justifyContent: 'center',
    marginVertical: 10,
  },
});
