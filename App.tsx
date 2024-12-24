import React from 'react';
import {SafeAreaProvider} from 'react-native-safe-area-context';

// Navigation
import RootNavigation from './src/routes/RootNavigation';
import {ThemeProvider} from './src/theme/useTheme';

let Root = function App() {
  return (
    <SafeAreaProvider>
      <ThemeProvider>
        <RootNavigation />
      </ThemeProvider>
    </SafeAreaProvider>
  );
};

export default Root;
