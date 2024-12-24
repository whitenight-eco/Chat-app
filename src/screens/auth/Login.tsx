import React, {useState} from 'react';
import {StyleSheet, View, ScrollView, Text, Alert} from 'react-native';
import Modal from 'react-native-modal';
import Icon from 'react-native-vector-icons/Feather';
import {Formik} from 'formik';
import * as Yup from 'yup';
import AsyncStorage from '@react-native-async-storage/async-storage';
import {sha256} from 'react-native-sha256';

import useHNavigation from 'src/hooks/useHNavigation';
import Layout from 'src/components/Layout';
import Card from 'src/components/Card';
import {Button} from 'src/components/Button/Button';
import {AuthInput} from 'src/components/Form';

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
  const [loginSuccessModalVisible, setloginSuccessModalVisible] =
    useState(false);
  const [forgotPassModalVisible, setForgotPassModalVisible] = useState(false);
  const [wrongPassModalVisible, setWrongPassModalVisible] = useState(false);
  const [timeRemaining, setTimeRemaining] = useState(0);

  const navigation = useHNavigation();

  const hideloginSuccessModalModal = () => {
    navigation.navigate('Home');
    setloginSuccessModalVisible(false);
  };

  const handleLogin = async (values: ValuesType) => {
    //
    try {
      // Retrieve private key from local storage
      const privateKey = await AsyncStorage.getItem('privateKey');
      if (!privateKey) {
        Alert.alert('Not registered', 'Please sign up first.', [
          {
            text: 'OK',
            onPress: () => {
              navigation.navigate('Signup');
            },
          },
        ]);
        return;
      }

      // Step 1: Retrieve the stored hashed password from local storage
      const storedHashedPassword = await AsyncStorage.getItem('password');
      if (!storedHashedPassword) {
        Alert.alert('Error', 'No password found. Please sign up first.');
        return;
      }

      // Step 2: Hash the entered password using SHA256
      const hashedPassword = await sha256(values.password);

      // Retrieve failed login attempts and lockout timestamp
      const failedAttempts = await AsyncStorage.getItem('failedAttempts');
      const lastFailedTime = await AsyncStorage.getItem('lastFailedTime');

      const failedAttemptsCount = failedAttempts ? parseInt(failedAttempts) : 0;
      const lastFailedTimestamp = lastFailedTime ? parseInt(lastFailedTime) : 0;

      // Check if the user is locked out
      const currentTime = Date.now();
      if (
        failedAttemptsCount >= MAX_FAILED_ATTEMPTS &&
        currentTime - lastFailedTimestamp < LOCKOUT_TIME
      ) {
        setTimeRemaining(
          Math.floor(
            (LOCKOUT_TIME - (currentTime - lastFailedTimestamp)) / 1000 / 60 +
              1,
          ),
        );
        setWrongPassModalVisible(true);
        return;
      }

      // Step 3: Compare the entered hashed password with the stored hashed password
      if (hashedPassword === storedHashedPassword) {
        setloginSuccessModalVisible(true);
        // Reset failed attempts after successful login
        await AsyncStorage.setItem('failedAttempts', '0');
      } else {
        await AsyncStorage.setItem(
          'failedAttempts',
          (failedAttemptsCount + 1).toString(),
        );
        await AsyncStorage.setItem('lastFailedTime', currentTime.toString());
        Alert.alert('Error', 'Please input correct password.');
      }
    } catch (error) {
      console.error('Error during login:', error);
      Alert.alert('Error', 'Something went wrong during login.');
    }
  };

  const handleForgotPasssword = async () => {
    await AsyncStorage.clear();
    setForgotPassModalVisible(false);
    navigation.navigate('Dashboard');
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
                    onPress={() => setForgotPassModalVisible(true)}>
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

        {/* Login Success Modal */}
        <Modal
          isVisible={loginSuccessModalVisible}
          onBackdropPress={hideloginSuccessModalModal}
          backdropOpacity={1.0}
          style={styles.modal}
          animationIn="zoomInUp"
          animationOut="zoomOut">
          <View style={styles.modalContainer}>
            <View style={styles.modalIconWrapper}>
              <Icon name="check" size={70} color="#FFFDFD" />
            </View>
            <Text style={styles.modalHealine}>Login Success</Text>
            <Text style={styles.modalDescription}>Welcome to Cypher!</Text>
          </View>
        </Modal>

        {/* Wrong Password Modal */}
        <Modal
          isVisible={wrongPassModalVisible}
          onBackdropPress={() => setWrongPassModalVisible(false)}
          backdropOpacity={1.0}
          style={styles.modal}
          animationIn="zoomInUp"
          animationOut="zoomOut">
          <View style={styles.modalContainer}>
            <View style={styles.modalIconWrapper2}>
              <Icon name="x" size={70} color="#FFFDFD" />
            </View>
            <Text style={styles.modalHealine}>Incorrect password</Text>
            <Text style={styles.modalDescription2}>
              Try again after {timeRemaining} minutes
            </Text>
          </View>
        </Modal>

        {/* Forgot Password Modal */}
        <Modal
          isVisible={forgotPassModalVisible}
          backdropOpacity={1.0}
          style={styles.modal}
          animationIn="zoomInUp"
          animationOut="zoomOut">
          <View style={styles.modalContainer}>
            <Text style={styles.forgotPassHeadline}>Forgot password</Text>
            <Text style={styles.forgotPassDescription}>
              Account stored encrypted on your device. we unable to reset your
              password. Delete account and start new?{'\n\n'}Note: Your
              contacts, messages and feeds will be permanently lost and deleted.
            </Text>
            <View style={styles.forgotPassActionWrapper}>
              <Text
                style={styles.cancelButton}
                onPress={() => setForgotPassModalVisible(false)}>
                Cancel
              </Text>
              <Text
                style={styles.deleteButton}
                onPress={() => handleForgotPasssword()}>
                Delete
              </Text>
            </View>
          </View>
        </Modal>
      </ScrollView>
    </Layout>
  );
};

