export interface IUser {
  id: string;
  username: string;
  avatar?: string;
  netstats: string;
  publicKey: string;
  externalLink: string;
  registered?: boolean;
}

export interface IUserLogin {
  username: string;
  password: string;
  publicKey: string;
}

export interface IChatsType {
  id: string;
  username: string;
  avatar: string;
  netstats: string;
}

export interface IActionSheet {
  visible?: boolean;
  buttons?: Array<{
    color?: string;
    onPress?: () => void;
    text?: string;
  }>;
  onCancel?: () => void;
}

export interface IMessage {
  text?: string;
  image?: string;
  video?: string;
  createdAt: number;
  user: {
    id: string;
    name: string;
  };
}

export interface IContactRequest {
  id: string;
  fromUsername: string;
  fromPublicKey: string;
  fromExternalLink: string;
  toUsername: string;
  status: 'pending' | 'accepted' | 'declined';
  timestamp: string;
}

export type RootStackParamList = {
  Home: undefined;
  Dashboard: undefined;
  Login: undefined;
  Signup: undefined;
  AddContacts: undefined;
  QrScan: undefined;
  Chat: IUser; // Ensure the type matches the route params for the Chat screen
};
