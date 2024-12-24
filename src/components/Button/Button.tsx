import * as React from 'react';
import {
  TouchableOpacity,
  Text,
  StyleSheet,
  ActivityIndicator,
  ViewStyle,
  StyleProp,
} from 'react-native';
import {useTheme, ThemeContextInterface} from '../../theme/useTheme';

export type ButtonProps = {
  onPress: () => void;
  text: string;
  isLoading?: boolean;
  style?: StyleProp<ViewStyle>;
  disabled?: boolean;
};

export const Button = ({
  onPress,
  text,
  isLoading = false,
  style,
  disabled = false,
}: ButtonProps) => {
  const {theme}: Partial<ThemeContextInterface> = useTheme();

  const styles = StyleSheet.create({
    container: {
      paddingHorizontal: 16,
      paddingVertical: 16,
      backgroundColor: isLoading
        ? theme.buttonColor.primaryInactive
        : theme.buttonColor.primary,
      borderRadius: 32,
    },
    text: {
      color: 'black',
      textAlign: 'center',
      fontSize: 16,
      fontFamily: 'Urbanist-Bold',
    },
  });

  return (
    <TouchableOpacity
      style={[styles.container, style]}
      onPress={!isLoading ? onPress : () => {}}
      activeOpacity={isLoading ? 1 : 0.8}
      disabled={disabled}>
      {isLoading ? (
        <ActivityIndicator size={20} />
      ) : (
        <Text style={styles.text}>{text}</Text>
      )}
    </TouchableOpacity>
  );
};
