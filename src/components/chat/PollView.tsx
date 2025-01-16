import React, {useEffect, useState} from 'react';
import {Text, View, StyleSheet, TouchableOpacity} from 'react-native';

const staticOptions = [
  {name: 'Donal Trump', count: 12},
  {name: 'Biden', count: 5},
];

const PollView = () => {
  const [selectedOption, setSelectedOption] = useState();
  const [totalCount, setTotalCount] = useState(0);
  const [options, setOptions] = useState(staticOptions);

  const handlePress = (newIndex: any) => {
    const updatedOptions = options.map((option, idx) => {
      if (idx === selectedOption) {
        // Decrease count for the previously selected option
        return {...option, count: option.count - 1};
      } else if (idx === newIndex) {
        // Increase count for the newly selected option
        return {...option, count: option.count + 1};
      }
      return option;
    });

    setOptions(updatedOptions);
    setSelectedOption(newIndex);
  };

  useEffect(() => {
    const totalCount = options.reduce((total, num) => total + num.count, 0);
    setTotalCount(totalCount);
  }, [setSelectedOption]);

  return (
    <View style={styles.card}>
      <Text style={styles.cardTitle}>Votes</Text>
      <Text style={styles.subTitle}>Presidency 2027</Text>

      {/* Candidate 1 */}
      {options.map((option, index) => (
        <View style={styles.radioContainer} key={index}>
          <TouchableOpacity
            style={styles.radioOption}
            onPress={() => handlePress(index)}>
            <View style={styles.radioCircle}>
              {selectedOption === index && (
                <View style={styles.radioSelected} />
              )}
            </View>
          </TouchableOpacity>

          <View style={styles.textWrapper}>
            <Text style={styles.voteText}>{option.name}</Text>
            <Text style={styles.voteCount}>{option.count}</Text>
            <View style={styles.progressBarWrapper}>
              <View
                style={[
                  styles.progressBar,
                  {width: `${(option.count / totalCount) * 100}%`},
                ]}></View>
            </View>
          </View>
        </View>
      ))}
    </View>
  );
};

export default PollView;

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#41545C',
    borderRadius: 8,
    padding: 16,
    marginBottom: 8,
  },
  cardTitle: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 13,
  },
  subTitle: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 13,
    marginBottom: 10,
  },
  radioContainer: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    gap: 20,
  },
  radioOption: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 10,
  },
  radioCircle: {
    width: 20,
    height: 20,
    borderRadius: 10,
    borderWidth: 2,
    borderColor: '#FFF',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: 'transparent',
  },
  radioSelected: {
    width: 12, // Inner circle size
    height: 12,
    borderRadius: 6,
    backgroundColor: '#05FCFC',
  },
  radioLabel: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 14,
    textAlign: 'center',
  },
  progressBarWrapper: {
    flexGrow: 1,
    backgroundColor: '#000',
    height: 8,
    borderRadius: 4,
    width: '100%',
  },
  progressBar: {
    flexGrow: 1,
    backgroundColor: '#02FFFF',
    height: 8,
    borderRadius: 4,
  },
  textWrapper: {
    flex: 1,
    flexDirection: 'column',
  },
  voteText: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 13,
  },
  voteCount: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 9,
  },
});