export default Login;

const styles = StyleSheet.create({
  scrollview: {
    flexGrow: 1,
  },
  container: {
    flex: 1,
    justifyContent: 'flex-end',
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
  modal: {
    justifyContent: 'center',
    alignItems: 'center',
    margin: 0, // Removes default margin around modal
  },
  modalContainer: {
    width: '80%',
    height: '35%',
    backgroundColor: '#131212',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 20,
  },
  modalIconWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 100,
    height: 100,
    backgroundColor: '#000',
    borderRadius: 50,
    borderWidth: 3,
    borderColor: '#25FFAE',
    marginBottom: 20,
  },
  modalIconWrapper2: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 100,
    height: 100,
    backgroundColor: '#000',
    borderRadius: 50,
    borderWidth: 3,
    borderColor: '#FF3F25',
    marginBottom: 20,
  },
  modalHealine: {
    fontFamily: 'Poppins-Bold',
    fontSize: 24,
    color: '#FFF',
    marginBottom: 15,
  },
  modalDescription: {
    fontFamily: 'Poppins-SemiBold',
    fontSize: 14,
    color: '#F2F2F2',
    marginBottom: 30,
  },
  modalDescription2: {
    fontFamily: 'Poppins-SemiBold',
    fontSize: 14,
    color: '#FF0606',
    marginBottom: 30,
  },
  forgotPassHeadline: {
    fontFamily: 'PublicSans-Bold',
    fontSize: 19,
    color: '#FFF',
    marginBottom: 30,
  },
  forgotPassDescription: {
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
    color: '#FFF',
    lineHeight: 21,
  },
  forgotPassActionWrapper: {
    flexDirection: 'row',
    gap: 10,
    paddingHorizontal: 12,
    marginTop: 30,
  },
  cancelButton: {
    flex: 3,
    textAlign: 'center',
    color: '#FFF',
    padding: 10,
    backgroundColor: '#414141',
    borderRadius: 8,
  },
  deleteButton: {
    flex: 3,
    textAlign: 'center',
    color: '#FF3B30',
    padding: 10,
    backgroundColor: '#131212',
    borderColor: '#94A3B8',
    borderWidth: 1,
    borderRadius: 8,
  },
});
