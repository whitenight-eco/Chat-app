import React, {useState} from 'react';
import {StyleSheet, View, ScrollView, Text} from 'react-native';
import Icon from 'react-native-vector-icons/MaterialIcons';

import useHNavigation from 'src/hooks/useHNavigation';
import Layout from 'src/components/Layout';
import {Button} from 'src/components/Button/Button';

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
    justifyContent: 'center',
    alignItems: 'center',
    marginTop: 170,
  },
  headline: {
    color: '#ffffff',
    fontFamily: 'Poppins-Bold',
    fontSize: 35,
    marginBottom: 60,
  },
  descriptionWrapper: {
    flexDirection: 'row',
    width: '100%',
    paddingRight: 20,
    gap: 12,
  },
  description: {
    color: '#ffffff',
    fontFamily: 'Poppins-Regular',
    lineHeight: 27,
    fontSize: 16,
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
    marginVertical: 35,
    paddingHorizontal: 22
  },
  loginButton: {
    flex: 7,
    backgroundColor: '#FFFFFF',
  },
  signButton: {
    flex: 1,
    backgroundColor: '#05FCFC',
  },
});
