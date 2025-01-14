import React from 'react';
import {
  View,
  Text,
  FlatList,
  Image,
  StyleSheet,
  ListRenderItem,
} from 'react-native';
import GroupIcon from 'react-native-vector-icons/MaterialIcons';
import CommonHeader from 'src/components/header';
import Layout from '../Layout';

type Member = {
  id: string;
  name: string;
  message: string | null;
  profilePic: string;
};

const GroupProfile = () => {
  const members: Member[] = [
    {
      id: '1',
      name: 'Maciej Kowalski',
      message: 'maciej.kowalski@email.com',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '2',
      name: 'Odeusz Piotrowski',
      message: 'Will do, super, thank you 😊❤️',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '3',
      name: 'Maciej Kowalski',
      message: 'maciej.kowalski@email.com',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '4',
      name: 'Maciej Kowalski',
      message: 'maciej.kowalski@email.com',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '5',
      name: 'Odeusz Piotrowski',
      message: 'Will do, super, thank you 😊❤️',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '6',
      name: 'Maciej Kowalski',
      message: 'maciej.kowalski@email.com',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '7',
      name: 'Maciej Kowalski',
      message: 'maciej.kowalski@email.com',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '8',
      name: 'Odeusz Piotrowski',
      message: 'Will do, super, thank you 😊❤️',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '9',
      name: 'Maciej Kowalski',
      message: 'maciej.kowalski@email.com',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '10',
      name: 'Maciej Kowalski',
      message: 'maciej.kowalski@email.com',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '11',
      name: 'Odeusz Piotrowski',
      message: 'Will do, super, thank you 😊❤️',
      profilePic: 'https://via.placeholder.com/50',
    },
    {
      id: '12',
      name: 'Maciej Kowalski',
      message: 'maciej.kowalski@email.com',
      profilePic: 'https://via.placeholder.com/50',
    },
  ];

  const renderMember: ListRenderItem<Member> = ({item}) => (
    <View style={styles.memberRow}>
      <Image source={{uri: item.profilePic}} style={styles.profilePic} />
      <View style={styles.memberInfo}>
        <Text style={styles.memberName}>{item.name}</Text>
        {item.message && (
          <Text style={styles.memberMessage} numberOfLines={2}>
            {item.message}
          </Text>
        )}
      </View>
    </View>
  );

  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader headerName="" iconExist={true} />

        <View style={styles.groupInfo}>
          <View style={styles.groupIconWrapper}>
            <GroupIcon name={'people-outline'} size={72} color="#000" />
          </View>
          <Text style={styles.groupName}>Member group</Text>
          <Text style={styles.groupDetails}>
            Created by Tanvirul on 20.09.2024
          </Text>
          <Text style={styles.memberCount}>13 Members</Text>
        </View>

        {/* Member List */}
        <FlatList
          data={members}
          keyExtractor={item => item.id}
          renderItem={renderMember}
        />
      </View>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  groupInfo: {
    alignItems: 'center',
    marginVertical: 30,
  },
  groupIconWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 148,
    height: 148,
    borderRadius: 32,
    backgroundColor: '#05FCFC',
    marginBottom: 30,
  },
  groupName: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 18,
    textAlign: 'center',
  },
  groupDetails: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 12,
    marginBottom: 30,
  },
  memberCount: {
    color: '#00eaff',
    fontSize: 14,
  },
  memberRow: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingBottom: 16,
    gap: 20,
  },
  profilePic: {
    width: 44,
    height: 44,
    borderRadius: 22,
  },
  memberInfo: {
    flex: 1,
  },
  memberName: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 15,
  },
  memberMessage: {
    color: '#FFF',
    fontFamily: 'Poppins-light',
    fontSize: 13,
  },
});

export default GroupProfile;
