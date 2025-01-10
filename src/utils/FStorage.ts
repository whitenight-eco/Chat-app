
import storage, { FirebaseStorageTypes } from '@react-native-firebase/storage';
import 'react-native-get-random-values';
import { v4 } from 'uuid';

class FStorage {
    constructor(){

    }

    upload(path: string, bucket: string,
        cb?: (snapShot: FirebaseStorageTypes.TaskSnapshot) => void): Promise<string> {
        const extension = path.split(".").pop();
        const filename = v4() + "." + extension;
        const reference = storage().ref(`/${bucket}/${filename}`);
        const task = reference.putFile(path);
        return new Promise((resolve, reject) => {
            task.on("state_changed", cb);
            task.then(() => {
                const url = reference.getDownloadURL();
                resolve(url);
            }).catch(reject);
        })
    }

    uploadBase64(base64: string, bucket: string,
        cb?: (snapShot: FirebaseStorageTypes.TaskSnapshot) => void): Promise<string> {
        const filename = v4() + ".pdf";
        const reference = storage().ref(`/${bucket}/${filename}`);
        const task = reference.putString(base64, "base64");
        return new Promise((resolve, reject) => {
            task.on("state_changed", cb);
            task.then(() => {
                const url = reference.getDownloadURL();
                resolve(url);
            }).catch(reject);
        })
    }

    uploadImage(path: string, cb?: (snapShot: FirebaseStorageTypes.TaskSnapshot) => void) {
        return this.upload(path, "images", cb);
    }

    uploadVideo(path: string, cb?: (snapShot: FirebaseStorageTypes.TaskSnapshot) => void) {
        return this.upload(path, "videos", cb);
    }
}

export default new FStorage();