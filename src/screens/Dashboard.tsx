import React, {useState} from 'react';
import {StyleSheet, View, ScrollView, Text} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';

import useHNavigation from 'src/hooks/useHNavigation';
import Layout from 'src/components/Layout';
import Card from 'src/components/card';
import {Button} from 'src/components/button';

const Dashboard = () => {
  const [loading, setLoading] = useState<boolean>(false);
  const navigation = useHNavigation();

  const gotoLogin = () => {
    navigation.navigate('Login');
  };

  const gotoSign = () => {
    navigation.navigate('Signup');
  };

  return (
    <Layout>
      <ScrollView contentContainerStyle={styles.scrollview}>
        <View style={styles.container}>
          <Card style={styles.formWrapper}>
            <Text style={styles.headline}>
              Secure messaging and Stay connected even Offline!
            </Text>
            <View style={styles.descriptionWrapper}>
              <Icon name="arrow-forward" size={30} color="#FFF" />
              <Text style={styles.description}>
                Experience seamless communication. Cypher enables peer-to-peer
                (P2P) communication. Thanks to Bluetooth connection for ensuring
                communication continuity, empower us to communicate more freely
                and securely.
              </Text>
            </View>
            <View style={styles.lineWrapper}>
              <View style={styles.line} />
            </View>
            <View style={styles.buttonWrapper}>
              <Button
                style={styles.loginButton}
                text="Login"
                isLoading={loading}
                onPress={gotoLogin}
              />
              <Button
                style={styles.signButton}
                text="Sign"
                isLoading={loading}
                onPress={gotoSign}
              />
            </View>
          </Card>
        </View>
      </ScrollView>
    </Layout>
  );
};

export default Dashboard;

const styles = StyleSheet.create({
  scrollview: {
    flexGrow: 1,
  },
  container: {
    flex: 1,
    justifyContent: 'flex-end',
    alignItems: 'center',
  },
  formWrapper: {
    width: '100%',
  },
  headline: {
    color: '#FFF',
    fontFamily: 'Poppins-Bold',
    fontSize: 32, //35
    marginBottom: 30,
  },
  descriptionWrapper: {
    flexDirection: 'row',
    width: '100%',
    gap: 10,
    marginBottom: 100,
    flexWrap: 'wrap',
  },
  description: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    lineHeight: 27,
    fontSize: 14, //16
    flex: 1,
  },
  lineWrapper: {
    alignItems: 'center',
  },
  line: {
    width: '90%',
    height: 1,
    backgroundColor: '#F7F2F2',
  },
  buttonWrapper: {
    flexDirection: 'row',
    gap: 10,
    marginVertical: 20,
    paddingHorizontal: 20,
  },
  loginButton: {
    flex: 7,
    backgroundColor: '#FFFFFF',
  },
  signButton: {
    flex: 2,
    backgroundColor: '#05FCFC',
  },
});
