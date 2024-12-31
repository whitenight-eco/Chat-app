import React from 'react';
import {View, Text, StyleSheet, TouchableOpacity} from 'react-native';
import ThreeDotIcon from 'react-native-vector-icons/Entypo';

const FeedsHeader = () => {
  return (
    <View style={styles.header}>
      <Text style={styles.headerTitle}>Feeds</Text>
      <TouchableOpacity>
        <ThreeDotIcon name="dots-three-vertical" size={20} color="#FFF" />
      </TouchableOpacity>
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
});

export default FeedsHeader;
