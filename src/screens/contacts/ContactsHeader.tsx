import React from 'react';
import {View, Text, StyleSheet, TouchableOpacity} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';

const ContactsHeader = () => {
  return (
    <View style={styles.header}>
      <Text style={styles.headerTitle}>Contacts</Text>
      <TouchableOpacity>
        <Icon name="add" size={24} color="#05FCFC" />
      </TouchableOpacity>
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
    paddingHorizontal: 5,
  },
  headerTitle: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 18,
    textAlign: 'center',
  },
});

export default ContactsHeader;
