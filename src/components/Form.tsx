import React, {useState} from 'react';
import {
  StyleSheet,
  View,
  Text,
  TextInput,
  TouchableOpacity,
} from 'react-native';
import FontAwesomeIcon from 'react-native-vector-icons/FontAwesome5';
import Octicon from 'react-native-vector-icons/Octicons';

import {useTheme} from 'src/theme/useTheme';
import {typeSizes} from 'src/theme/theme';
import {InputPropsType} from 'src/types/components';

const AuthInput = ({
  style,
  error,
  iconName,
  secureTextEntry,
  ...rest
}: InputPropsType) => {
  const {theme} = useTheme();
  const [isPasswordVisible, setPasswordVisible] = useState(false);

  const togglePasswordVisibility = () => {
    setPasswordVisible(!isPasswordVisible);
  };

  return (
    <View style={styles.container}>
      <View style={styles.inputWrp}>
        {iconName === 'user-circle' ? (
          <FontAwesomeIcon name={iconName} size={20} color="#000" />
        ) : (
          <Octicon name={iconName} size={20} color="#000" />
        )}
        <TextInput
          placeholderTextColor={'#A9A9A9'}
          {...rest}
          secureTextEntry={secureTextEntry && !isPasswordVisible}
          style={[
            styles.input,
            {color: theme.color, borderColor: theme.layoutBg},
            {...style},
          ]}
        />
        {iconName === 'user-circle' ? (
          <></>
        ) : (
          <TouchableOpacity
            onPress={togglePasswordVisibility}
            style={styles.iconContainer}>
            <FontAwesomeIcon
              name={isPasswordVisible ? 'eye' : 'eye-slash'}
              size={20}
              color="#D5D5D5"
            />
          </TouchableOpacity>
        )}
      </View>
      {error ? (
        <Text style={[styles.error, {color: theme.error}]}>{error}</Text>
      ) : null}
    </View>
  );
};

export {AuthInput};

const styles = StyleSheet.create({
  container: {
    marginBottom: 30,
  },
  inputWrp: {
    backgroundColor: '#FFF',
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 8,
    paddingHorizontal: 20,
  },
  input: {
    flex: 1,
    height: 50,
    paddingLeft: 20,
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
  },
  iconContainer: {
    marginLeft: 10,
  },
  error: {
    paddingLeft: 12,
    paddingTop: 12,
    fontSize: typeSizes.FONT_SIZE_SMALL,
  },
});
