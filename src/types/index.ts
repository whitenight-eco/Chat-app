import {RouteProp} from '@react-navigation/native';

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

export interface IChatUserInfo {
  publicKey: string;
  externalLink: string;
  username: string;
  deletedFromChat: boolean;
}

interface IChatLastAccess {
  username: string;
  externalLink: string;
  date: number;
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

interface IGroupAdminInfo {
  publicKey: string;
  externalLink: string;
  username: string;
}

export interface IChatDoc {
  chatId: string;
  groupAdmins?: IGroupAdminInfo;
  lastUpdated: number;
  groupName: string;
  groupAvatar?: string;
  users: IChatUserInfo[];
  lastAccess: IChatLastAccess[];
  messages: IMessage[];
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

export interface IExistingChat {
  chatId: string;
  users: IUser[];
}

export interface IChatRouteProp {
  channel: string;
  chatName: string;
}

export type RootStackParamList = {
  Home: undefined;
  Dashboard: undefined;
  Login: undefined;
  Signup: undefined;
  AddContacts: undefined;
  QrScan: undefined;
  Chat: IChatRouteProp; // Ensure the type matches the route params for the Chat screen
};

type ChatRouteProp = RouteProp<RootStackParamList, 'Chat'>;

export interface ChatProps {
  route: ChatRouteProp;
}
