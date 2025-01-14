import React from 'react';
import {View, Text, Image, StyleSheet, TouchableOpacity} from 'react-native';

import AddIcon from 'react-native-vector-icons/Ionicons';
import SearchIcon from 'react-native-vector-icons/Ionicons';
import ThreeDotIcon from 'react-native-vector-icons/Entypo';
import QrCodeScanIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import CloseIcon from 'react-native-vector-icons/Ionicons';
import GroupIcon from 'react-native-vector-icons/MaterialIcons';
import GoBackIcon from 'react-native-vector-icons/MaterialIcons';

import useHNavigation from 'src/hooks/useHNavigation';

const CommonHeader = ({
  avatar,
  isGroup,
  netstats,
  headerName,
  iconExist,
}: {
  avatar?: string;
  isGroup?: boolean;
  netstats?: string;
  headerName: string;
  iconExist: boolean;
}) => {
  const navigation = useHNavigation();

  const renderIcons = () => {
    switch (headerName) {
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
      case 'Chat':
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
      case '':
        return iconExist ? (
          <TouchableOpacity>
            <ThreeDotIcon name="dots-three-vertical" size={20} color="#FFF" />
          </TouchableOpacity>
        ) : null;
      case 'Rename group':
        return null;
      case 'Poll':
        return null;
      default:
        return (
          <View style={styles.iconWrapper}>
            <TouchableOpacity>
              <SearchIcon name="search" size={20} color="#FFF" />
            </TouchableOpacity>
            <TouchableOpacity>
              <ThreeDotIcon name="dots-three-vertical" size={20} color="#FFF" />
            </TouchableOpacity>
          </View>
        );
    }
  };

  return (
    <View style={styles.header}>
      <View style={styles.userContainer}>
        <View style={styles.imageContainer}>
          {avatar && (
            <>
              {!isGroup ? (
                <>
                  <Image source={{uri: avatar}} style={styles.avatar} />
                  <View
                    style={[
                      styles.statusDot,
                      netstats === 'online_internet'
                        ? styles.online_internet
                        : netstats === 'online_bluetooth'
                        ? styles.online_bluetooh
                        : styles.offline,
                    ]}
                  />
                </>
              ) : (
                <>
                  <TouchableOpacity
                    style={styles.groupDefaultIcon}
                    onPress={() => navigation.navigate('GroupProfile')}>
                    <GroupIcon name={'people-outline'} size={24} color="#000" />
                  </TouchableOpacity>
                </>
              )}
            </>
          )}
        </View>
        {headerName ? (
          <>
            {(headerName === 'Rename group' ||
              headerName === 'Poll' ||
              headerName === 'Event') && (
              <TouchableOpacity onPress={() => navigation.goBack()}>
                <GoBackIcon name="arrow-back" size={30} color="#FFF" />
              </TouchableOpacity>
            )}
            <Text style={styles.headerTitle}>{headerName}</Text>
          </>
        ) : (
          <TouchableOpacity onPress={() => navigation.goBack()}>
            <GoBackIcon name="arrow-back" size={30} color="#FFF" />
          </TouchableOpacity>
        )}
      </View>
      {renderIcons()}
    </View>
  );
};

const styles = StyleSheet.create({
  header: {
    width: '100%',
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 16,
    paddingBottom: 5,
  },
  userContainer: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  imageContainer: {
    position: 'relative',
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
  },
  groupDefaultIcon: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 40,
    height: 40,
    borderRadius: 20,
    backgroundColor: '#05FCFC',
  },
  statusDot: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    width: 12,
    height: 12,
    borderRadius: 6,
  },
  online_internet: {
    backgroundColor: '#4CE417',
  },
  online_bluetooh: {
    backgroundColor: '#0B23F8',
  },
  offline: {
    backgroundColor: '#808080',
  },
  name: {
    flex: 1,
    overflow: 'hidden',
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 18,
    textAlign: 'left',
  },
  headerTitle: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 18,
    textAlign: 'left',
  },
  iconWrapper: {
    flexDirection: 'row',
    gap: 20,
  },
});

export default CommonHeader;
