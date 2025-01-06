import {computed, makeObservable, observable, runInAction} from 'mobx';
import firestore from '@react-native-firebase/firestore';
import Utils from 'src/utils/Utils';
import {IUser, IUserLogin} from './types';

class ProfileStore {
  user: IUser | null = null;
  firstLogin: boolean = false;
  externalLink: string = '';

  constructor() {
    makeObservable(this, {
      // observeralble variables
      user: observable,
      firstLogin: observable,
      externalLink: observable,

      // action functions
      isLoggedIn: computed,
    });
  }

  async init() {
    const res = await Utils.getObject('profile');
    const firstLogin = await Utils.getString('first-login');
    if (firstLogin == null) {
      runInAction(() => (this.firstLogin = true));
    }

    runInAction(() => {
      this.user = res;
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
