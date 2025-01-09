import React from 'react';
import {View, Text, StyleSheet, FlatList, Image} from 'react-native';

import Layout from 'src/screens/Layout';
import CommonHeader from 'src/components/header';

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

interface ChatsProps {
  setUnreadCount: React.Dispatch<React.SetStateAction<number>>;
}

const ChatsScreen: React.FC<ChatsProps> = ({setUnreadCount}) => {
  const renderItem = ({item}: {item: (typeof chats)[0]}) => (
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
