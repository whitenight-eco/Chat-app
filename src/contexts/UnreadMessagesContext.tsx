import React, {createContext, useState, useEffect, ReactNode} from 'react';
import Utils from 'src/utils/Utils';

// Define the shape of the context
interface UnreadMessagesContextType {
  unreadCount: number;
  setUnreadCount: React.Dispatch<React.SetStateAction<number>>;
}

// Create the context with an initial value
export const UnreadMessagesContext = createContext<UnreadMessagesContextType>({
  unreadCount: 0,
  setUnreadCount: () => {}, // Default no-op function
});

// Define the props for the provider
interface UnreadMessagesProviderProps {
  children: ReactNode;
}

export const UnreadMessagesProvider: React.FC<UnreadMessagesProviderProps> = ({
  children,
}) => {
  const [unreadCount, setUnreadCount] = useState<number>(0);

  useEffect(() => {
    const loadUnreadCount = async () => {
      try {
        const storedMessages = Utils.getObject('newMessages');

        const count = Object.values(storedMessages).reduce(
          (total, num) => total + num,
          0,
        );
        setUnreadCount(count);
      } catch (error) {
        console.error('Error loading unread messages count', error);
      }
    };
    loadUnreadCount();
  }, []);

  return (
    <UnreadMessagesContext.Provider value={{unreadCount, setUnreadCount}}>
      {children}
    </UnreadMessagesContext.Provider>
  );
};
