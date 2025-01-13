import React, {useState, useEffect, useCallback} from 'react';
import {useFocusEffect} from '@react-navigation/native';
import {
  View,
  Text,
  StyleSheet,
  TextInput,
  FlatList,
  Image,
  TouchableOpacity,
  BackHandler,
  Alert,
} from 'react-native';
import SearchIcon from 'react-native-vector-icons/Ionicons';
import ArrowRightIcon from 'react-native-vector-icons/Feather';
import CameraIcon from 'react-native-vector-icons/FontAwesome5';

import {observer} from 'mobx-react';

import firestore from '@react-native-firebase/firestore';
import {useDebounce} from 'use-debounce';

import Layout from 'src/screens/Layout';
import {IExistingChat, IUser} from 'src/types';
import CommonHeader from 'src/components/header';
import ContactsStore from 'src/store/ContactsStore';
import useHNavigation from 'src/hooks/useHNavigation';
import ProfileStore from 'src/store/ProfileStore';

const ContactsScreen = () => {
  const [searchItem, setSearchItem] = useState('');
  const [debouncedSearchTerm] = useDebounce(searchItem, 300);
  const [contacts, setContacts] = useState<IUser[]>([]);
  const [filteredContacts, setFilteredContacts] = useState<IUser[]>([]);

  const [existingChats, setExistingChats] = useState<IExistingChat[]>([]);
  const [users, setUsers] = useState<IUser[]>([]);
  const [selectedItems, setSelectedItems] = useState<IUser[]>([]);
  const [isSelecting, setIsSelecting] = useState<boolean>(false);
  const [isgroupNameStep, setIsGroupNameStep] = useState<boolean>(false);
  const [groupName, setGroupName] = useState<string>('');

  const currentUser = ProfileStore.user;

  const navigation = useHNavigation();

  useFocusEffect(() => {
    setContacts(ContactsStore.contacts);
  });

  useEffect(() => {
    const filteredItems = contacts.filter(user =>
      user?.username.toLowerCase().includes(debouncedSearchTerm.toLowerCase()),
    );
    setFilteredContacts(filteredItems);
  }, [debouncedSearchTerm, contacts]);

  useEffect(() => {
    // Fetch users
    const unsubscribeUsers = firestore()
      .collection('users')
      .orderBy('username', 'asc')
      .onSnapshot(snapshot => {
        if (snapshot.empty) {
          console.log('There are no users registered.');
          setUsers([]);
        } else {
          const usersList: IUser[] = snapshot.docs.map(doc => {
            const docData = doc.data();
            return {
              id: docData.id,
              username: docData.username,
              avatar: docData.avatar,
              netstats: docData.netstats,
              publicKey: docData.publicKey,
              externalLink: docData.externalLink,
            };
          });
          setUsers(usersList);
        }
      });

    // Fetch existing chats
    const unsubscribeChats = firestore()
      .collection('chats')
      .where('users', 'array-contains', {
        publicKey: currentUser?.publicKey,
        externalLink: currentUser?.externalLink,
        username: currentUser?.username,
        deletedFromChat: false,
      })
      .where('groupName', '==', '')
      .onSnapshot(snapshot => {
        if (snapshot.empty) {
          console.log('There are no chats registered.');
          setExistingChats([]);
        } else {
          const existingChats = snapshot.docs.map(doc => ({
            chatId: doc.id,
            users: doc.data().users as IUser[],
          }));
          setExistingChats(existingChats);
        }
      });

    // Return cleanup function
    return () => {
      unsubscribeUsers();
      unsubscribeChats();
    };
  }, [setUsers, setExistingChats]);

  useEffect(() => {
    const backHandler = BackHandler.addEventListener(
      'hardwareBackPress',
      handleBackPress,
    );

    return () => backHandler.remove();
  }, [isSelecting]);

  const handleName = useCallback((user: IUser) => {
    const name = user.username;
    const publicKey = user.publicKey;
    if (name) {
      return publicKey === currentUser?.publicKey ? `${name}*(You)` : name;
    }
    return '';
  }, []);

  const handlePress = useCallback(
    (contact: IUser) => {
      if (!isSelecting) {
        let navigationChatID = '';
        let messageYourselfChatID = '';
        existingChats.forEach(existingChat => {
          const isCurrentUserInTheChat = existingChat.users.some(
            item => item.publicKey === currentUser?.publicKey,
          );
          const isMessageYourselfExists = existingChat.users.filter(
            item => item.publicKey === contact.publicKey,
          ).length;

          if (
            isCurrentUserInTheChat &&
            existingChat.users.some(
              item => item.publicKey === contact.publicKey,
            )
          ) {
            navigationChatID = existingChat.chatId;
          }

          if (isMessageYourselfExists === 2) {
            messageYourselfChatID = existingChat.chatId;
          }

          if (currentUser?.publicKey === contact.publicKey) {
            navigationChatID = '';
          }
        });

        if (messageYourselfChatID) {
          navigation.navigate('Chat', {
            channel: messageYourselfChatID,
            chatName: handleName(contact),
          });
        } else if (navigationChatID) {
          navigation.navigate('Chat', {
            channel: navigationChatID,
            chatName: handleName(contact),
          });
        } else {
          // Creates new chat
          const newRef = firestore().collection('chats').doc();
          newRef
            .set({
              chatId: newRef.id,
              lastUpdated: Date.now(),
              groupName: '', // It is not a group chat
              groupAvatar: '',
              users: [
                {
                  publicKey: currentUser?.publicKey,
                  externalLink: currentUser?.externalLink,
                  username: currentUser?.username,
                  deletedFromChat: false,
                },
                {
                  publicKey: contact.publicKey,
                  username: contact.username,
                  externalLink: contact.externalLink,
                  deletedFromChat: false,
                },
              ],
              lastAccess: [
                {
                  username: currentUser?.username,
                  externalLink: currentUser?.externalLink,
                  date: Date.now(),
                },
                {
                  username: contact.username,
                  externalLink: contact.externalLink,
                  date: null,
                },
              ],
              messages: [],
            })
            .then(() => {
              navigation.navigate('Chat', {
                channel: newRef.id,
                chatName: handleName(contact),
              });
            });
        }
      }
      selectItems(contact);
    },
    [existingChats, navigation],
  );

  const handleBackPress = () => {
    if (isSelecting) {
      setIsSelecting(false);
      setIsGroupNameStep(false);
      return true;
    }
    return false;
  };

  const getSelected = (user: IUser) => {
    return selectedItems.some(selected => selected.id === user.id);
  };

  const selectItems = (user: IUser) => {
    if (selectedItems.some(selected => selected.id === user.id)) {
      setSelectedItems(
        selectedItems.filter(selected => selected.id !== user.id),
      );
    } else {
      setSelectedItems([...selectedItems, user]);
    }
  };

  const handlegoNext = (users: IUser[]) => {
    if (users.length > 0) setIsGroupNameStep(true);

    if (isgroupNameStep) {
      if (!groupName.trim()) {
        Alert.alert('Error', 'Group name cannot be empty');
        return;
      }

      const usersToAdd = users
        .filter(user => selectedItems.some(selected => selected.id === user.id))
        .map(user => ({
          publicKey: user.publicKey,
          externalLink: user.externalLink,
          username: user.username,
          deletedFromChat: false,
        }));

      usersToAdd.unshift({
        publicKey: currentUser?.publicKey || '',
        externalLink: currentUser?.externalLink || '',
        username: currentUser?.username || '',
        deletedFromChat: false,
      });

      // Creates new chat
      const newRef = firestore().collection('chats').doc();
      newRef
        .set({
          chatId: newRef.id,
          lastUpdated: Date.now(),
          users: usersToAdd,
          groupName: groupName,
          groupAdmins: {
            publicKey: currentUser?.publicKey,
            externalLink: currentUser?.externalLink,
            username: currentUser?.username,
          },
          groupAvatar: 'default',
          messages: [],
        })
        .then(() => {
          navigation.navigate('Chat', {
            channel: newRef.id,
            chatName: groupName,
          });
          setGroupName('');
          setIsGroupNameStep(false);
          setIsSelecting(false);
        });
    }
  };

  const renderItem = ({item}: {item: IUser}) => (
    <TouchableOpacity
      onPress={() => handlePress(item)}
      onLongPress={() => setIsSelecting(true)}
      style={styles.contactItem}>
      <View style={styles.imageContainer}>
        <Image source={{uri: item.avatar}} style={styles.contactImage} />
        <View
          style={[
            styles.statusDot,
            item.netstats === 'online_internet'
              ? styles.online_internet
              : item.netstats === 'online_bluetooth'
              ? styles.online_bluetooh
              : styles.offline,
          ]}
        />
      </View>
      <Text style={styles.contactName} numberOfLines={1}>
        {item.username}
      </Text>
      {isSelecting && (
        <TouchableOpacity
          style={styles.radioOption}
          onPress={() => selectItems(item)}>
          <View style={styles.radioCircle}>
            {getSelected(item) && <View style={styles.radioSelected} />}
          </View>
        </TouchableOpacity>
      )}
    </TouchableOpacity>
  );

  return (
    <Layout>
      <View style={styles.container}>
        {isSelecting ? (
          <CommonHeader
            headerName={
              isgroupNameStep ? 'Group name' : 'New group - Add contacts'
            }
            iconExist={false}
          />
        ) : (
          <CommonHeader headerName="Contacts" iconExist={true} />
        )}

        {isgroupNameStep ? (
          // Input Group name
          <View style={styles.groupNameWrapper}>
            <View style={styles.cameraIcon}>
              <CameraIcon name="camera" size={20} color="#000" />
            </View>

            <TextInput
              placeholder="What will be your group name"
              placeholderTextColor="#A9A9A9"
              style={styles.groupNameInput}
              value={groupName}
              onChangeText={setGroupName}
            />
          </View>
        ) : (
          // Search Bar
          <View style={styles.searchBar}>
            <TextInput
              placeholder=""
              placeholderTextColor="#888"
              style={styles.searchInput}
              value={searchItem}
              onChangeText={setSearchItem}
            />
            <SearchIcon
              name="search"
              size={20}
              color="#FFF"
              style={styles.searchIcon}
            />
          </View>
        )}

        {/* Contact List */}
        <FlatList
          data={filteredContacts}
          keyExtractor={item => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.contactList}
        />
        {isSelecting && (
          <TouchableOpacity
            style={styles.goNextIconWrapper}
            onPress={() => handlegoNext(selectedItems)}>
            <ArrowRightIcon name="arrow-right" size={35} color="#000" />
          </TouchableOpacity>
        )}
      </View>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    flexDirection: 'column',
  },
  groupNameWrapper: {
    flexDirection: 'row',
    gap: 12,
  },
  groupNameInput: {
    flex: 1,
    backgroundColor: '#FFF',
    borderRadius: 10,
    height: 48,
    paddingLeft: 16,
    color: '#000',
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
  },
  cameraIcon: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#05FCFC',
  },
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    borderColor: '#CECECE',
    borderWidth: 1,
    borderRadius: 10,
  },
  searchIcon: {
    marginRight: 8,
  },
  searchInput: {
    flex: 1,
    height: 40,
    paddingLeft: 16,
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 12,
  },
  selectedContactRow: {
    backgroundColor: '#E0E0E0',
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
    borderRadius: 12,
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
  contactList: {
    marginTop: 10,
  },
  contactItem: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    paddingVertical: 12,
    gap: 20,
  },
  imageContainer: {
    position: 'relative',
  },
  contactImage: {
    width: 40,
    height: 40,
    borderRadius: 20,
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
  contactName: {
    flexGrow: 1,
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 14,
    textAlign: 'left',
  },
  goNextIconWrapper: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    alignItems: 'center',
    justifyContent: 'center',
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#05FCFC',
  },
});

export default observer(ContactsScreen);
