import {makeObservable, observable, runInAction} from 'mobx';
import firestore from '@react-native-firebase/firestore';
import Utils from 'src/utils/Utils';
import {IUser} from 'src/types';

class ContactsStore {
  isConnected: boolean = false;
  users: IUser[] = [];

  constructor() {
    makeObservable(this, {
      // observeralble variables
      isConnected: observable,
      users: observable,
    });
  }

  async getAllContacts() {
    try {
      const res = await Utils.getObject('contacts');
      if (res) {
        runInAction(() => {
          this.users = res;
        });
      }
    } catch (error) {
      console.error('Error fetching contacts from storage:', error);
    }
  }
}

export default new ContactsStore();
