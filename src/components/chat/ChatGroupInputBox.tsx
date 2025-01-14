import React, {useRef, useState} from 'react';
import {
  StyleSheet,
  Text,
  TextInput,
  TouchableOpacity,
  View,
  Modal,
} from 'react-native';
import {launchCamera, launchImageLibrary} from 'react-native-image-picker';

import AddIcon from 'react-native-vector-icons/Ionicons';
import SendIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import GalleryIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import CameraIcon from 'react-native-vector-icons/FontAwesome5';
import AudioIcon from 'react-native-vector-icons/MaterialIcons';
import FileIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import PollIcon from 'react-native-vector-icons/EvilIcons';
import EventIcon from 'react-native-vector-icons/MaterialIcons';

import FStorage from 'src/utils/FStorage';

interface IProps {
  onChangeText?: (text: string) => void;
  onSend: (data: {text?: string; image?: string; video?: string}) => void;
}

const ChatGroupInputBox = (props: IProps) => {
  const [value, setValue] = useState('');
  const input = useRef<TextInput | any>();
  const [modalVisible, setModalVisible] = useState(false);

  const onChangeText = (text: string) => {
    props.onChangeText?.(text);
    setValue(text);
  };

  const onSend = () => {
    if (value === '') return;
    props.onSend({text: value});
    setValue('');
    setTimeout(() => input.current.focus(), 10);
  };

  const onPickImage = async () => {
    setModalVisible(false); // Close modal
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
    setModalVisible(false); // Close modal
    const result = await launchCamera({
      mediaType: 'mixed',
      quality: 0.8,
      videoQuality: 'medium',
      durationLimit: 100,
      presentationStyle: 'fullScreen',
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

  const options = [
    {label: 'gallery', onPress: onPickImage},
    {label: 'camera', onPress: onCamera},
    {label: 'audio', onPress: () => {}},
    {label: 'file', onPress: () => {}},
    {label: 'poll', onPress: () => {}},
    {label: 'event', onPress: () => {}},
  ];

  const getModalIcon = (label: string) => {
    switch (label) {
      case 'gallery':
        return <GalleryIcon name="image" size={40} color="#2C2C2C" />;
      case 'camera':
        return <CameraIcon name="camera" size={40} color="#000" />;
      case 'audio':
        return <AudioIcon name="multitrack-audio" size={40} color="#000" />;
      case 'file':
        return <FileIcon name="file-document-outline" size={40} color="#000" />;
      case 'poll':
        return <PollIcon name="chart" size={40} color="#000" />;
      case 'event':
        return <EventIcon name="event-note" size={40} color="#000" />;
      default:
        return null;
    }
  };

  const handleNavigate = (label: string) => {
    switch (label) {
      case 'gallery':
        return <GalleryIcon name="image" size={40} color="#2C2C2C" />;
      case 'camera':
        return <CameraIcon name="camera" size={40} color="#000" />;
      case 'audio':
        return <AudioIcon name="multitrack-audio" size={40} color="#000" />;
      case 'file':
        return <FileIcon name="file-document-outline" size={40} color="#000" />;
      case 'poll':
        return <PollIcon name="chart" size={40} color="#000" />;
      case 'event':
        return <EventIcon name="event-note" size={40} color="#000" />;
      default:
        return null;
    }
  };

  return (
    <>
      <View style={styles.footer}>
        <TouchableOpacity onPress={() => setModalVisible(true)}>
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
          />
        </View>
        {value !== '' && (
          <TouchableOpacity style={styles.sendIconWrapper} onPress={onSend}>
            <SendIcon name="send" size={32} color="#02FFFF" />
          </TouchableOpacity>
        )}
      </View>

      {/* Custom Modal */}
      <Modal
        animationType="slide"
        transparent={true}
        visible={modalVisible}
        onRequestClose={() => setModalVisible(false)}>
        <View style={styles.modalContainer}>
          <View style={styles.modalContent}>
            {/* Close Button */}
            <TouchableOpacity
              style={styles.closeLineWrapper}
              onPress={() => setModalVisible(false)}>
              <View style={styles.closeLine} />
            </TouchableOpacity>

            {/* Separator */}
            <View style={styles.separateLineWrapper}>
              <View style={styles.separateLine} />
            </View>

            {/* Grid of Options */}
            <View style={styles.optionsGrid}>
              {options.map((option, index) => (
                <TouchableOpacity
                  key={index}
                  style={styles.optionWrapper}
                  onPress={() => {
                    console.log(option.label);
                    handleNavigate(option.label);
                    setModalVisible(false);
                  }}>
                  <View style={styles.optionButton}>
                    {getModalIcon(option.label)}
                  </View>
                  <Text style={styles.optionText}>{option.label}</Text>
                </TouchableOpacity>
              ))}
            </View>
          </View>
        </View>
      </Modal>
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
    height: 45,
    paddingLeft: 10,
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    color: '#FFF',
  },
  micIconWrapper: {
    position: 'absolute',
    right: 10,
    top: 6,
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#02FFFF',
    justifyContent: 'center',
    alignItems: 'center',
  },
  sendIconWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 32,
    height: 32,
  },
  modalContainer: {
    flex: 1,
    justifyContent: 'flex-end',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  modalContent: {
    position: 'relative',
    backgroundColor: '#333333',
    paddingHorizontal: 20,
    paddingBottom: 20,
    borderTopLeftRadius: 20,
    borderTopRightRadius: 20,
  },
  closeLineWrapper: {
    position: 'absolute',
    alignItems: 'center',
    top: 10,
    left: 0,
    right: 0,
    zIndex: 10,
  },
  closeLine: {
    width: '10%',
    height: 4,
    borderRadius: 10,
    backgroundColor: '#DEDEDE',
  },
  separateLineWrapper: {
    position: 'absolute',
    top: 50,
    left: 0,
    right: 0,
  },
  separateLine: {
    width: '100%',
    height: 1,
    backgroundColor: '#DEDEDE',
  },
  optionsGrid: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    flexWrap: 'wrap',
    columnGap: 50,
    marginTop: 60,
  },
  optionWrapper: {
    flexDirection: 'column',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
  },
  optionButton: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 60,
    height: 60,
    backgroundColor: '#05FCFC',
    borderRadius: 30,
    marginTop: 20,
  },
  optionText: {
    fontFamily: 'Poppins-Regular',
    fontSize: 15,
    color: '#FFF',
    textAlign: 'center',
  },
});

export default ChatGroupInputBox;
