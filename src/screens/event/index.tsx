import React, {useState} from 'react';
import {View, TextInput, TouchableOpacity, StyleSheet} from 'react-native';

import ArrowRightIcon from 'react-native-vector-icons/Feather';

import CommonHeader from 'src/components/header';
import Layout from 'src/screens/Layout';

const Event = () => {
  const [eventName, setEventName] = useState<string>('');
  const [description, setDescription] = useState<string>('');
  const [venue, setVenue] = useState<string>('');
  const [date, setDate] = useState<string>('');
  const [time, setTime] = useState<string>('');

  const handleSubmit = () => {
    console.log({eventName, description, venue, date, time});
    // Add form submission logic here
  };

  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader headerName="Event" iconExist={false} />

        <View style={styles.inputContainer}>
          <TextInput
            style={styles.input}
            placeholder="Event name"
            placeholderTextColor="#A9A9A9"
            value={eventName}
            onChangeText={text => setEventName(text)}
          />
          <TextInput
            style={styles.input}
            placeholder="Description"
            placeholderTextColor="#A9A9A9"
            value={description}
            onChangeText={text => setDescription(text)}
          />
          <View style={styles.datetimeWrapper}>
            <TextInput
              style={styles.inputDateTime}
              placeholder="Date"
              placeholderTextColor="#FFF"
              value={date}
              onChangeText={text => setDate(text)}
            />
            <TextInput
              style={styles.inputDateTime}
              placeholder="Time"
              placeholderTextColor="#FFF"
              value={time}
              onChangeText={text => setTime(text)}
            />
          </View>
          <TextInput
            style={styles.input}
            placeholder="Venue"
            placeholderTextColor="#A9A9A9"
            value={venue}
            onChangeText={text => setVenue(text)}
          />
        </View>
        <TouchableOpacity style={styles.submitButton} onPress={handleSubmit}>
          <ArrowRightIcon name="arrow-right" size={35} color="#000" />
        </TouchableOpacity>
      </View>
    </Layout>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  inputContainer: {
    marginTop: 50,
  },
  input: {
    backgroundColor: '#FFF',
    color: '#000',
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
    padding: 10,
    borderRadius: 10,
    marginBottom: 20,
  },
  datetimeWrapper: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  inputDateTime: {
    backgroundColor: '#2C2C2C',
    color: '#000',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    padding: 8,
    width: '45%',
    borderRadius: 8,
    marginBottom: 20,
  },
  submitButton: {
    position: 'absolute',
    bottom: 0,
    right: 0,
    alignItems: 'center',
    justifyContent: 'center',
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: '#05FCFC',
  },
});

export default Event;
