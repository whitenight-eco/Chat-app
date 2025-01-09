import React, {useState} from 'react';
import {
  View,
  Text,
  TouchableOpacity,
  StyleSheet,
  ScrollView,
} from 'react-native';

import Layout from 'src/screens/Layout';
import CommonHeader from 'src/components/header';
import BluetoothView from './BluetoothView';
import CelluarWifiView from './CelluarWifiView';

const AddContactsScreen = () => {
  const [connectionType, setConnectionType] = useState<
    'bluetooth' | 'celluar_wifi'
  >('celluar_wifi');

  return (
    <Layout>
      <ScrollView contentContainerStyle={styles.scrollview}>
        <View style={styles.container}>
          {connectionType === 'celluar_wifi' ? (
            <CommonHeader headerName="Add Contacts" iconExist={false} />
          ) : (
            <CommonHeader headerName="Add Contacts" iconExist={true} />
          )}

          <Text style={styles.subtitle}>Choose connection type</Text>
          <View style={styles.radioContainer}>
            <TouchableOpacity
              style={styles.radioOption}
              onPress={() => setConnectionType('bluetooth')}>
              <View style={styles.radioCircle}>
                {connectionType === 'bluetooth' && (
                  <View style={styles.radioSelected} />
                )}
              </View>
              <Text style={styles.radioLabel}>Bluetooth</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={styles.radioOption}
              onPress={() => setConnectionType('celluar_wifi')}>
              <View style={styles.radioCircle}>
                {connectionType === 'celluar_wifi' && (
                  <View style={styles.radioSelected} />
                )}
              </View>
              <Text style={styles.radioLabel}>Cellular / Wifi</Text>
            </TouchableOpacity>
          </View>

          {connectionType === 'celluar_wifi' ? (
            <CelluarWifiView />
          ) : (
            <BluetoothView />
          )}
        </View>
      </ScrollView>
    </Layout>
  );
};

const styles = StyleSheet.create({
  scrollview: {
    flexGrow: 1,
  },
  container: {
    flex: 1,
    height: '100%',
  },
  subtitle: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    lineHeight: 19,
    paddingVertical: 30,
  },
  radioContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 50,
  },
  radioOption: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
  },
  radioCircle: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: '#FFF',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'transparent',
  },
  radioSelected: {
    width: 12, // Inner circle size
    height: 12,
    borderRadius: 6,
    backgroundColor: '#05FCFC',
  },
  radioLabel: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    textAlign: 'center',
  },
});

export default AddContactsScreen;
