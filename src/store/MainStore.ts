import {makeObservable, observable, runInAction} from 'mobx';
import {IActionSheet} from 'src/types';

class MainStore {
  signupSuccessModalVisible: boolean = false;
  loginSuccessModalVisible: boolean = false;
  wrongPasswordModalVisible: boolean = false;
  timeRemaining: number = 0;
  forgotPasswordModalVisible: boolean = false;
  connectSuccessModalVisible: boolean = false;
  actionsheet: IActionSheet = {visible: false};

  constructor() {
    makeObservable(this, {
      // observeralble variables
      signupSuccessModalVisible: observable,
      loginSuccessModalVisible: observable,
      wrongPasswordModalVisible: observable,
      timeRemaining: observable,
      forgotPasswordModalVisible: observable,
      connectSuccessModalVisible: observable,
      actionsheet: observable,
    });
  }

  showSignupSuccessModal() {
    runInAction(() => (this.signupSuccessModalVisible = true));
  }

  hideSignupSuccessModal() {
    runInAction(() => (this.signupSuccessModalVisible = false));
  }

  showLoginSuccessModal() {
    runInAction(() => (this.loginSuccessModalVisible = true));
  }

  hideLoginSuccessModal() {
    runInAction(() => (this.loginSuccessModalVisible = false));
  }

  showWrongPasswordModal() {
    runInAction(() => (this.wrongPasswordModalVisible = true));
  }

  hideWrongPasswordModal() {
    runInAction(() => (this.wrongPasswordModalVisible = false));
  }

  setRetryTimeRemain(timeRemaining: number) {
    runInAction(() => (this.timeRemaining = timeRemaining));
  }

  showForgotPasswordModal() {
    runInAction(() => (this.forgotPasswordModalVisible = true));
  }

  hideForgotPasswordModal() {
    runInAction(() => (this.forgotPasswordModalVisible = false));
  }

  showConnectionSuccessModal() {
    runInAction(() => (this.connectSuccessModalVisible = true));
  }

  hideConnectionSuccessModal() {
    runInAction(() => (this.connectSuccessModalVisible = false));
  }

  showActionSheet(actionsheet: IActionSheet) {
    runInAction(() => (this.actionsheet = actionsheet));
  }

  hideActionSheet() {
    runInAction(() => (this.actionsheet = {visible: false}));
  }
}

export default new MainStore();
