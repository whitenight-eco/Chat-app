import {makeObservable, observable, runInAction} from 'mobx';
import firestore from '@react-native-firebase/firestore';
import Utils from 'src/utils/Utils';
import {IContactsType} from './types';

class ContactsStore {
  isConnected: boolean = false;
  contacts: IContactsType[] = [];

  constructor() {
    makeObservable(this, {
      // observeralble variables
      isConnected: observable,
      contacts: observable,
    });
  }

  async getAllContacts() {
    try {
      const res = await Utils.getObject('contacts');

      if (res) {
        runInAction(() => {
          this.contacts = res;
        });
      }
    } catch (error) {
      console.error('Error fetching contacts from storage:', error);
    }
  }

  async checkLink(
    contactLink: string,
  ): Promise<{success: boolean; error?: string}> {
    try {
      const snapshot = await firestore()
        .collection('users')
        .where('externalLink', '==', contactLink)
        .get();

      if (!snapshot.empty) {
        let contactsArray: IContactsType[] =
          (await Utils.getObject('contacts')) || [];

        for (const doc of snapshot.docs) {
          const docdata = doc.data();
          const documentId = doc.ref.id;
          const {username, netstats} = docdata;

          const contact: IContactsType = {
            id: documentId,
            name: username,
            image: 'https://via.placeholder.com/50',
            netstats: netstats,
          };

          // Check for duplicate ID
          const isDuplicate = contactsArray.some(
            existingContact => existingContact.id === contact.id,
          );
          if (!isDuplicate) {
            contactsArray.push(contact);
            await Utils.storeObject('contacts', contactsArray);

            runInAction(() => {
              this.contacts = contactsArray;
            });

            return {success: true};
          } else {
            return {
              success: false,
              error: `Contact with that link '${contactLink}' already exists.`,
            };
          }
        }
      }

      return {success: false, error: 'No matching contact found in Firestore.'};
    } catch (error) {
      console.error('Error:', error);
      return {success: false, error: 'An unexpected error occurred.'};
    }
  }
}

export default new ContactsStore();
