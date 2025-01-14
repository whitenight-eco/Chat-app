import React, {useState} from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  TextInput,
} from 'react-native';

import {launchCamera, launchImageLibrary} from 'react-native-image-picker';

import GroupIcon from 'react-native-vector-icons/MaterialIcons';
import EditIcon from 'react-native-vector-icons/MaterialIcons';
import GalleryIcon from 'react-native-vector-icons/MaterialCommunityIcons';
import CameraIcon from 'react-native-vector-icons/FontAwesome5';

import CommonHeader from 'src/components/header';
import Layout from 'src/screens/Layout';

import FStorage from 'src/utils/FStorage';

const GroupProfileEdit = () => {
  const [groupName, setGroupName] = useState('');
  const [groupImage, setGroupImage] = useState(null);

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
      // props.onSend({
      //   image: isImage ? uploadResult : undefined,
      //   video: isImage ? undefined : uploadResult,
      // });
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

    if (!result.didCancel && result.errorMessage == null) {
      const isImage = (result.assets?.[0].type?.indexOf('image') || 0) >= 0;
      const uploadResult = await (isImage
        ? FStorage.uploadImage
        : FStorage.uploadVideo
      ).bind(FStorage)(result.assets?.[0].uri || '');
      // props.onSend({
      //   image: isImage ? uploadResult : undefined,
      //   video: isImage ? undefined : uploadResult,
      // });
    }
  };

  const handleSave = () => {
    console.log('Group Name:', groupName);
    console.log('Group Image:', groupImage);
    // Add logic to save group details
  };

  const options = [
    {label: 'camera', onPress: onCamera},
    {label: 'gallery', onPress: onPickImage},
  ];

  const getIcon = (label: string) => {
    switch (label) {
      case 'gallery':
        return <GalleryIcon name="image" size={24} color="#2C2C2C" />;
      case 'camera':
        return <CameraIcon name="camera" size={21} color="#000" />;
      default:
        return null;
    }
  };
  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader headerName="Rename group" iconExist={false} />

        <View style={styles.groupInfo}>
          <TouchableOpacity style={styles.imageContainer}>
            <View style={styles.groupIconWrapper}>
              <GroupIcon name="people-outline" size={72} color="#000" />
            </View>
            <TouchableOpacity style={styles.editIcon}>
              <EditIcon name="mode-edit-outline" size={24} color="#FFF" />
            </TouchableOpacity>
          </TouchableOpacity>
          <Text style={styles.groupName}>Member group</Text>
          <TextInput
            style={styles.input}
            placeholder="Enter group name"
            placeholderTextColor="#aaa"
            value={groupName}
            onChangeText={setGroupName}
          />

          <View style={styles.optionsGrid}>
            {options.map((option, index) => (
              <TouchableOpacity key={index} style={styles.optionWrapper}>
                <View style={styles.optionButton}>{getIcon(option.label)}</View>
                <Text style={styles.optionText}>{option.label}</Text>
              </TouchableOpacity>
            ))}
          </View>
        </View>

        <View style={styles.saveButtonContainer}>
          <TouchableOpacity style={styles.saveButton} onPress={handleSave}>
            <Text style={styles.saveButtonText}>Save</Text>
          </TouchableOpacity>
        </View>
      </View>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  groupInfo: {
    flexGrow: 1,
    alignItems: 'center',
    marginVertical: 30,
  },
  imageContainer: {
    position: 'relative',
    marginBottom: 30,
  },
  groupIconWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    width: 148,
    height: 148,
    borderRadius: 32,
    backgroundColor: '#05FCFC',
  },
  editIcon: {
    position: 'absolute',
    alignItems: 'center',
    justifyContent: 'center',
    bottom: 0,
    right: -25,
    width: 58,
    height: 58,
    borderRadius: 15,
    backgroundColor: '#2F4156',
    padding: 5,
  },
  groupName: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 18,
    textAlign: 'center',
  },
  inputWrapper: {
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 10,
  },
  input: {
    alignItems: 'center',
    justifyContent: 'center',
    width: '80%',
    backgroundColor: '#FFF',
    borderRadius: 10,
    padding: 5,
    color: '#000',
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
  },
  optionsGrid: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    flexWrap: 'wrap',
    columnGap: 50,
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
    width: 32,
    height: 32,
    borderRadius: 16,
    backgroundColor: '#05FCFC',
    marginTop: 20,
  },
  optionText: {
    fontFamily: 'Poppins-Regular',
    fontSize: 15,
    color: '#FFF',
    textAlign: 'center',
  },
  saveButtonContainer: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  saveButton: {
    backgroundColor: '#05FCFC',
    borderRadius: 32,
    padding: 15,
    alignItems: 'center',
    width: '80%',
  },
  saveButtonText: {
    fontFamily: 'Poppins-Bold',
    fontSize: 16,
    color: '#131212',
    textAlign: 'center',
  },
});

export default GroupProfileEdit;
