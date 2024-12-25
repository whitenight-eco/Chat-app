import React, {useState, useEffect} from 'react';
import {NavigationContainer} from '@react-navigation/native';
import {createDrawerNavigator} from '@react-navigation/drawer';
import {createStackNavigator} from '@react-navigation/stack';
import Icon from 'react-native-vector-icons/Ionicons';

import {useTheme} from 'src/theme/useTheme';

import SplashScreen from 'react-native-splash-screen';
import Splash from 'src/screens/onboarding/SplashScreen';

import Dashboard from 'src/screens/Dashboard';
import Login from 'src/screens/auth/Login';
import Signup from 'src/screens/auth/Signup';

import ContactsScreen from 'src/screens/contacts/ContactsScreen';

const DrawerNavigator = () => {
  const Drawer = createDrawerNavigator();
  const {theme} = useTheme();

  const dynamicStyles = {
    header: {
      backgroundColor: theme.cardBg,
    },
    title: {
      color: theme.color,
      lineHeight: 20,
    },
    icon: {
      color: theme.color,
    },
  };

  const CustomDrawerScreen = (name: string, component: any) => {
    return (
      <Drawer.Screen
        name={name}
        component={component}
        options={({navigation}) => ({
          title: name,
          headerStyle: dynamicStyles.header,
          headerTitleStyle: dynamicStyles.title,
          headerTitleAlign: 'left',
          headerLeft: () => (
            <Icon.Button
              name="menu"
              size={18}
              color={theme.color}
              onPress={() => navigation.toggleDrawer()}
              style={{backgroundColor: 'white'}}
            />
          ),
        })}
      />
    );
  };

  return (
    <Drawer.Navigator
      screenOptions={
        {
          headerStyle: dynamicStyles.header,
          headerTitleStyle: dynamicStyles.title,
        }
      }>
      {CustomDrawerScreen('Contacts', ContactsScreen)}
    </Drawer.Navigator>
  );
};

export default function RootNavigation() {
  const [doneSplash, setDoneSplash] = useState(false);

  useEffect(() => {
    SplashScreen.hide();
  }, []);

  const Stack = createStackNavigator();

  return doneSplash ? (
    <NavigationContainer>
      <Stack.Navigator
        initialRouteName="Dashboard"
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
}
