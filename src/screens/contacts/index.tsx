import React from 'react';
import {View, Text, StyleSheet, TextInput, FlatList, Image} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';

import Layout from 'src/screens/Layout';
import ContactsHeader from './ContactsHeader';

const contacts = [
  {
    id: '1',
    name: 'Adina Nurrahma',
    image: 'https://via.placeholder.com/50',
    status: 'online',
  },
  {
    id: '2',
    name: 'Mike Mazowski',
    image: 'https://via.placeholder.com/50',
    status: 'offline',
  },
  {
    id: '3',
    name: 'Marvin Robertson',
    image: 'https://via.placeholder.com/50',
    status: 'online',
  },
];

const ContactsScreen = () => {
  const renderItem = ({item}: {item: (typeof contacts)[0]}) => (
    <View style={styles.contactItem}>
      <View style={styles.imageContainer}>
        <Image source={{uri: item.image}} style={styles.contactImage} />
        <View
          style={[
            styles.statusDot,
            item.status === 'online' ? styles.online : styles.offline,
          ]}
        />
      </View>
      <Text style={styles.contactName}>{item.name}</Text>
    </View>
  );

  return (
    <Layout>
      <View style={styles.container}>
        <ContactsHeader />

        {/* Search Bar */}
        <View style={styles.searchBar}>
          <TextInput
            placeholder=""
            placeholderTextColor="#888"
            style={styles.searchInput}
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
          data={contacts}
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
  online: {
    backgroundColor: '#4CE417',
  },
  offline: {
    backgroundColor: '#0B23F8',
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

export default ContactsScreen;
