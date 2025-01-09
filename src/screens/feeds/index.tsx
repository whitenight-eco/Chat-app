import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  Image,
  FlatList,
  TouchableOpacity,
} from 'react-native';
import Icon from 'react-native-vector-icons/Ionicons';

import Layout from 'src/screens/Layout';
import CommonHeader from 'src/components/header';

const posts = [
  {
    id: '1',
    user: {
      name: 'Salina Hayek',
      profileImage: 'https://via.placeholder.com/50',
    },
    timestamp: 'Mar 15 12:15',
    images: [
      'https://via.placeholder.com/100',
      'https://via.placeholder.com/100',
      'https://via.placeholder.com/100',
    ],
    text: '',
    commentsCount: 13,
    likesCount: 200,
  },
  {
    id: '2',
    user: {
      name: 'Gen Fred',
      profileImage: 'https://via.placeholder.com/50',
    },
    timestamp: 'Mar 15 12:15',
    images: [],
    text: 'If I try to say the joke to myself, making the word mean the three different things at the same time, it is like hearing three different pieces of music at the same time which is uncomfortable and confusing and not nice like white noise. It is like three people trying to talk to you at the same time about different things.',
    commentsCount: 13,
    likesCount: 200,
  },
];

const FeedsScreen = () => {
  const PostItem = ({item}: {item: (typeof posts)[0]}) => (
    <View style={styles.postContainer}>
      {/* Post Header */}
      <View style={styles.postHeader}>
        <Image
          source={{uri: item.user.profileImage}}
          style={styles.profileImage}
        />
        <View>
          <Text style={styles.userName}>{item.user.name}</Text>
          <Text style={styles.timestamp}>{item.timestamp}</Text>
        </View>
      </View>

      {/* Post Content */}
      <View style={styles.postWrapper}>
        <Text style={styles.postText}>{item.text}</Text>
        <View style={styles.imageGrid}>
          {item.images?.map((image, index) => (
            <Image key={index} source={{uri: image}} style={styles.postImage} />
          ))}
        </View>
      </View>

      {/* Post Actions */}
      <View style={styles.postActions}>
        <TouchableOpacity style={styles.actionButton}>
          <Text style={styles.actionText}>{item.commentsCount} Comment</Text>
        </TouchableOpacity>
        <TouchableOpacity style={styles.actionButton}>
          <Icon name="heart-outline" size={20} color="red" />
          <Text style={styles.actionText}>{item.likesCount} Likes</Text>
        </TouchableOpacity>
      </View>
    </View>
  );
  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader headerName="Feeds" iconExist={true} />

        {/* Feeds */}
        <FlatList
          data={posts}
          keyExtractor={item => item.id}
          renderItem={({item}) => <PostItem item={item} />}
          contentContainerStyle={styles.feedList}
        />
      </View>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    flexDirection: 'column',
  },
  feedList: {
    marginTop: 20,
  },
  postContainer: {
    marginBottom: 20,
    backgroundColor: 'transparent',
  },
  postHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 10,
  },
  profileImage: {
    width: 40,
    height: 40,
    borderRadius: 20,
    marginRight: 10,
  },
  userName: {
    color: '#FFF',
    fontFamily: 'Poppins-SemiBold',
    fontSize: 12,
  },
  timestamp: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 10,
  },
  postWrapper: {
    flex: 1,
  },
  postText: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
    paddingLeft: 50,
  },
  imageGrid: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  postImage: {
    width: 100,
    height: 100,
    borderRadius: 10,
  },
  postActions: {
    flexDirection: 'row',
    justifyContent: 'flex-end',
    gap: 10,
    marginTop: 10,
  },
  actionButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 5,
  },
  actionText: {
    color: '#94A3B8',
    fontFamily: 'Poppins-Regular',
    fontSize: 10,
  },
});

export default FeedsScreen;
