import React, {useState, useCallback} from 'react';
import {useFocusEffect} from '@react-navigation/native';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  Image,
  Pressable,
  ActivityIndicator,
  TouchableOpacity,
} from 'react-native';
import firestore from '@react-native-firebase/firestore';

import Layout from 'src/screens/Layout';
import CommonHeader from 'src/components/header';
import Utils from 'src/utils/Utils';
import ProfileStore from 'src/store/ProfileStore';
import {IChatDoc} from 'src/types';
import ContactsStore from 'src/store/ContactsStore';
import moment from 'moment';
import useHNavigation from 'src/hooks/useHNavigation';

interface ChatsProps {
  setUnreadCount: React.Dispatch<React.SetStateAction<number>>;
}

const ChatsScreen: React.FC<ChatsProps> = ({setUnreadCount}) => {
  const [chats, setChats] = useState<IChatDoc[]>([]);
  const [loading, setLoading] = useState(true);
  const [channel, setChannel] = useState('');
  const [newMessages, setNewMessages] = useState<Record<string, number>>({});

  const navigation = useHNavigation();

  const currentUser = ProfileStore.user;

  useFocusEffect(
    useCallback(() => {
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
          } else {
            snapshot.docs.forEach(doc => setChannel(doc.id));
          }
        });

      // Load unread messages from AsyncStorage when screen is focused
      const loadNewMessages = async () => {
        try {
          const storedMessages = (await Utils.getObject(
            'newMessages',
          )) as Record<string, number>;

          setNewMessages(storedMessages);
          setUnreadCount(
            Object.values(storedMessages).reduce(
              (total, num) => total + num,
              0,
            ),
          );
        } catch (error) {
          console.log('Error loading new messages from storage', error);
        }
      };

      // Set up Firestore listener for chat updates
      if (!currentUser) return;

      const unsubscribe = firestore()
        .collection('chats')
        .where('users', 'array-contains', {
          publicKey: currentUser?.publicKey,
          externalLink: currentUser?.externalLink,
          username: currentUser?.username,
          deletedFromChat: false,
        })
        .onSnapshot(snapshot => {
          if (snapshot.empty) {
            console.log('There are no chats registered~~.');
            setChats([]);
            setLoading(false);
          } else {
            snapshot.docs.forEach(doc => setChannel(doc.id));
            setChats(snapshot.docs.map(doc => doc.data() as IChatDoc));

            setLoading(false);

            snapshot.docChanges().forEach(change => {
              if (change.type === 'modified') {
                const chatId = change.doc.id;
                setNewMessages(prev => {
                  const updatedMessages = {
                    ...prev,
                    [chatId]: (prev[chatId] || 0) + 1,
                  };
                  Utils.storeObject('newMessages', updatedMessages);
                  setUnreadCount(
                    Object.values(
                      updatedMessages as Record<string, number>,
                    ).reduce((total, num) => total + num, 0),
                  );
                  return updatedMessages;
                });
              }
            });
          }
        });
      //Load unread messages and start listener when screen is focused
      loadNewMessages();

      // Clean up listener on focus change
      return () => {
        unsubscribe();
        unsubscribeChats();
      };
    }, []),
  );

  const dynamicDisplayTime = (date: number) => {
    const messageDate = moment(new Date(date));
    const now = moment();

    if (now.diff(messageDate, 'days') < 1) {
      // Within 1 day
      return messageDate.format('H:mm');
    } else if (now.diff(messageDate, 'years') < 1) {
      // Within 1 year
      return messageDate.format('D MMM');
    } else {
      // Over 1 year
      return messageDate.format('YYYY D MMM');
    }
  };

  const RenderChatInfo = (chat: IChatDoc, itemName: string) => {
    const chattingUser = chat.users.find(
      item => item.publicKey !== currentUser?.publicKey,
    );

    const chattingUserInfo = ContactsStore.contacts.find(
      item => item.publicKey === chattingUser?.publicKey,
    );

    switch (itemName) {
      case 'username':
        return chattingUser?.username ? chattingUser?.username : '';

      case 'avatar':
        return chattingUserInfo?.avatar ? chattingUserInfo?.avatar : '';

      case 'netstats':
        return chattingUserInfo?.netstats ? chattingUserInfo?.netstats : '';

      case 'message':
        const latestMessage = chat.messages?.reduce((latest, current) =>
          current.createdAt > latest.createdAt ? current : latest,
        );
        return latestMessage.text;

      case 'lastedUpdateTime':
        return dynamicDisplayTime(chat.lastUpdated);

      default:
        return '';
    }
  };

  const handleNavigate = async (chat: IChatDoc) => {
    const chattingUser = chat.users.find(
      item => item.publicKey !== currentUser?.publicKey,
    );

    const chattingUserInfo = ContactsStore.contacts.find(
      item => item.publicKey === chattingUser?.publicKey,
    );
    // Reset unread count for the selected chat
    setNewMessages(prev => {
      const updatedMessages = {...prev, [channel]: 0};
      Utils.storeObject('newMessages', updatedMessages);
      setUnreadCount(
        Object.values(updatedMessages as Record<string, number>).reduce(
          (total, num) => total + num,
          0,
        ),
      );
      return updatedMessages;
    });
    navigation.navigate('Chat', {channel: channel, user: chattingUserInfo});
  };

  const renderItem = ({item}: {item: IChatDoc}) => (
    <TouchableOpacity
      style={styles.chatItem}
      onPress={() => handleNavigate(item)}>
      <View style={styles.imageContainer}>
        <Image
          source={{uri: RenderChatInfo(item, 'avatar')}}
          style={styles.contactImage}
        />
        <View
          style={[
            styles.statusDot,
            RenderChatInfo(item, 'netstats') === 'online_internet'
              ? styles.online_internet
              : RenderChatInfo(item, 'netstats') === 'online_bluetooth'
              ? styles.online_bluetooh
              : styles.offline,
          ]}
        />
      </View>
      <View style={styles.chatInfo}>
        <Text style={styles.contactName}>
          {RenderChatInfo(item, 'username')}
        </Text>
        <Text style={styles.chatMessage} numberOfLines={2}>
          {RenderChatInfo(item, 'message')}
        </Text>
      </View>
      <View style={{flex: 1, flexDirection: 'column'}}>
        <Text style={styles.chatTime} numberOfLines={1}>
          {RenderChatInfo(item, 'lastedUpdateTime')}
        </Text>
        {Object.values(newMessages).reduce((total, num) => total + num, 0) >
          0 && (
          <View style={styles.newMessageBadge}>
            <Text style={styles.newMessageText}>
              {Object.values(newMessages).reduce(
                (total, num) => total + num,
                0,
              )}
            </Text>
          </View>
        )}
      </View>
    </TouchableOpacity>
  );

  return (
    <Layout>
      <Pressable style={styles.container}>
        {loading ? (
          <ActivityIndicator size="large" style={styles.loadingContainer} />
        ) : (
          <>
            <CommonHeader headerName="Chats" iconExist={true} />
            {/* chat List */}
            <FlatList
              data={chats}
              keyExtractor={item => item.lastUpdated.toString()}
              renderItem={renderItem}
              contentContainerStyle={styles.chatList}
            />
          </>
        )}
      </Pressable>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    flexDirection: 'column',
  },
  loadingContainer: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    color: '#26A69A',
  },
  chatList: {
    marginTop: 10,
  },
  chatItem: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingVertical: 12,
    gap: 15,
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
  chatInfo: {
    flex: 7,
  },
  contactName: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 15,
  },
  chatMessage: {
    color: '#FFF',
    fontFamily: 'Poppins-Light',
    fontSize: 13,
    letterSpacing: 1,
  },
  chatTime: {
    flex: 2,
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 15,
    textAlign: 'center',
  },
  newMessageBadge: {
    backgroundColor: '#26A69A',
    borderRadius: 12,
    paddingHorizontal: 6,
    paddingVertical: 2,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 4,
  },
  newMessageText: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
});

export default ChatsScreen;
