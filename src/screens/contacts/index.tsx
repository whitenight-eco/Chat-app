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
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';
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

  const currentUser = ProfileStore.user;

  const navigation = useHNavigation();

  useFocusEffect(() => {
    setContacts(ContactsStore.users);
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
        const existingChats = snapshot.docs.map(doc => ({
          chatId: doc.id,
          users: doc.data().users as IUser[],
        }));
        setExistingChats(existingChats);
      });

    // Return cleanup function
    return () => {
      unsubscribeUsers();
      unsubscribeChats();
    };
  }, [setUsers, setExistingChats]);

  const handlePress = useCallback(
    (contact: IUser) => {
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
          existingChat.users.some(item => item.publicKey === contact.publicKey)
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
          user: contact,
        });
      } else if (navigationChatID) {
        navigation.navigate('Chat', {channel: navigationChatID, user: contact});
      } else {
        // Creates new chat
        const newRef = firestore().collection('chats').doc();
        newRef
          .set({
            lastUpdated: new Date().getTime(),
            groupName: '', // It is not a group chat
            users: [
              {
                publicKey: currentUser?.publicKey,
                externalLink: currentUser?.externalLink,
                username: currentUser?.username,
                netstats: currentUser?.netstats,
                deletedFromChat: false,
              },
              {
                publicKey: contact.publicKey,
                username: contact.username,
                externalLink: contact.externalLink,
                netstats: contact?.netstats,
                deletedFromChat: false,
              },
            ],
            lastAccess: [
              {
                username: currentUser?.username,
                externalLink: currentUser?.externalLink,
                date: new Date().getTime(),
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
            navigation.navigate('Chat', {channel: newRef.id, user: contact});
          });
      }
    },
    [existingChats, navigation],
  );

  const renderItem = ({item}: {item: IUser}) => (
    <TouchableOpacity
      onPress={() => handlePress(item)}
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
      <Text style={styles.contactName}>{item.username}</Text>
    </TouchableOpacity>
  );

  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader headerName="Contacts" iconExist={true} />

        {/* Search Bar */}
        <View style={styles.searchBar}>
          <TextInput
            placeholder=""
            placeholderTextColor="#888"
            style={styles.searchInput}
            value={searchItem}
            onChangeText={setSearchItem}
          />
          <Icon
            name="search"
            size={20}
            color="#FFF"
            style={styles.searchIcon}
          />
        </View>

        {/* Contact List */}
        <FlatList
          data={filteredContacts}
          keyExtractor={item => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.contactList}
        />
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
  contactList: {
    marginTop: 10,
  },
  contactItem: {
    flexDirection: 'row',
    alignItems: 'center',
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
  contactInfo: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  contactName: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 14,
    textAlign: 'center',
  },
});

export default observer(ContactsScreen);
