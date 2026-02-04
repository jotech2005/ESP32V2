import React from 'react';
import { ActivityIndicator, FlatList, StyleSheet, View } from 'react-native';
import { Text } from './Text';
import { colors } from '../theme/colors';

function Item({ item }) {
  const luzLabel = item?.luz === 1 ? 'Encendida' : item?.luz === 0 ? 'Apagada' : '—';
  const tempLabel = item?.temp != null ? `${item.temp.toFixed(1)} °C` : '—';
  const humLabel = item?.hum != null ? `${item.hum.toFixed(1)} %` : '—';
  return (
    <View style={styles.item}>
      <View style={styles.itemHeader}>
        <Text style={styles.itemDate}>{item.formattedDate || item.datetime || '—'}</Text>
        <View style={styles.uidPill}>
          <Text style={styles.uidText}>{item.uid || '—'}</Text>
        </View>
      </View>
      <View style={styles.row}>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>Luz: {luzLabel}</Text>
        </View>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>T: {tempLabel}</Text>
        </View>
        <View style={styles.badge}>
          <Text style={styles.badgeText}>H: {humLabel}</Text>
        </View>
      </View>
    </View>
  );
}

export default function HistoryList({ data, loading }) {
  if (loading && (!data || data.length === 0)) {
    return <ActivityIndicator color={colors.accent} />;
  }
  if (!loading && (!data || data.length === 0)) {
    return <Text style={styles.empty}>Sin datos</Text>;
  }
  return (
    <FlatList
      data={data}
      keyExtractor={(item, idx) => `${item.datetime}-${item.uid}-${idx}`}
      renderItem={({ item }) => <Item item={item} />}
      contentContainerStyle={{ gap: 10 }}
      scrollEnabled={false}
    />
  );
}

const styles = StyleSheet.create({
  item: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    padding: 12,
    borderWidth: 1,
    borderColor: colors.border,
    shadowColor: colors.shadow,
    shadowOpacity: 0.25,
    shadowRadius: 8,
    shadowOffset: { width: 0, height: 4 },
    elevation: 2,
  },
  itemHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 6,
  },
  itemDate: {
    color: colors.muted,
  },
  uidPill: {
    backgroundColor: colors.surfaceAlt,
    borderColor: colors.border,
    borderWidth: 1,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 999,
  },
  uidText: {
    color: colors.text,
    fontWeight: '700',
    fontSize: 12,
  },
  row: {
    flexDirection: 'row',
    gap: 8,
    flexWrap: 'wrap',
  },
  badge: {
    backgroundColor: colors.surfaceAlt,
    borderColor: colors.border,
    borderWidth: 1,
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 999,
  },
  badgeText: {
    color: colors.text,
    fontSize: 12,
  },
  empty: {
    color: colors.muted,
  },
});
