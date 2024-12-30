import AsyncStorage from '@react-native-async-storage/async-storage';
import moment from 'moment';

class Utils {
  async storeObject(key: string, body: any) {
    await AsyncStorage.setItem(key, JSON.stringify(body));
  }

  async getObject(key: string) {
    const result = (await AsyncStorage.getItem(key)) as string;
    return result ? JSON.parse(result) : null;
  }

  async storeString(key: string, data: string) {
    await AsyncStorage.setItem(key, data);
  }

  async getString(key: string) {
    return await AsyncStorage.getItem(key);
  }

  async removeItem(key: string) {
    await AsyncStorage.removeItem(key);
  }

  async clearAll() {
    await AsyncStorage.clear();
  }

  async getAll() {
    return await AsyncStorage.getAllKeys();
  }

  isExpired(date: any) {
    if (date == null) return 'danger';
    const diff = moment(new Date(date)).diff(moment(), 'd');
    return diff <= 0 ? 'danger' : diff <= 0 ? 'warning' : 'safe';
  }
}

export default new Utils();
