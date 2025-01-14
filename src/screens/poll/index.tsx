import React, {useState} from 'react';
import {
  View,
  Text,
  TextInput,
  Switch,
  TouchableOpacity,
  FlatList,
  StyleSheet,
} from 'react-native';

import AddIcon from 'react-native-vector-icons/Ionicons';
import ArrowRightIcon from 'react-native-vector-icons/Feather';

import CommonHeader from 'src/components/header';
import Layout from 'src/screens/Layout';

const Poll = () => {
  const [question, setQuestion] = useState('');
  const [options, setOptions] = useState<string[]>(['', '']);
  const [everyoneRespond, setEveryoneRespond] = useState(true);
  const MAX_HEIGHT = 200;

  const addOption = () => {
    setOptions([...options, '']);
  };

  const handleOptionChange = (text: string, index: number) => {
    const newOptions = [...options];
    newOptions[index] = text;
    setOptions(newOptions);
  };

  const handleSubmit = () => {
    console.log({question, options, everyoneRespond});
    // Add form submission logic here
  };

  return (
    <Layout>
      <View style={styles.container}>
        <CommonHeader headerName="Poll" iconExist={false} />

        <TextInput
          style={styles.input}
          placeholder="Ask question"
          placeholderTextColor="#A9A9A9"
          value={question}
          onChangeText={setQuestion}
          multiline={true}
        />
        <FlatList
          data={options}
          keyExtractor={(item, index) => index.toString()}
          renderItem={({item, index}) => (
            <TextInput
              style={styles.inputOption}
              placeholder={`Option ${index + 1}`}
              placeholderTextColor="#A9A9A9"
              value={item}
              onChangeText={text => handleOptionChange(text, index)}
            />
          )}
          style={{maxHeight: MAX_HEIGHT}}
        />
        <TouchableOpacity style={styles.addButton} onPress={addOption}>
          <AddIcon name="add" size={24} color="#05FCFC" />
          <Text style={styles.addButtonText}>Add option</Text>
        </TouchableOpacity>

        <View style={styles.toggleContainer}>
          <Text style={styles.toggleText}>
            Everyone in the group to respond
          </Text>
          <Switch
            value={everyoneRespond}
            onValueChange={setEveryoneRespond}
            trackColor={{true: '#05FCFC', false: '#444'}}
            thumbColor={everyoneRespond ? '#05FCFC' : '#ccc'}
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
  input: {
    backgroundColor: '#FFF',
    color: '#000',
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
    padding: 10,
    height: 110,
    borderRadius: 10,
    marginTop: 50,
    marginBottom: 20,
  },
  inputOption: {
    backgroundColor: '#FFF',
    color: '#000',
    fontFamily: 'Poppins-Regular',
    fontSize: 12,
    padding: 12,
    borderRadius: 10,
    marginBottom: 20,
  },
  addButton: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
    marginBottom: 20,
  },
  addButtonText: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 14,
    textAlign: 'center',
  },
  toggleContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    marginBottom: 20,
  },
  toggleText: {
    color: '#FFF',
    fontFamily: 'Poppins-Medium',
    fontSize: 14,
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

export default Poll;
