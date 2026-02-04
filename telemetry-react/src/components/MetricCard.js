import React from 'react';
import { View, StyleSheet } from 'react-native';
import { Text } from './Text';
import { colors } from '../theme/colors';

export default function MetricCard({ title, value, full, tone, subtitle }) {
  const toneStyle =
    tone === 'accent'
      ? styles.cardAccent
      : tone === 'success'
        ? styles.cardSuccess
        : tone === 'warning'
          ? styles.cardWarning
          : null;
  return (
    <View style={[styles.card, toneStyle, full && styles.full]}>
      <Text style={styles.label}>{title}</Text>
      <Text style={styles.value}>{value}</Text>
      {subtitle ? <Text style={styles.subtitle}>{subtitle}</Text> : null}
    </View>
  );
}

const styles = StyleSheet.create({
  card: {
    flex: 1,
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: colors.border,
    shadowColor: colors.shadow,
    shadowOpacity: 0.3,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 6 },
    elevation: 3,
  },
  cardAccent: {
    borderColor: colors.accent,
    backgroundColor: colors.accentSoft,
  },
  cardSuccess: {
    borderColor: colors.success,
    backgroundColor: '#0B2F25',
  },
  cardWarning: {
    borderColor: colors.warning,
    backgroundColor: '#2D2408',
  },
  full: {
    width: '100%',
  },
  label: {
    color: colors.muted,
    marginBottom: 6,
  },
  value: {
    color: colors.text,
    fontSize: 18,
    fontWeight: '700',
  },
  subtitle: {
    color: colors.muted,
    marginTop: 6,
    fontSize: 12,
  },
});
