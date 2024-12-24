import {useNavigation} from '@react-navigation/native';
import {Keyboard} from 'react-native';
import useKeyboard from './useKeyboard';

const useHNavigation = () => {
  const navigation = useNavigation<any>();
  const showKeyboard = useKeyboard();

  const navigate = (screen: string, params?: any) => {
    if (showKeyboard) {
      Keyboard.dismiss();
      setTimeout(() => navigation.navigate(screen, params), 500);
    } else {
      navigation.navigate(screen, params);
    }
  };

  const goBack = () => {
    if (showKeyboard) {
      Keyboard.dismiss();
      setTimeout(() => navigation.goBack(), 500);
    } else {
      navigation.goBack();
    }
  };

  return {
    navigate,
    goBack,
  };
};

export default useHNavigation;
