import React from 'react';
import {SafeAreaProvider} from 'react-native-safe-area-context';

// Navigation
import RootNavigation from './src/routes/RootNavigation';
import {ThemeProvider} from './src/contexts/useTheme';
import {UnreadMessagesProvider} from 'src/contexts/UnreadMessagesContext';

let Root = function App() {
  return (
    <SafeAreaProvider>
      <ThemeProvider>
        <UnreadMessagesProvider>
          <RootNavigation />
        </UnreadMessagesProvider>
      </ThemeProvider>
    </SafeAreaProvider>
  );
};

export default Root;
