import React, {useState, useEffect} from 'react';
import {Text, TouchableOpacity, View, StyleSheet, Image} from 'react-native';

const staticOptions = [
  {name: 'Going', selected: true},
  {name: 'Not going', selected: false},
];

const EventView = () => {
  const [selectedOption, setSelectedOption] = useState();
  const [options, setOptions] = useState(staticOptions);
  const [totalCount, setTotalCount] = useState(2);

  const handlePress = (newIndex: any) => {
    const updatedOptions = options.map((option, idx) => {
      if (idx === selectedOption) {
        setTotalCount(totalCount + 1);
        return {...option, selected: true};
      } else if (idx === newIndex) {
        setTotalCount(totalCount - 1);
        return {...option, selected: false};
      }
      return option;
    });

    setOptions(updatedOptions);
    setSelectedOption(newIndex);
  };

  return (
    <View style={styles.card}>
      <Text style={styles.cardTitle}>Event</Text>
      <Text style={styles.subTitle}>Fishing at Lake Toba</Text>
      <Text style={styles.eventDate}>09/10/2024, 8:00am</Text>

      {/* Participants */}
      <View style={styles.participants}>
        <Image
          source={{uri: 'https://via.placeholder.com/150'}}
          style={styles.avatar}
        />
        <Image
          source={{uri: 'https://via.placeholder.com/150'}}
          style={styles.avatar}
        />

        <Text style={styles.participantsText}>{totalCount} going</Text>
      </View>

      <View style={styles.radioContainer}>
        {options.map((option, index) => (
          <TouchableOpacity
            style={styles.radioOption}
            key={index}
            onPress={() => handlePress(index)}>
            <View style={styles.radioCircle}>
              {selectedOption === index && (
                <View style={styles.radioSelected} />
              )}
            </View>
            <Text style={styles.radioLabel}>{option.name}</Text>
          </TouchableOpacity>
        ))}
      </View>
    </View>
  );
};

export default EventView;

const styles = StyleSheet.create({
  card: {
    backgroundColor: '#41545C',
    borderRadius: 8,
    padding: 16,
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
  },
  eventDate: {
    color: '#FFF',
    fontFamily: 'Poppins-Regular',
    fontSize: 13,
  },
  avatar: {
    width: 40,
    height: 40,
    borderRadius: 20,
  },
  participants: {
    flexDirection: 'row',
    alignItems: 'center',
    borderBottomWidth: 1,
    borderColor: '#FFF',
    marginBottom: 16,
  },
  participantsText: {
    color: '#FFF',
    fontSize: 14,
  },
  radioContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
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
});
