import React, {useState, useRef, useEffect} from 'react';
import {View, StyleSheet} from 'react-native';

import Layout from 'src/screens/Layout';

import CommonHeader from 'src/components/header';
import ChatIndividualInputBox from 'src/components/chat/ChatIndividualInputBox';
import ChatGroupInputBox from 'src/components/chat/ChatGroupInputBox';
import ChatList from 'src/components/chat/ChatList';

import ProfileStore from 'src/store/ProfileStore';
import ContactsStore from 'src/store/ContactsStore';
import FSDatabase from 'src/utils/FSDatabase';
import {ChatProps, IMessage, IChatUserInfo} from 'src/types';

const Chat: React.FC<ChatProps> = ({route}) => {
  const {channel, chatName} = route.params;
  const [messages, setMessages] = useState<IMessage[]>([]);
  const [avatar, setAvatar] = useState<string>('');
  const [isGroup, setIsGroup] = useState<boolean>(false);
  const [netstats, setNetstats] = useState<string>('');
  const hasMounted = useRef<boolean>(true);

  const currentUser = ProfileStore.user;

  const onSend = (data: {text?: string; image?: string; video?: string}) => {
    const user = ProfileStore.user;
    FSDatabase.send(
      channel,
      {
        ...data,
        createdAt: Date.now(),
        user: {
          id: user?.id || '',
          name: user?.username || '',
        },
      },
      error => {
        if (error) console.error(error);
      },
    );
  };

  const getAvatarAndNetstats = (
    users: IChatUserInfo[],
  ): {avatar: string; netstats: string} => {
    const chattingUser =
      users[0].publicKey === currentUser?.publicKey ? users[1] : users[0];

    const chattingUserInfo = ContactsStore.contacts.find(
      item => item.publicKey === chattingUser?.publicKey,
    );

    return {
      avatar: chattingUserInfo?.avatar || '',
      netstats: chattingUserInfo?.netstats || '',
    };
  };

  useEffect(() => {
    const unsubscribe = FSDatabase.load(channel, chatDoc => {
      if (hasMounted.current) {
        setMessages(chatDoc.messages);
        if (chatDoc.groupName !== '') {
          setIsGroup(true);
          setAvatar(chatDoc.groupAvatar || 'default');
        } else {
          setIsGroup(false);
          const result = getAvatarAndNetstats(chatDoc.users);
          setAvatar(result.avatar || 'default');
          setNetstats(result.netstats || '');
        }
      }
    });

    return () => {
      hasMounted.current = false;
      unsubscribe();
    };
  }, []);

  return (
    <Layout>
      <View style={styles.container}>
        <View style={styles.chatContainer}>
          <CommonHeader
            avatar={avatar}
            isGroup={isGroup}
            netstats={netstats}
            headerName={chatName}
            iconExist={true}
          />
          <ChatList messages={messages} />
          {isGroup ? (
            <ChatGroupInputBox onSend={onSend} />
          ) : (
            <ChatIndividualInputBox onSend={onSend} />
          )}
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
