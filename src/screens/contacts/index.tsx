import React, {useState, useEffect} from 'react';
import {useFocusEffect} from '@react-navigation/native';
import {View, Text, StyleSheet, TextInput, FlatList, Image} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';

import {useDebounce} from 'use-debounce';

import Layout from 'src/screens/Layout';
import {IContactsType} from 'src/store/types';
import CommonHeader from 'src/components/CommonHeader';

import ContactsStore from 'src/store/ContactsStore';

const ContactsScreen = () => {
  const [searchItem, setSearchItem] = useState('');
  const [debouncedSearchTerm] = useDebounce(searchItem, 300);
  const [contacts, setContacts] = useState<IContactsType[]>([]);
  const [filteredUsers, setFilteredUsers] = useState<IContactsType[]>([]);

  useFocusEffect(() => {
    setContacts(ContactsStore.contacts);
  });

  useEffect(() => {
    const filteredItems = contacts.filter(user =>
      user.name.toLowerCase().includes(debouncedSearchTerm.toLowerCase()),
    );
    setFilteredUsers(filteredItems);
  }, [debouncedSearchTerm, contacts]);

  const renderItem = ({item}: {item: IContactsType}) => (
    <View style={styles.contactItem}>
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
      <Text style={styles.contactName}>{item.name}</Text>
    </View>
  );

  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader name="Contacts" iconExist={true} />

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
          data={filteredUsers}
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

export default ContactsScreen;
