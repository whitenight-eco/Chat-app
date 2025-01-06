import React from 'react';
import {createStackNavigator} from '@react-navigation/stack';
import ContactsScreen from 'src/screens/contacts';
import AddContactsScreen from 'src/screens/contacts/AddContacts';
import QrScanScreen from '../contacts/QrScan';

const Stack = createStackNavigator();

const ContactsStack = () => {
  return (
    <Stack.Navigator screenOptions={{headerShown: false}}>
      <Stack.Screen name="ContactsMain" component={ContactsScreen} />
      <Stack.Screen name="AddContacts" component={AddContactsScreen} />
      <Stack.Screen name="QrScan" component={QrScanScreen} />
    </Stack.Navigator>
  );
};

export default ContactsStack;
