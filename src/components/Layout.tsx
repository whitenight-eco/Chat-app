import React from 'react';
import {StatusBar, StyleSheet, View} from 'react-native';
import {SafeAreaView} from 'react-native-safe-area-context';
import {useTheme} from 'src/contexts/useTheme';
import {spacing} from 'src/contexts/theme';

import {LayoutPropsType} from 'src/types/components';
import {ThemeContextInterface} from 'src/contexts/useTheme';

const Layout = ({children, style}: LayoutPropsType) => {
  const {theme}: Partial<ThemeContextInterface> = useTheme();
  return (
    <SafeAreaView style={styles.container}>
      <StatusBar
        animated
        backgroundColor={theme.cardBg}
        barStyle={theme?.name === 'light' ? 'dark-content' : 'light-content'}
      />
      <View style={[styles.layout, {backgroundColor: theme?.layoutBg}, style]}>
        {children}
      </View>
    </SafeAreaView>
  );
};

export default Layout;

const styles = StyleSheet.create({
  container: {flex: 1},
  layout: {
    flex: 1,
    backgroundColor: '#ffffff',
    paddingHorizontal: spacing.layoutPaddingH,
  },
});
