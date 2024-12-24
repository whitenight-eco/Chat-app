import React from 'react';
import {StyleSheet, View} from 'react-native';
import {useTheme} from 'src/theme/useTheme';
import {spacing} from 'src/theme/theme';
import {CardPropsType} from 'src/types/components';

const Card = ({children, style}: CardPropsType) => {
  const {theme} = useTheme();
  return (
    <View
      style={[
        styles.card,
        {backgroundColor: theme?.cardBg, borderColor: theme?.cardBorderColor},
        style,
      ]}>
      {children}
    </View>
  );
};

export default Card;

const styles = StyleSheet.create({
  card: {
    width: '100%',
    paddingHorizontal: spacing.layoutPaddingH,
    paddingVertical: spacing.layoutPaddingH,
    borderWidth: StyleSheet.hairlineWidth,
    borderRadius: spacing.borderRadius,
    backgroundColor: '#000',
    // borderColor: '#ffffff',
  },
});

// IntrinsicAttributes
