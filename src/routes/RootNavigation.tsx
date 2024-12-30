import React, {useState, useEffect} from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createDrawerNavigator} from '@react-navigation/drawer';
import {createStackNavigator} from '@react-navigation/stack';
import {observer} from 'mobx-react';

import ProfileStore from 'src/store/ProfileStore';

import SplashScreen from 'react-native-splash-screen';
import Splash from 'src/screens/onboarding/SplashScreen';

import Dashboard from 'src/screens/Dashboard';
import Login from 'src/screens/auth/Login';
import Signup from 'src/screens/auth/Signup';

import ContactsScreen from 'src/screens/contacts/ContactsScreen';
import ChatsScreen from 'src/screens/chats/ChatsScreen';
import FeedsScreen from 'src/screens/feeds/FeeedsScreen';

const DrawerNavigator = () => {
  const Drawer = createDrawerNavigator();

  const CustomDrawerScreen = (name: string, component: any) => {
    return <Drawer.Screen name={name} component={component} />;
  };

  return (
    <Drawer.Navigator
      screenOptions={{headerShown: false, swipeEnabled: false}}
      initialRouteName="Chats">
      {CustomDrawerScreen('Contacts', ContactsScreen)}
      {CustomDrawerScreen('Chats', ChatsScreen)}
      {CustomDrawerScreen('Feeds', FeedsScreen)}
    </Drawer.Navigator>
  );
};

const RootNavigation = () => {
  const [doneSplash, setDoneSplash] = useState(false);
  const {isLoggedIn} = ProfileStore;

  useEffect(() => {
    SplashScreen.hide();
  }, []);

  const Stack = createStackNavigator();

  return doneSplash ? (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName={isLoggedIn ? 'Home' : 'Dashboard'}
        screenOptions={{
          headerShown: false,
        }}>
        <Stack.Screen
          name="Home"
          component={DrawerNavigator}
          options={{headerShown: false, title: 'Home'}}
        />
        <Stack.Screen name="Dashboard" component={Dashboard} />
        <Stack.Screen name="Login" component={Login} />
        <Stack.Screen name="Signup" component={Signup} />
      </Stack.Navigator>
    </NavigationContainer>
  ) : (
    <Splash onFinish={() => setDoneSplash(true)} />
  );
};

export default observer(RootNavigation);
