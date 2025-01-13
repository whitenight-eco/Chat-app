import {
  firebase,
  FirebaseFirestoreTypes,
} from '@react-native-firebase/firestore';
import {IChatDoc, IMessage} from 'src/types';

class FSDatabse {
  db: FirebaseFirestoreTypes.Module = firebase.firestore();

  async send(channel: string, body: IMessage, cb?: (error: any) => void) {
    try {
      const ref = this.db.collection('chats').doc(channel);
      const chatDoc = await ref.get();

      // Append new message to existing messages
      const existingMessages = chatDoc.exists
        ? chatDoc.data()?.messages || []
        : [];
      const updatedMessages = [...existingMessages, body];

      await ref.set(
        {messages: updatedMessages, lastUpdated: Date.now()},
        {merge: true},
      );

      // Optional callback
      cb?.(null);
    } catch (error) {
      console.error('Error sending message:', error);
      cb?.(error);
    }
  }

  load(channel: string, listener?: (chatDoc: IChatDoc) => void): () => void {
    const ref = this.db.collection('chats').doc(channel);

    const unsubscribe = ref.onSnapshot(
      snapshot => {
        if (snapshot.exists) {
          const data = snapshot.data() as IChatDoc;
          listener?.(data);
        } else {
          listener?.({
            chatId: ref.id,
            lastUpdated: 0,
            groupName: '',
            groupAvatar: '',
            users: [],
            lastAccess: [],
            messages: [],
          });
        }
      },
      error => {
        console.error('Error loading chat document:', error.message);
      },
    );

    return unsubscribe;
  }

  async removeMessage(channel: string, messageId: string) {
    try {
      const ref = this.db.collection('chats').doc(channel);
      const chatDoc = await ref.get();

      if (chatDoc.exists) {
        const existingMessages: IMessage[] = chatDoc.data()?.messages || [];
        const updatedMessages = existingMessages.filter(
          msg => msg.createdAt !== Number(messageId),
        );

        await ref.set({messages: updatedMessages}, {merge: true});
      }
    } catch (error) {
      console.error('Error removing message:', error);
    }
  }
}

export default new FSDatabse();
