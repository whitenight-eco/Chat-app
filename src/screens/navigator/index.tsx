import {createBottomTabNavigator} from '@react-navigation/bottom-tabs';
import {View} from 'react-native';

import MaterialIcon from 'react-native-vector-icons/MaterialIcons';
import MaterialCommunityIcons from 'react-native-vector-icons/MaterialCommunityIcons';

import ContactsScreen from 'src/screens/contacts';
import ChatsScreen from 'src/screens/chats';
import FeedsScreen from 'src/screens/feeds';

const TabNavigator = () => {
  const Tab = createBottomTabNavigator();

  const getTabIcon = (name: string, focused: boolean) => {
    const iconWrapperStyle = {
      backgroundColor: focused ? '#2F4156' : 'transparent',
      borderRadius: 20,
      alignItems: 'center' as 'center',
      paddingVertical: 3,
      paddingHorizontal: 20,
    };

    switch (name) {
      case 'Contacts':
        return (
          <View style={iconWrapperStyle}>
            <MaterialIcon
              name={focused ? 'people' : 'people-outline'}
              size={24}
              color="#FFF"
            />
          </View>
        );
      case 'Chats':
        return (
          <View style={iconWrapperStyle}>
            <MaterialCommunityIcons
              name={focused ? 'home' : 'home-outline'}
              size={24}
              color="#FFF"
            />
          </View>
        );
      case 'Feeds':
        return (
          <View style={iconWrapperStyle}>
            <MaterialCommunityIcons
              name={focused ? 'bell' : 'bell-outline'}
              size={24}
              color="#FFF"
            />
          </View>
        );
      default:
        return <></>;
    }
  };

  const CustomTabScreen = (name: string, component: any) => {
    return (
      <Tab.Screen
        name={name}
        component={component}
        options={{
          title: name,
          tabBarLabelStyle: {
            color: '#05FCFC',
            fontFamily: 'Poppins-Medium',
            fontSize: 12,
            lineHeight: 16,
            letterSpacing: 0.5,
            paddingBottom: 10,
          },
          tabBarIcon: ({focused}) => getTabIcon(name, focused),
        }}
      />
    );
  };

  return (
    <>
      <Tab.Navigator
        screenOptions={{
          headerShown: false,
          tabBarStyle: {
            backgroundColor: '#13232C',
            borderColor: '#000',
            height: 75,
            flexDirection: 'column',
            justifyContent: 'space-around',
            alignItems: 'center',
          },
        }}
        initialRouteName="Chats">
        {CustomTabScreen('Contacts', ContactsScreen)}
        {CustomTabScreen('Chats', ChatsScreen)}
        {CustomTabScreen('Feeds', FeedsScreen)}
      </Tab.Navigator>
    </>
  );
};

export default TabNavigator;
