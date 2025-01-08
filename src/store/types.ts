export interface IUser {
  avatar?: string;
  username: string;
  publicKey: string;
  title?: string;
  appInstalled?: boolean;
  registered?: boolean;
}

export interface IUserLogin {
  username: string;
  password: string;
  publicKey: string;
}

export interface IContactsType {
  id: string;
  name: string;
  image: string;
  netstats: string;
}
