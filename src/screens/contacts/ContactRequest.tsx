import AsyncStorage from '@react-native-async-storage/async-storage';
import firestore from '@react-native-firebase/firestore';
import ContactsStore from 'src/store/ContactsStore';
import {IContactRequest, IUser} from 'src/types';
import Utils from 'src/utils/Utils';

export const getUserByExternalLink = async (
  externalLink: string,
): Promise<IUser | null> => {
  try {
    const snapshot = await firestore()
      .collection('users')
      .where('externalLink', '==', externalLink)
      .get();

    if (!snapshot.empty) {
      const userDoc = snapshot.docs[0];
      return {id: userDoc.id, ...userDoc.data()} as IUser;
    } else {
      console.log('User not found for the provided external link');
      return null;
    }
  } catch (error) {
    console.error('Error fetching user by external link:', error);
    return null;
  }
};

export const sendContactRequest = async (
  toExternalLink: string,
): Promise<{success: boolean; error?: string}> => {
  try {
    const currentUser: IUser | null = await Utils.getObject('profile');
    if (!currentUser) {
      return {success: false, error: 'Current user details not found!'};
    }

    // Assuming you have a way to get the recipient's username and public key based on the external link
    const recipientUser: IUser | null = await getUserByExternalLink(
      toExternalLink,
    );
    if (!recipientUser) {
      return {
        success: false,
        error: 'Recipient not found for the provided link',
      };
    }

    const currentUserContacts: IUser[] =
      (await Utils.getObject('contacts')) || [];

    const usernameExists = currentUserContacts.some(
      item => item.username === recipientUser.username,
    );

    if (usernameExists) {
      return {
        success: false,
        error: 'Already added to the Contacts',
      };
    }

    // Send the contact request to Firestore
    await firestore().collection('requests').add({
      fromUsername: currentUser.username,
      fromPublicKey: currentUser.publicKey,
      toUsername: recipientUser.username, // Add recipient's username
      toPublicKey: recipientUser.publicKey, // Add recipient's public key
      toExternalLink: toExternalLink,
      status: 'pending',
      timestamp: firestore.FieldValue.serverTimestamp(),
    });

    console.log('Contact request sent successfully!');
    return {success: true};
  } catch (error) {
    console.error(`Error sending contact request: ${error}`);
    return {success: false, error: `Error sending contact request: ${error}`};
  }
};

export const listenForRequests = async (
  onRequestReceived: (request: IContactRequest) => Promise<void>,
): Promise<() => void> => {
  const currentUser = await Utils.getObject('profile');
  if (!currentUser) {
    console.error('Current user details not found!');
    return () => {};
  }

  // Set up a Firestore listener for incoming requests
  const unsubscribe = firestore()
    .collection('requests')
    .where('toUsername', '==', currentUser.username)
    .where('toPublicKey', '==', currentUser.publicKey)
    .where('status', '==', 'pending')
    .onSnapshot(snapshot => {
      if (snapshot.empty) {
        console.log('No pending requests found.');
      } else {
        snapshot.forEach(doc => {
          const request = {id: doc.id, ...doc.data()} as IContactRequest;
          console.log('Request received:', request); // This should log when requests are received
          onRequestReceived(request); // Trigger callback for each request
        });
      }
    });

  console.log('Listening for incoming requests...');
  return unsubscribe; // Return the unsubscribe function to stop listening
};

export const handleRequest = async (
  request: IContactRequest,
): Promise<{success: boolean; error?: string}> => {
  const {id, fromUsername, fromPublicKey} = request;

  try {
    const snapshot = await firestore()
      .collection('users')
      .where('username', '==', fromUsername)
      .where('publicKey', '==', fromPublicKey)
      .get();

    if (snapshot.empty) {
      return {success: false, error: 'The sender is not registered!'};
    }

    const senderDoc = snapshot.docs[0];
    const senderData = senderDoc.data();
    const senderContact: IUser = {
      id: senderDoc.id,
      username: senderData.username,
      avatar: senderData.avatar,
      netstats: senderData.netstats,
      publicKey: senderData.publicKey,
      externalLink: senderData.externalLink,
    };

    const currentUser: IUser | null = await Utils.getObject('profile');
    if (!currentUser) {
      return {success: false, error: 'Current user details not found!'};
    }

    // Add sender to current user's contacts
    const currentUserContacts: IUser[] =
      (await Utils.getObject('contacts')) || [];
    if (!currentUserContacts.some(contact => contact.id === senderContact.id)) {
      currentUserContacts.push(senderContact);
      await Utils.storeObject('contacts', currentUserContacts);
    }

    // Add current user to sender's contacts in Firestore
    const senderContactsRef = firestore()
      .collection('users')
      .doc(senderDoc.id)
      .collection('contacts');

    const senderContactsSnapshot = await senderContactsRef
      .where('id', '==', currentUser.id)
      .get();

    if (senderContactsSnapshot.empty) {
      await senderContactsRef.add({
        id: currentUser.id,
        username: currentUser.username,
        avatar: currentUser.avatar,
        publicKey: currentUser.publicKey,
        externalLink: currentUser.externalLink,
        netstats: currentUser.netstats,
      });
    }

    // Update request status
    await firestore()
      .collection('requests')
      .doc(id)
      .update({status: 'accepted'});

    await ContactsStore.getAllContacts();

    console.log('Request accepted and contacts updated successfully!');
    return {success: true};
  } catch (error) {
    console.error(`Error handling request: ${error}`);
    return {success: false, error: `Error handling request: ${error}`};
  }
};

export const getContacts = async (): Promise<IUser[]> => {
  try {
    const contacts: IUser[] = JSON.parse(
      (await AsyncStorage.getItem('contacts')) || '[]',
    );
    return contacts;
  } catch (error) {
    console.error('Error retrieving contacts:', error);
    return [];
  }
};
