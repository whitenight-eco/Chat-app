import React, {useState} from 'react';
import {StyleSheet, View, ScrollView, Text, Alert} from 'react-native';
import {Formik} from 'formik';
import * as Yup from 'yup';
import {sha256} from 'react-native-sha256';
import {observer} from 'mobx-react';

import useHNavigation from 'src/hooks/useHNavigation';
import Layout from 'src/components/Layout';
import Card from 'src/components/card';
import {Button} from 'src/components/button';
import {AuthInput} from 'src/components/form';

import Utils from 'src/utils/Utils';

import ProfileStore from 'src/store/ProfileStore';
import MainStore from 'src/store/MainStore';
import LoginSuccessModal from './modal/LoginSuccessModal';
import WrongPasswordModal from './modal/WrongPasswordModal';
import ForgotPasswordModal from './modal/ForgotPasswordModal';

interface ValuesType {
  password: string;
}

const initialValues: ValuesType = {password: ''};

const LoginSchema = Yup.object().shape({
  password: Yup.string().required('Required'),
});

const LOCKOUT_TIME = 5 * 60 * 1000; // 5 minutes in milliseconds
const MAX_FAILED_ATTEMPTS = 5;

const Login = () => {
  const [loading, setLoading] = useState(false);

  const navigation = useHNavigation();

  const handleLogin = async (values: ValuesType) => {
    setLoading(true);
    try {
      // Retrieve private key from local storage
      const privateKey = await Utils.getString('privateKey');
      const publicKey = await Utils.getString('publicKey');
      const username = await Utils.getString('username');

      if (!privateKey || !username) {
        Alert.alert('Not registered', 'Please sign up first.', [
          {
            text: 'OK',
            onPress: () => {
              navigation.navigate('Signup');
            },
          },
        ]);
        setLoading(false);
        return;
      }

      // Step 1: Retrieve the stored hashed password from local storage
      const storedHashedPassword = await Utils.getString('password');
      if (!storedHashedPassword) {
        Alert.alert('Error', 'No password found. Please sign up first.');
        setLoading(false);
        return;
      }

      // Step 2: Hash the entered password using SHA256
      const hashedPassword = await sha256(values.password);

      // Retrieve failed login attempts and lockout timestamp
      const failedAttempts = await Utils.getString('failedAttempts');
      const lastFailedTime = await Utils.getString('lastFailedTime');

      const failedAttemptsCount = failedAttempts ? parseInt(failedAttempts) : 0;
      const lastFailedTimestamp = lastFailedTime ? parseInt(lastFailedTime) : 0;

      // Check if the user is locked out
      const currentTime = Date.now();
      if (
        failedAttemptsCount >= MAX_FAILED_ATTEMPTS &&
        currentTime - lastFailedTimestamp < LOCKOUT_TIME
      ) {
        MainStore.setRetryTimeRemain(
          Math.floor(
            (LOCKOUT_TIME - (currentTime - lastFailedTimestamp)) / 1000 / 60 +
              1,
          ),
        );
        MainStore.showWrongPasswordModal();
        setLoading(false);
        return;
      }

      // Step 3: Compare the entered hashed password with the stored hashed password
      if (hashedPassword === storedHashedPassword) {
        // Reset failed attempts after successful login
        await Utils.storeString('failedAttempts', '0');
        const userLoginData = {
          username: username,
          password: hashedPassword,
          publicKey: publicKey || '',
        };
        const result = await ProfileStore.userLogin(userLoginData);

        if (result) {
          setLoading(false);
          MainStore.showLoginSuccessModal();
        } else {
          setLoading(false);
        }
      } else {
        await Utils.storeString(
          'failedAttempts',
          (failedAttemptsCount + 1).toString(),
        );
        await Utils.storeString('lastFailedTime', currentTime.toString());
        Alert.alert('Error', 'Please input correct password.');
        setLoading(false);
      }
    } catch (error) {
      console.error('Error during login:', error);
      Alert.alert('Error', 'Something went wrong during login.');
    }
  };

  return (
    <Layout>
      <ScrollView contentContainerStyle={styles.scrollview}>
        <View style={styles.container}>
          <Card style={styles.formWrapper}>
            <Text style={styles.headline}>Login</Text>
            <Text style={styles.description}>Fill in your password</Text>
            <Formik
              initialValues={initialValues}
              validationSchema={LoginSchema}
              onSubmit={handleLogin}>
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
                  <Text
                    style={styles.description2}
                    onPress={() => MainStore.showForgotPasswordModal()}>
                    Forgot password?
                  </Text>
                  <View style={styles.buttonWrapper}>
                    <Button
                      style={styles.button}
                      text="Continue"
                      isLoading={loading}
                      onPress={handleSubmit}
                    />
                  </View>
                </>
              )}
            </Formik>
          </Card>
        </View>

        <LoginSuccessModal />
        <WrongPasswordModal />
        <ForgotPasswordModal />
      </ScrollView>
    </Layout>
  );
};

export default observer(Login);

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
    textAlign: 'right',
    color: '#D2D2D2',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
  },
  buttonWrapper: {
    paddingHorizontal: 20,
  },
  button: {
    marginTop: 300,
    marginBottom: 20,
    backgroundColor: '#05FCFC',
  },
});
