import {computed, makeObservable, observable, runInAction} from 'mobx';
import firestore from '@react-native-firebase/firestore';
import Utils from 'src/utils/Utils';
import {IUser, IUserLogin} from './types';

class ProfileStore {
  user: IUser | null = null;
  firstLogin: boolean = false;
  externalLink: string = '';
  netstats: string = '';

  constructor() {
    makeObservable(this, {
      // observeralble variables
      user: observable,
      firstLogin: observable,
      externalLink: observable,
      netstats: observable,

      // action functions
      isLoggedIn: computed,
    });
  }

  async init() {
    const res = await Utils.getObject('profile');
    const externalLink = (await Utils.getString('externalLink')) || '';
    const firstLogin = await Utils.getString('first-login');
    if (firstLogin == null) {
      runInAction(() => (this.firstLogin = true));
    }

    runInAction(() => {
      this.user = res;
      this.externalLink = externalLink;
    });
  }

  async userLogin(data: IUserLogin) {
    try {
      const externalLink = await Utils.getString('externalLink');
      const snapshot = await firestore()
        .collection('users')
        .where('username', '==', data.username)
        .where('publicKey', '==', data.publicKey)
        .get();

      if (snapshot.empty === false && snapshot.size > 0) {
        snapshot.forEach(doc => {
          const docdata = doc.data();
          Utils.storeObject('profile', docdata);
          const {username, publicKey} = docdata;

          const userInfo: IUser = {
            username: username,
            publicKey: publicKey,
          };

          runInAction(() => {
            this.user = userInfo;
            this.externalLink = externalLink || '';
          });
        });
        return true;
      }
      return false;
    } catch (error) {
      return false;
    }
  }

  async logOut() {
    try {
      Utils.removeItem('profile');
      runInAction(() => {
        this.user = null;
      });
      return true;
    } catch (error) {
      return false;
    }
  }

  async deleteAllCollection(collectionName: string) {
    try {
      // Get all documents in the collection
      const snapshot = await firestore().collection(collectionName).get();

      // Loop through and delete each document
      const batch = firestore().batch();
      snapshot.docs.forEach(doc => {
        batch.delete(doc.ref);
      });

      // Commit the batch
      await batch.commit();
      console.log(`Collection '${collectionName}' deleted successfully.`);
    } catch (error) {
      console.error('Error deleting collection:', error);
    }
  }

  async getAllCollection(collectionName: string) {
    try {
      // Fetch all documents from the collection
      const snapshot = await firestore().collection(collectionName).get();

      // Map through the documents to extract data
      const documents = snapshot.docs.map(doc => ({
        id: doc.id, // Include the document ID
        ...doc.data(), // Include the document fields
      }));

      console.log('Documents:', documents);
      return documents;
    } catch (error) {
      console.error('Error fetching documents:', error);
    }
  }

  // async updateProfile(docid: any, profileData: any) {
  //   try {
  //     const res = await HendyApi.updateProfile(docid, profileData); //put(`${url.PROFILE_UPDATE}/${docid}`, profileData);
  //     console.log(res);
  //     if (res.success) {
  //       Utils.storeObject('profile', res.result);
  //       runInAction(() => {
  //         this.user = res.result;
  //       });
  //       return {success: true, message: 'Profile was updated!'};
  //     } else {
  //       return {success: false, message: 'Something went wrong!'};
  //     }
  //   } catch (error) {
  //     console.log(error);
  //     return error;
  //   }
  // }

  get isLoggedIn() {
    return this.user != null;
  }

  // async fetchUser() {
  //   if (this.user) {
  //     try {
  //       const res = await HendyApi.getCustomer(this.user?.id);
  //       this.user = res.result;
  //     } catch (error) {
  //       console.log(error);
  //     }
  //   }
  // }
}

export default new ProfileStore();
