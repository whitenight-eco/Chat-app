import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
} from 'react-native';
import SearchIcon from 'react-native-vector-icons/Ionicons';
import ThreeDotIcon from 'react-native-vector-icons/Entypo';

const ChatsHeader = () => {
  return (
    <View style={styles.header}>
      <Text style={styles.headerTitle}>Chats</Text>
      <View style={styles.iconWrapper}>
        <TouchableOpacity>
          <SearchIcon name="search" size={20} color="#FFF" />
        </TouchableOpacity>
        <TouchableOpacity>
          <ThreeDotIcon name="dots-three-vertical" size={20} color="#FFF" />
        </TouchableOpacity>
      </View>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    height: '100%',
    flexDirection: 'column',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    paddingTop: 16,
    paddingBottom: 5,
    paddingHorizontal: 5,
  },
  headerTitle: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 18,
    textAlign: 'center',
  },
  iconWrapper: {
    flexDirection: 'row',
    gap: 20,
  },
});

export default ChatsHeader;
