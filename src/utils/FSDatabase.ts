import {
  firebase,
  FirebaseFirestoreTypes,
} from '@react-native-firebase/firestore';
import {RSA} from 'react-native-rsa-native';
import AES from 'crypto-js/aes';
import Utf8 from 'crypto-js/enc-utf8';

import {IChatDoc, IChatUserInfo, IMessage, IMessageDetail} from 'src/types';
import Utils from './Utils';

class FSDatabse {
  db: FirebaseFirestoreTypes.Module = firebase.firestore();

  /**
   * Encrypts a message using a symmetric key and encrypts the key with the recipient's public key.
   */
  async encryptMessage(
    message: IMessageDetail,
    recipientPublicKey: string,
    senderPublicKey: string,
  ): Promise<IMessageDetail> {
    // Generate a random symmetric key
    const symmetricKey = Math.random().toString(36).substring(2, 15);

    // Encrypt the message using the symmetric key
    const encryptedText = message.text
      ? AES.encrypt(message.text, symmetricKey).toString()
      : undefined;

    // Encrypt the symmetric key for the recipient and sender
    const encryptedKeyForRecipient = await RSA.encrypt(
      symmetricKey,
      recipientPublicKey,
    );
    const encryptedKeyForSender = await RSA.encrypt(
      symmetricKey,
      senderPublicKey,
    );

    return {
      ...message,
      text: encryptedText, // Encrypted message content
      encryptedKeyForRecipient,
      encryptedKeyForSender,
    };
  }

  /**
   * Decrypts a message for either the sender or the recipient using the user's private key.
   */
  async decryptMessage(
    message: IMessageDetail,
    privateKey: string,
    decryptForSender: boolean,
  ): Promise<IMessageDetail> {
    try {
      // Choose the correct encrypted key based on the recipient or sender
      const encryptedKey = decryptForSender
        ? message.encryptedKeyForSender
        : message.encryptedKeyForRecipient;

      if (!encryptedKey) {
        throw new Error('Missing encrypted symmetric key');
      }

      // Decrypt the symmetric key
      const symmetricKey = await RSA.decrypt(encryptedKey, privateKey);

      // Decrypt the message content
      const decryptedText = message.text
        ? AES.decrypt(message.text, symmetricKey).toString(Utf8)
        : undefined;

      if (!decryptedText) {
        console.error('Decryption failed for message:', message);
        throw new Error('Decryption failed');
      }

      // Return the decrypted message
      return {
        ...message,
        text: decryptedText, // Decrypted message content
      };
    } catch (error) {
      console.error('Error decrypting message:', error);
      return {
        ...message,
        text: 'Decryption failed', // Fallback text in case of failure
      };
    }
  }

  async send(channel: string, body: IMessageDetail, cb?: (error: any) => void) {
    try {
      const ref = this.db.collection('chats').doc(channel);
      const chatDoc = await ref.get();

      const senderPublicKey = (await Utils.getString('publicKey')) || '';

      const userWithPublicKey = chatDoc
        .data()
        ?.users.find(
          (user: IChatUserInfo) => user.publicKey !== senderPublicKey,
        );

      if (!userWithPublicKey || !userWithPublicKey.publicKey) {
        throw new Error('Recipient public key not found');
      }

      const recipientPublicKey = userWithPublicKey.publicKey;

      // Encrypt the message for the recipient
      const encryptedMessageForRecipient = await this.encryptMessage(
        body,
        recipientPublicKey,
        senderPublicKey,
      );

      // Encrypt the message for the sender
      const encryptedMessageForSender = await this.encryptMessage(
        body,
        senderPublicKey,
        senderPublicKey,
      );

      // Append new message to existing messages
      const newMessage: IMessage = {
        encryptedMessageForRecipient,
        encryptedMessageForSender,
      };
  
      const existingMessages = chatDoc.exists
        ? chatDoc.data()?.messages || []
        : [];
  
      const updatedMessages = [...existingMessages, newMessage];
  
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
      async snapshot => {
        if (snapshot.exists) {
          const data = snapshot.data() as IChatDoc;
          const currentUserPrivateKey =
            (await Utils.getString('privateKey')) || '';
          const currentUserPublicKey =
            (await Utils.getString('publicKey')) || '';
  
          // Decrypt messages
          const decryptedMessages = await Promise.all(
            data.messages.map(async message => {
              try {
                // Determine if the message should be decrypted for the sender or recipient
                const decryptForSender =
                  message.encryptedMessageForSender.user.publicKey ===
                  currentUserPublicKey;
  
                const encryptedMessage = decryptForSender
                  ? message.encryptedMessageForSender
                  : message.encryptedMessageForRecipient;
  
                const decryptedMessageDetail = await this.decryptMessage(
                  encryptedMessage,
                  currentUserPrivateKey,
                  decryptForSender,
                );
  
                return {
                  encryptedMessageForRecipient: message.encryptedMessageForRecipient,
                  encryptedMessageForSender: message.encryptedMessageForSender,
                  ...decryptedMessageDetail,
                };
              } catch (error) {
                console.error('Error decrypting message:', error);
  
                // Return a valid IMessage object with fallback content
                return {
                  encryptedMessageForRecipient: message.encryptedMessageForRecipient,
                  encryptedMessageForSender: message.encryptedMessageForSender,
                  text: 'Decryption failed',
                  createdAt: message.encryptedMessageForRecipient.createdAt,
                  user: message.encryptedMessageForRecipient.user,
                };
              }
            }),
          );
  
          listener?.({
            ...data,
            messages: decryptedMessages, // Ensure type compatibility
          });
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
        const existingMessages: IMessageDetail[] = chatDoc.data()?.messages || [];
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
