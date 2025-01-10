import React, {useRef, useState} from 'react';
import {
  StyleSheet,
  TextInput,
  TouchableOpacity,
  View,
  Keyboard,
} from 'react-native';
import {launchCamera, launchImageLibrary} from 'react-native-image-picker';

import AddIcon from 'react-native-vector-icons/Ionicons';
import MicIcon from 'react-native-vector-icons/FontAwesome5';
import SendIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import CameraIcon from 'react-native-vector-icons/FontAwesome5';

import EmojiModal from 'react-native-emoji-modal';

import FStorage from 'src/utils/FStorage';
import MainStore from 'src/store/MainStore';

interface IProps {
  onChangeText?: (text: string) => void;
  onSend: (data: {text?: string; image?: string; video?: string}) => void;
}

const ChatInputBox = (props: IProps) => {
  const [value, setValue] = useState('');
  const input = useRef<TextInput | any>();
  const [modal, setModal] = useState(false);

  const onChangeText = (text: string) => {
    props.onChangeText?.(text);
    setValue(text);
  };

  const onSend = () => {
    if (value === '') return;
    props.onSend({text: value});
    setValue('');
    hideEmojiPanel();
    setTimeout(() => input.current.focus(), 10);
  };

  const onPickImage = async () => {
    const result = await launchImageLibrary({
      mediaType: 'mixed',
      quality: 0.8,
      videoQuality: 'medium',
    });
    if (!result.didCancel && result.errorMessage == null) {
      const isImage = (result.assets?.[0].type?.indexOf('image') || 0) >= 0;
      const uploadResult = await (isImage
        ? FStorage.uploadImage
        : FStorage.uploadVideo
      ).bind(FStorage)(result.assets?.[0].uri || '');
      props.onSend({
        image: isImage ? uploadResult : undefined,
        video: isImage ? undefined : uploadResult,
      });
    }
  };

  const onCamera = async () => {
    const result = await launchCamera({
      mediaType: 'mixed',
      quality: 0.8,
      videoQuality: 'medium',
      durationLimit: 100,
      presentationStyle: 'fullScreen',
    });
    console.log(result);
    if (!result.didCancel && result.errorMessage == null) {
      const isImage = (result.assets?.[0].type?.indexOf('image') || 0) >= 0;
      const uploadResult = await (isImage
        ? FStorage.uploadImage
        : FStorage.uploadVideo
      ).bind(FStorage)(result.assets?.[0].uri || '');
      props.onSend({
        image: isImage ? uploadResult : undefined,
        video: isImage ? undefined : uploadResult,
      });
    }
  };

  const onImageUpload = () => {
    MainStore.showActionSheet({
      visible: true,
      buttons: [
        {text: 'Take a picture', onPress: onCamera},
        {text: 'Pick a image from library', onPress: onPickImage},
        // { text: "Record a voice", onPress: () => { } },
      ],
    });
  };

  const showEmojiPanel = () => {
    Keyboard.dismiss();
    setModal(true);
  };

  const hideEmojiPanel = () => {
    setModal(false);
  };

  return (
    <>
      <View style={styles.footer}>
        <TouchableOpacity onPress={showEmojiPanel}>
          <AddIcon name="add" size={30} color="#05FCFC" />
        </TouchableOpacity>

        <View style={styles.inputWrapper}>
          <TextInput
            ref={input}
            value={value}
            style={styles.input}
            placeholderTextColor={'#A9A9A9'}
            placeholder="Write"
            multiline={true}
            scrollEnabled={true}
            onChangeText={onChangeText}
            onFocus={hideEmojiPanel}
          />
          {value === '' && (
            <TouchableOpacity style={styles.micIconWrapper}>
              <MicIcon name="microphone" size={22} color="#000" />
            </TouchableOpacity>
          )}
        </View>
        {value !== '' && (
          <TouchableOpacity style={styles.sendIconWrapper} onPress={onSend}>
            <SendIcon name="send" size={32} color="#02FFFF" />
          </TouchableOpacity>
        )}

        <TouchableOpacity onPress={onImageUpload}>
          <View style={styles.camerIconaWrapper}>
            <CameraIcon name="camera" size={22} color="#000" />
          </View>
        </TouchableOpacity>
      </View>

      {modal && (
        <EmojiModal
          onPressOutside={hideEmojiPanel}
          modalStyle={styles.emojiModal}
          containerStyle={styles.emojiContainerModal}
          backgroundStyle={styles.emojiBackgroundModal}
          columns={5}
          emojiSize={30}
          activeShortcutColor="#2196f3"
          onEmojiSelected={emoji => {
            setValue(prevValue => `${prevValue} ${emoji || ''}`);
          }}
        />
      )}
    </>
  );
};

const styles = StyleSheet.create({
  footer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 10,
  },
  inputWrapper: {
    flex: 1,
    position: 'relative',
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#1A1A1A',
    borderColor: '#484848',
    borderWidth: 1,
    borderRadius: 10,
    paddingLeft: 5,
    paddingRight: 5,
  },
  input: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
    height: 45,
    paddingLeft: 10,
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    color: '#FFF',
    textAlignVertical: 'center',
  },
  micIconWrapper: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
    right: 10,
    top: 6,
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#02FFFF',
  },
  sendIconWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 32,
    height: 32,
  },
  camerIconaWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#02FFFF',
  },
  emojiModal: {},
  emojiContainerModal: {
    position: 'absolute',
    bottom: 0,
    left: 0,
    right: 0,
    height: 320,
  },
  emojiBackgroundModal: {},
});

export default ChatInputBox;
