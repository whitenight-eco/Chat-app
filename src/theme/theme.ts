// Types
interface spacingType {
  borderRadius: number;
  layoutPaddingH: number;
  containerPaddingV: number;
  cardMarginB: number;
}

interface typeSizesType {
  FONT_SIZE_SMALL: number;
  FONT_SIZE_MEDIUM: number;
  FONT_SIZE_LARGE: number;
  FONT_WEIGHT_LIGHT: number;
  FONT_WEIGHT_MEDIUM: number;
  FONT_WEIGHT_HEAVY: number;
}

export interface themeType {
  name: string;
  text: string,
  color: string;
  primary: string;
  layoutBg: string;
  cardBg: string;
  cardBorderColor: string;
  accent: string;
  error: string;
  buttonColor: {
    primary: string,
    primaryInactive: string,
    secondary: string,
  }
}

interface themesType {
  light: themeType;
  dark: themeType;
}

// Spacing:- Common margins and paddings
const spacing: spacingType = {
  borderRadius: 16,
  layoutPaddingH: 16,
  containerPaddingV: 16,
  cardMarginB: 16,
};

// Type Sizes:- Font sizes and weights
const typeSizes: typeSizesType = {
  FONT_SIZE_SMALL: 12,
  FONT_SIZE_MEDIUM: 14,
  FONT_SIZE_LARGE: 16,
  FONT_WEIGHT_LIGHT: 200,
  FONT_WEIGHT_MEDIUM: 600,
  FONT_WEIGHT_HEAVY: 800,
};

// Themes:- Can alter values here. Can only be consumed through Context (see useTheme.js file)
const themes: themesType = {
  light: {
    name: 'light',
    text: '#695D5D',
    color: '#695D5D',
    primary: '#EF936E',
    layoutBg: '#F4F3F1',
    cardBg: '#FFF',
    cardBorderColor: '#EEECEC',
    accent: '#0071FF',
    error: '#B00020',
    buttonColor: {
      primary: '#800080',
      primaryInactive: '#e000e0',
      secondary: 'blue'
    }
  },
  dark: {
    name: 'dark',
    text: '#695D5D',
    color: '#FFF', 
    primary: '#EF936E',
    layoutBg: '#000',
    cardBg: '#000',
    cardBorderColor: '#000',
    accent: '#0071FF',
    error: '#B00020',
    buttonColor: {
      primary: '#FFF',
      primaryInactive: '#EDA182',
      secondary: 'pink'
    }
  },
};

export {spacing, typeSizes, themes};
