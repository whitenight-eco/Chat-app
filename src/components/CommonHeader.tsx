import React from 'react';
import {View, Text, StyleSheet, TouchableOpacity} from 'react-native';

import AddIcon from 'react-native-vector-icons/Ionicons';
import SearchIcon from 'react-native-vector-icons/Ionicons';
import ThreeDotIcon from 'react-native-vector-icons/Entypo';
import QrCodeScanIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import CloseIcon from 'react-native-vector-icons/Ionicons';

import useHNavigation from 'src/hooks/useHNavigation';

const CommonHeader = ({
  name,
  iconExist,
}: {
  name: string;
  iconExist: boolean;
}) => {
  const navigation = useHNavigation();

  const renderIcons = () => {
    switch (name) {
      case 'Contacts':
        return iconExist ? (
          <TouchableOpacity onPress={() => navigation.navigate('AddContacts')}>
            <AddIcon name="add" size={24} color="#05FCFC" />
          </TouchableOpacity>
        ) : null;
      case 'Chats':
        return iconExist ? (
          <View style={styles.iconWrapper}>
            <TouchableOpacity>
              <SearchIcon name="search" size={20} color="#FFF" />
            </TouchableOpacity>
            <TouchableOpacity>
              <ThreeDotIcon name="dots-three-vertical" size={20} color="#FFF" />
            </TouchableOpacity>
          </View>
        ) : null;
      case 'Feeds':
        return iconExist ? (
          <TouchableOpacity>
            <ThreeDotIcon name="dots-three-vertical" size={20} color="#FFF" />
          </TouchableOpacity>
        ) : null;
      case 'Add Contacts':
        return iconExist ? (
          <QrCodeScanIcon name="qrcode-scan" size={20} color="#FFF" />
        ) : null;
      case 'QR Camera':
        return iconExist ? (
          <TouchableOpacity onPress={() => navigation.navigate('AddContacts')}>
            <CloseIcon name="close-circle-outline" size={30} color="#FFF" />
          </TouchableOpacity>
        ) : null;
      default:
        return null;
    }
  };

  return (
    <View style={styles.header}>
      <Text style={styles.headerTitle}>{name}</Text>
      {renderIcons()}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    flexDirection: 'column',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 16,
    paddingBottom: 5,
  },
  headerTitle: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 18,
    textAlign: 'center',
  },
  iconWrapper: {
    flexDirection: 'row',
    gap: 20,
  },
});

export default CommonHeader;
