import React, {useCallback, useState} from 'react';
import {
  View,
  Text,
  TextInput,
  TouchableOpacity,
  StyleSheet,
  Alert,
} from 'react-native';

import Clipboard from '@react-native-clipboard/clipboard';
import ClipBoardIcon from 'react-native-vector-icons/Feather';
import WaringIcon from 'react-native-vector-icons/Ionicons';

import {useFocusEffect} from '@react-navigation/native';
import NetInfo from '@react-native-community/netinfo';

import {observer} from 'mobx-react';
import ProfileStore from 'src/store/ProfileStore';
import ContactsStore from 'src/store/ContactsStore';
import MainStore from 'src/store/MainStore';

import {Button} from 'src/components/Button/Button';
import ConnectSuccessModal from './ConnectSuccessModal';

const CelluarWifiView = () => {
  const [loading, setLoading] = useState(false);

  const [link, setLink] = useState(ProfileStore.externalLink);

  const [contactLink, setContactLink] = useState('');
  const [error, setError] = useState(false);
  const [errMsg, setErrMsg] = useState('');

  const [hasInternet, setHasInternet] = useState<boolean>(true);

  useFocusEffect(
    useCallback(() => {
      const netInfoSubscription = NetInfo.addEventListener(state => {
        setHasInternet(state.isConnected ?? false);
      });
      return () => {
        netInfoSubscription();
      };
    }, []),
  );

  const handleContinue = async () => {
    setLoading(true);
    if (hasInternet) {
      if (!contactLink) {
        setError(true);
        setErrMsg('You must both add each other’s links');
        setLoading(false);
      } else {
        const result = await ContactsStore.checkLink(contactLink);
        if (result.success) {
          setError(false);
          MainStore.showConnectionSuccessModal();
        } else {
          setError(true);
          setErrMsg(result.error || '');
        }
        setLoading(false);
      }
    } else {
      setError(true);
      setErrMsg('You are not connected to the Internet.');
      setLoading(false);
    }
  };

  const copyToClipboard = () => {
    Clipboard.setString(link);
    Alert.alert('Copied!', 'Your link has been copied to the clipboard.');
  };

  return (
    <>
      <Text style={styles.step}>1. Exchange links with your contact</Text>
      <Text style={styles.instruction}>Give below link to your contact</Text>
      <View style={styles.linkContainer}>
        <Text style={styles.link}>{link}</Text>
        <TouchableOpacity onPress={() => copyToClipboard()}>
          <ClipBoardIcon name="copy" size={20} color="#000" />
        </TouchableOpacity>
      </View>

      <Text style={styles.step}>2. Enter links from your contact</Text>
      <TextInput
        style={[styles.input, error && styles.inputError]}
        placeholder="Enter link from your contact here"
        placeholderTextColor="#94A3B8"
        value={contactLink}
        onChangeText={text => setContactLink(text)}
      />

      {error && (
        <View style={styles.errorContainer}>
          <WaringIcon name="warning-outline" size={24} color="#FF0404" />
          <Text style={styles.errorText}>{errMsg}</Text>
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

      <ConnectSuccessModal />
    </>
  );
};

export default observer(CelluarWifiView);

const styles = StyleSheet.create({
  step: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    paddingBottom: 20,
  },
  instruction: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    lineHeight: 19,
    paddingLeft: 10,
    paddingBottom: 30,
  },
  linkContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#FFF',
    borderRadius: 10,
    padding: 15,
    marginBottom: 50,
  },
  link: {
    flex: 1,
    color: '#007AFF',
    fontFamily: 'Poppins-SemiBold',
    fontSize: 13,
    lineHeight: 19,
  },
  input: {
    backgroundColor: '#FFF',
    borderRadius: 10,
    padding: 15,
    color: '#000',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    lineHeight: 19,
    marginBottom: 10,
  },
  inputError: {
    borderColor: 'red',
    borderWidth: 1,
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
    marginBottom: -118,
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
    marginTop: 180,
    marginBottom: 20,
    backgroundColor: '#05FCFC',
  },
});
