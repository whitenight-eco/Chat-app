import React from 'react';
import {View, Text, StyleSheet} from 'react-native';
import Layout from '../Layout';
import {ScrollView} from 'react-native-gesture-handler';

const ContactsScreen = ({
  navigation,
  route,
}: {
  navigation: any;
  route: any;
}): React.ReactElement => {
  return (
    <Layout>
      <ScrollView nestedScrollEnabled={true}>
        <View style={styles.container}>
          <Text style={styles.text}>Contacts Screen</Text>
        </View>
      </ScrollView>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    flexDirection: 'column',
  },
  text: {
    color: '#FFF',
    fontSize: 20,
  },
});
export default ContactsScreen;
