import React, {useState} from 'react';
import {StyleSheet, View, ScrollView, Text, Alert} from 'react-native';
import {Formik} from 'formik';
import * as Yup from 'yup';

import {RSA} from 'react-native-rsa-native';
import {sha256} from 'react-native-sha256';
import firestore from '@react-native-firebase/firestore';
import moment from 'moment';

import useHNavigation from 'src/hooks/useHNavigation';
import Layout from 'src/components/Layout';
import Card from 'src/components/Card';
import {Button} from 'src/components/Button/Button';
import {AuthInput} from 'src/components/Form';

import Utils from 'src/utils/Utils';

import MainStore from 'src/store/MainStore';
import SignupSuccessModal from './modal/SignupSuccessModal';

interface ValuesType {
  username: string;
  password: string;
  password2: string;
}

const initialValues: ValuesType = {username: '', password: '', password2: ''};

const SignupSchema = Yup.object().shape({
  username: Yup.string().min(2, 'Too Short!').required('Required'),
  password: Yup.string()
    .min(8, 'Password must be at least 8 characters')
    .max(12, 'Password must not exceed 12 characters')
    .matches(
      /^(?=.*[A-Za-z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,12}$/,
      'Password must be 8-12 characters and include at least one letter, one number, and one special character',
    )
    .required('Required'),
  password2: Yup.string()
    .oneOf([Yup.ref('password')], "Password doesn't match")
    .required('Required'),
});

const Signup = () => {
  const [loading, setLoading] = useState<boolean>(false);

  const navigation = useHNavigation();

  const handleSignUp = async (values: ValuesType) => {
    setLoading(true);
    try {
      // Check if the private key already exists in local storage
      const existingPrivateKey = await Utils.getString('privateKey');

      if (existingPrivateKey) {
        // If the private key exists, show an alert and navigate to the login page
        Alert.alert(
          'Account already exists',
          'You already have an account. Please login.',
          [
            {
              text: 'OK',
              onPress: () => {
                navigation.navigate('Login');
              },
            },
          ],
        );
        return; // Stop further execution if private key exists
      }

      // Generate key pair
      const keys = await RSA.generateKeys(2048);
      const {private: privateKey, public: publicKey} = keys;

      // Save private, public keys to local storage
      await Utils.storeString('privateKey', privateKey);
      await Utils.storeString('publicKey', publicKey);

      // Hash the password before storing it (using SHA256)
      const hashedPassword = await sha256(values.password);

      // Save password to local storage
      await Utils.storeString('password', hashedPassword);

      // Save username to local storage
      await Utils.storeString('username', values.username);

      // Save public key in Realtime Database
      // const userRef = database().ref('users').push();
      // await userRef.set({
      //   username: values.username,
      //   publicKey,
      // });

      const hashedPbulicKey = await sha256(publicKey);
      const shortHash = hashedPbulicKey.slice(0, 24);
      const externalLink = `cypher://${shortHash}`;
      await Utils.storeString('externalLink', externalLink);

      // Save username, public key in FireStore
      const docRef = await firestore().collection('users').add({
        username: values.username,
        publicKey,
        externalLink: externalLink,
        netstats: 'online_internet',
        createdDate: moment().toISOString(),
      });

      if (docRef.id) {
        setLoading(false);
        MainStore.showSignupSuccessModal();
      } else {
        setLoading(false);
      }
    } catch (error) {
      console.error('Error during signup:', error);
      Alert.alert('Error', 'Something went wrong during signup.');
    }
  };

  return (
    <Layout>
      <ScrollView contentContainerStyle={styles.scrollview}>
        <View style={styles.container}>
          <Card style={styles.formWrapper}>
            <Text style={styles.headline}>Create Your Account</Text>
            <Text style={styles.description}>
              Please complete all required field
            </Text>
            <Formik
              initialValues={initialValues}
              validationSchema={SignupSchema}
              onSubmit={handleSignUp}>
              {({
                handleChange,
                handleBlur,
                handleSubmit,
                values,
                errors,
                touched,
              }) => (
                <>
                  <AuthInput
                    testID="Signup.Username"
                    iconName="user-circle"
                    placeholder="What should be your name"
                    onChangeText={handleChange('username')}
                    onBlur={handleBlur('username')}
                    value={values.username}
                    keyboardType="email-address"
                    error={
                      errors.username && touched.username ? errors.username : ''
                    }
                  />
                  <AuthInput
                    testID="Signup.Password"
                    iconName="lock"
                    placeholder="Password"
                    onChangeText={handleChange('password')}
                    onBlur={handleBlur('password')}
                    value={values.password}
                    secureTextEntry
                    error={
                      errors.password && touched.password ? errors.password : ''
                    }
                  />
                  <AuthInput
                    testID="Signup.Password"
                    iconName="lock"
                    placeholder="Confirm password"
                    onChangeText={handleChange('password2')}
                    onBlur={handleBlur('password2')}
                    value={values.password2}
                    secureTextEntry
                    error={
                      errors.password2 && touched.password2
                        ? errors.password2
                        : ''
                    }
                  />
                  <Text style={styles.description2}>
                    A strong password helps prevent unauthorized access.
                  </Text>
                  <View style={styles.buttonWrapper}>
                    <Button
                      style={styles.button}
                      text="Submit"
                      isLoading={loading}
                      onPress={handleSubmit}
                    />
                  </View>
                </>
              )}
            </Formik>
          </Card>
        </View>

        <SignupSuccessModal />
      </ScrollView>
    </Layout>
  );
};

export default Signup;

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
    color: '#FFFFFF',
    fontFamily: 'Poppins-Bold',
    fontSize: 24,
  },
  description: {
    marginTop: -5,
    color: '#D2D2D2',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    marginBottom: 20,
  },
  description2: {
    color: '#D2D2D2',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
  },
  buttonWrapper: {
    paddingHorizontal: 20,
  },
  button: {
    marginTop: 130,
    marginBottom: 20,
    backgroundColor: '#05FCFC',
  },
});
