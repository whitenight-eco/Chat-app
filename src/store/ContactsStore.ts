import {makeObservable, observable, runInAction} from 'mobx';
import firestore from '@react-native-firebase/firestore';
import Utils from 'src/utils/Utils';
import {IUser} from 'src/types';

class ContactsStore {
  isConnected: boolean = false;
  contacts: IUser[] = [];

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
}

export default new ContactsStore();
