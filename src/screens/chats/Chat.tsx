import React, {useState, useRef, useEffect} from 'react';
import {View, StyleSheet} from 'react-native';

import Layout from 'src/screens/Layout';

import CommonHeader from 'src/components/header';
import ChatInputBox from 'src/components/chat/ChatsInputBox';
import ChatList from 'src/components/chat/ChatList';

import ProfileStore from 'src/store/ProfileStore';
import FSDatabase from 'src/utils/FSDatabase';
import {ChatProps, IMessage} from 'src/types';

const Chat: React.FC<ChatProps> = ({route}) => {
  const {channel} = route.params;
  const {username, avatar, netstats} = route.params.user;

  const [messages, setMessages] = useState<IMessage[]>([]);
  const hasMounted = useRef<boolean>(true);

  const onSend = (data: {text?: string; image?: string; video?: string}) => {
    const user = ProfileStore.user;
    FSDatabase.send(
      channel,
      {
        ...data,
        createdAt: new Date().getTime(),
        user: {
          id: user?.id || '',
          name: user?.username || '',
        },
      },
      error => {
        console.log(error);
      },
    );
  };

  useEffect(() => {
    FSDatabase.load(channel, data => {
      const messages = Object.entries(data || {})
        .map(([key, message]) => ({
          key,
          ...message,
        }))
        .sort((a, b) => b.createdAt - a.createdAt);

      if (hasMounted.current) {
        setMessages(messages);
      }
    });
    return () => {
      hasMounted.current = false;
    };
  }, []);

  return (
    <Layout>
      <View style={styles.container}>
        <View style={styles.chatContainer}>
          <CommonHeader
            image={avatar}
            username={username}
            netstats={netstats}
            headerName="Chat"
            iconExist={true}
          />
          <ChatList messages={messages} />
          <ChatInputBox onSend={onSend} />
        </View>
      </View>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    flexDirection: 'column',
  },
  chatContainer: {
    flex: 1,
  },
});

export default Chat;
