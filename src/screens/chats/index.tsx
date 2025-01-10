import React, {useState, useCallback} from 'react';
import {useFocusEffect} from '@react-navigation/native';
import {View, Text, StyleSheet, FlatList, Image} from 'react-native';
import firestore from '@react-native-firebase/firestore';

import Layout from 'src/screens/Layout';
import CommonHeader from 'src/components/header';
import Utils from 'src/utils/Utils';
import ProfileStore from 'src/store/ProfileStore';
import {IChatDoc, IMessage} from 'src/types';

const chats = [
  {
    id: '1',
    name: 'Maciej Kowalski',
    time: '08:43',
    image: 'https://via.placeholder.com/50',
    netstats: 'online_internet',
    message: 'maciej.kowalski@email.com',
  },
  {
    id: '2',
    name: 'Odeusz Piotrowski',
    time: 'Tue',
    image: 'https://via.placeholder.com/50',
    netstats: 'online_bluetooth',
    message: 'Will do, super, thank you 😊❤️',
  },
  {
    id: '3',
    name: 'Bożenka Malina',
    time: 'Sun',
    image: 'https://via.placeholder.com/50',
    netstats: 'online_internet',
    message: 'Uploaded file.',
  },
  {
    id: '4',
    name: 'Maciej Orlowski',
    time: '23 Mar',
    image: 'https://via.placeholder.com/50',
    netstats: 'offline',
    message:
      'Here is another tutorial, if you Here is another tutorial Here is another tutorial, if you Here is another tutorial',
  },
];

interface StoredMessages {
  [key: string]: number;
}
interface ChatsProps {
  setUnreadCount: React.Dispatch<React.SetStateAction<number>>;
}

const ChatsScreen: React.FC<ChatsProps> = ({setUnreadCount}) => {
  // const [chats, setChats] = useState<IChatDoc[]>([]);
  const [loading, setLoading] = useState(true);
  const [newMessages, setNewMessages] = useState({});
  const currentUser = ProfileStore.user;

  // useFocusEffect(
  //   useCallback(() => {
  //     // Load unread messages from AsyncStorage when screen is focused
  //     const loadNewMessages = async () => {
  //       try {
  //         const storedMessages = await Utils.getObject('newMessages');

  //         setNewMessages(storedMessages);
  //         setUnreadCount(
  //           Object.values(storedMessages).reduce(
  //             (total, num) => total + num,
  //             0,
  //           ),
  //         );
  //       } catch (error) {
  //         console.log('Error loading new messages from storage', error);
  //       }
  //     };

  //     // Set up Firestore listener for chat updates
  //     if (!currentUser) return;

  //     const unsubscribe = firestore()
  //       .collection('chats')
  //       .where('users', 'array-contains', {
  //         publicKey: currentUser?.publicKey,
  //         externalLink: currentUser?.externalLink,
  //         username: currentUser?.username,
  //         deletedFromChat: false,
  //       })
  //       .orderBy('lastUpdated', 'desc')
  //       .onSnapshot(snapshot => {
  //         setChats(snapshot.docs);
  //         setLoading(false);

  //         snapshot.docChanges().forEach(change => {
  //           if (change.type === 'modified') {
  //             const chatId = change.doc.id;
  //             const data = change.doc.data() as IChat;
  //             const messages = data.messages;
  //             const firstMessage = messages[0];

  //             if (firstMessage?.user._id !== currentUser.email) {
  //               setNewMessages(prev => {
  //                 const updatedMessages = {
  //                   ...prev,
  //                   [chatId]: (prev[chatId] || 0) + 1,
  //                 };
  //                 Utils.storeObject('newMessages', updatedMessages);
  //                 setUnreadCount(
  //                   Object.values(updatedMessages).reduce(
  //                     (total, num) => total + num,
  //                     0,
  //                   ),
  //                 );
  //                 return updatedMessages;
  //               });
  //             }
  //           }
  //         });
  //       });
  //     // Load unread messages and start listener when screen is focused
  //     loadNewMessages();

  //     // Clean up listener on focus change
  //     return () => unsubscribe();
  //   }, []),
  // );

  const renderItem = ({item}: {item: any}) => (
    <View style={styles.chatItem}>
      <View style={styles.imageContainer}>
        <Image source={{uri: item.image}} style={styles.contactImage} />
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
      <View style={styles.chatInfo}>
        <Text style={styles.contactName}>{item.name}</Text>
        <Text style={styles.chatMessage} numberOfLines={2}>
          {item.message}
        </Text>
      </View>
      <Text style={styles.chatTime} numberOfLines={1}>
        {item.time}
      </Text>
    </View>
  );

  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader headerName="Chats" iconExist={true} />

        {/* chat List */}
        <FlatList
          data={chats}
          keyExtractor={item => item.id}
          renderItem={renderItem}
          contentContainerStyle={styles.chatList}
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
});

export default ChatsScreen;
