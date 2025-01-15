import React, {useState} from 'react';
import {
  FlatList,
  Image,
  Text,
  TouchableOpacity,
  View,
  StyleSheet,
} from 'react-native';
import moment from 'moment';
import {IMessage, IMessageDetail} from 'src/types';
import ProfileStore from 'src/store/ProfileStore';
import ImageView from 'react-native-image-viewing';
import Video from 'react-native-video';

interface IProps {
  messages: IMessageDetail[];
}

const ChatList = (props: IProps) => {
  const [visible, setIsVisible] = useState(false);
  const [images, setImages] = useState<any>([]);

  const user = ProfileStore.user;

  const isMine = (item: IMessageDetail) => item.user.id === user?.id;

  const dynamicDisplayTime = (date: number) => {
    const messageDate = moment(new Date(date));
    const now = moment();

    if (now.diff(messageDate, 'days') < 1) {
      // Within 1 day
      return messageDate.format('H:mm');
    } else if (now.diff(messageDate, 'years') < 1) {
      // Within 1 year
      return messageDate.format('D MMM H:mm');
    } else {
      // Over 1 year
      return messageDate.format('YYYY D MMM H:mm');
    }
  };

  const onShowImage = (uri: string) => {
    setImages([{uri}]);
    setIsVisible(true);
  };

  const shouldShowTime = (currentMessage: IMessageDetail, index: number) => {
    if (index === 0) return true; // Show time for the first message
    const previousMessage = props.messages[index - 1];
    if (!previousMessage) return true;

    const currentTime = moment(currentMessage.createdAt || 0);
    const previousTime = moment(previousMessage.createdAt || 0);
    return currentTime.diff(previousTime, 'seconds') > 60;
  };

  return (
    <View style={styles.container}>
      <FlatList
        data={props.messages}
        keyExtractor={(item, index) => index.toString()}
        renderItem={({item, index}) => (
          <>
            {shouldShowTime(item, index) && (
              <Text style={styles.dateText}>
                {dynamicDisplayTime(item.createdAt || 0)}
              </Text>
            )}
            <View
              style={[
                styles.itemContainer,
                isMine(item) ? styles.itemRight : styles.itemLeft,
              ]}>
              {item.text ? (
                <>
                  <View
                    style={
                      isMine(item)
                        ? styles.textContainerMine
                        : styles.textContainerOther
                    }>
                    <Text style={styles.normalText}>{item.text}</Text>
                  </View>
                </>
              ) : item.image ? (
                <TouchableOpacity
                  onPress={() => onShowImage(item.image || '')}
                  style={styles.imageCard}>
                  <Image source={{uri: item.image}} style={styles.preview} />
                </TouchableOpacity>
              ) : item.video ? (
                <View style={styles.videoCard}>
                  <Video
                    source={{uri: item.video}}
                    controls
                    resizeMode="cover"
                    style={StyleSheet.absoluteFill}
                    paused
                  />
                </View>
              ) : null}
            </View>
          </>
        )}
        // inverted
      />
      <ImageView
        images={images}
        imageIndex={0}
        visible={visible}
        onRequestClose={() => setIsVisible(false)}
      />
    </View>
  );
};

export default ChatList;

const styles = StyleSheet.create({
  container: {
    flex: 1,
    marginBottom: 20,
  },
  itemContainer: {
    flexDirection: 'row',
    marginVertical: 5,
  },
  dateText: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 13,
    textAlign: 'center',
    letterSpacing: 1,
    marginBottom: 10,
  },
  itemRight: {
    justifyContent: 'flex-end',
  },
  itemLeft: {
    justifyContent: 'flex-start',
  },
  textContainerMine: {
    paddingHorizontal: 15,
    paddingVertical: 10,
    borderRadius: 20,
    backgroundColor: '#41545C',
    maxWidth: '50%',
  },
  textContainerOther: {
    paddingHorizontal: 15,
    paddingVertical: 10,
    borderRadius: 20,
    backgroundColor: '#424242',
    maxWidth: '50%',
  },
  normalText: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 13,
  },
  imageCard: {
    width: 120,
    height: 100,
  },
  preview: {
    flex: 1,
    borderRadius: 10,
    resizeMode: 'cover',
  },
  videoCard: {
    width: 180,
    height: 120,
    borderRadius: 10,
    overflow: 'hidden',
    backgroundColor: 'black',
  },
});
