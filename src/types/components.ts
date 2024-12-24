import {ChangeEvent, Dispatch, ReactNode, SetStateAction} from 'react';
import {
  ViewStyle,
  StyleProp,
  TextInputProps,
  NativeSyntheticEvent,
  TextInputFocusEventData,
} from 'react-native';

// Layout
export interface LayoutPropsType {
  children: ReactNode;
  style?: StyleProp<ViewStyle>;
}

export interface BaseInputType {
  onChange: (e: string | ChangeEvent<any>) => void;
  onBlur: (
    e:
      | string
      | ChangeEvent<any>
      | NativeSyntheticEvent<TextInputFocusEventData>,
  ) => void;
}

// MenuItem
export interface MenuItemPropsType {
  label: string;
  rightItem?: ReactNode;
  onPress: () => void;
}

// Card
export interface CardPropsType {
  children: ReactNode;
  style?: StyleProp<ViewStyle>;
}

// Input
export interface InputPropsType extends TextInputProps {
  testID?: string;
  style?: ViewStyle;
  error?: string;
  iconName: string;
}

// DropDown
export interface DropDownType extends BaseInputType {
  placeholder: string;
  name: string;
  navigation?: any;
  data?: any;
  list: Array<any>;
  value: string;
  search?: boolean;
  error: string;
}

// AutocompleteNScanInput
export interface AutocompleteNScanInputType extends BaseInputType {
  value: string;
  data: Array<any>;
  error: string;
  screenName?: string;
  enableButton?: boolean;
  isEditable?: boolean;
  fieldName?: string;
  navigation?: any;
  style?: ViewStyle;
  index?: number;
  placeholder?: string;
  label?: boolean;
  name?: string;
  displayName?: string;
  handleId: Dispatch<SetStateAction<string>>;
}

// UOMField
export interface UOMFieldType extends AutocompleteNScanInputType {
  groupName: string;
  wt_value: string;
  wt_displayName?: string;
  wt_data: Array<any>;
  wt_onChange: (e: string | ChangeEvent<any>) => void;
  wt_onBlur: (
    e:
      | string
      | ChangeEvent<any>
      | NativeSyntheticEvent<TextInputFocusEventData>,
  ) => void;
  wt_error: string;
}

export interface TSwitchItem {
  onLabel: string;
  offLabel: string;
  isSwitchOn: boolean;
  onSwitch: (e: boolean | ChangeEvent<any>) => void;
}
