import React from 'react';
import { Text as RNText } from 'react-native';
import { colors } from '../theme/colors';

export function Text({ style, ...rest }) {
  return (
    <RNText
      allowFontScaling
      maxFontSizeMultiplier={1.2}
      style={[{ color: colors.text }, style]}
      {...rest}
    />
  );
}
