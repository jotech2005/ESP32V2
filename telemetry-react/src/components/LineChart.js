import React from 'react';
import { View, StyleSheet, useWindowDimensions } from 'react-native';
import Svg, { Path, Line, G, Text as SvgText } from 'react-native-svg';
import { colors } from '../theme/colors';
import { Text } from './Text';

const CHART_HEIGHT = 220;
const PADDING = 18;

function buildPath(values, width) {
  if (!values.length) return { d: '' };
  const max = Math.max(...values.map(v => v.value));
  const min = Math.min(...values.map(v => v.value));
  const span = max - min || 1;
  const count = values.length;
  const chartWidth = width - 32 - PADDING * 2;
  const stepX = count <= 1 ? 0 : chartWidth / (count - 1);

  let d = '';
  values.forEach((pt, idx) => {
    const x = PADDING + stepX * idx;
    const yRatio = (pt.value - min) / span;
    const y = PADDING + (1 - yRatio) * (CHART_HEIGHT - PADDING * 2);
    d += `${idx === 0 ? 'M' : 'L'}${x.toFixed(1)},${y.toFixed(1)} `;
  });
  return { d, min, max };
}

function smoothValues(values, windowSize = 3) {
  if (!values.length || windowSize <= 1) return values;
  const half = Math.floor(windowSize / 2);
  return values.map((pt, idx) => {
    let sum = 0;
    let count = 0;
    for (let i = idx - half; i <= idx + half; i += 1) {
      if (i >= 0 && i < values.length) {
        sum += values[i].value;
        count += 1;
      }
    }
    return { ...pt, value: count ? sum / count : pt.value };
  });
}

export default function LineChart({ tempPoints = [], humPoints = [], smooth = false }) {
  const { width: screenWidth } = useWindowDimensions();
  const width = screenWidth - 32;
  const tempSeries = smooth ? smoothValues(tempPoints, 3) : tempPoints;
  const humSeries = smooth ? smoothValues(humPoints, 3) : humPoints;
  const tempPath = buildPath(tempSeries, width);
  const humPath = buildPath(humSeries, width);

  const yLabels = 4;
  return (
    <View style={styles.wrapper}>
      <Svg height={CHART_HEIGHT} width={width}>
        <G>
          {Array.from({ length: yLabels }).map((_, i) => {
            const y = PADDING + ((CHART_HEIGHT - PADDING * 2) / (yLabels - 1)) * i;
            return (
              <Line
                key={i}
                x1={PADDING}
                x2={width - PADDING}
                y1={y}
                y2={y}
                stroke={colors.border}
                strokeDasharray="4 6"
                strokeWidth={1}
              />
            );
          })}
        </G>
        {humPath.d ? (
          <Path d={humPath.d} stroke={colors.secondary || '#A855F7'} strokeWidth={3} fill="none" strokeLinecap="round" />
        ) : null}
        {tempPath.d ? (
          <Path d={tempPath.d} stroke={colors.accent} strokeWidth={3} fill="none" strokeLinecap="round" />
        ) : null}

        <G>
          {humPath.max !== undefined && humPath.min !== undefined ? (
            <SvgText x={PADDING} y={PADDING + 10} fill={colors.muted} fontSize="10">
              Hum {humPath.max.toFixed(1)}
            </SvgText>
          ) : null}
          {humPath.max !== undefined && humPath.min !== undefined ? (
            <SvgText x={PADDING} y={CHART_HEIGHT - PADDING + 10} fill={colors.muted} fontSize="10">
              Hum {humPath.min.toFixed(1)}
            </SvgText>
          ) : null}
          {tempPath.max !== undefined && tempPath.min !== undefined ? (
            <SvgText x={width - PADDING * 4} y={PADDING + 10} fill={colors.muted} fontSize="10">
              Temp {tempPath.max.toFixed(1)}
            </SvgText>
          ) : null}
          {tempPath.max !== undefined && tempPath.min !== undefined ? (
            <SvgText x={width - PADDING * 4} y={CHART_HEIGHT - PADDING + 10} fill={colors.muted} fontSize="10">
              Temp {tempPath.min.toFixed(1)}
            </SvgText>
          ) : null}
        </G>
      </Svg>
      <View style={styles.legendRow}>
        <View style={styles.legendItem}>
          <View style={[styles.dot, { backgroundColor: colors.accent }]} />
          <Text style={styles.legendText}>Temperatura</Text>
        </View>
        <View style={styles.legendItem}>
          <View style={[styles.dot, { backgroundColor: colors.secondary || '#A855F7' }]} />
          <Text style={styles.legendText}>Humedad</Text>
        </View>
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  wrapper: {
    width: '100%',
    backgroundColor: colors.surface,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
    paddingVertical: 10,
    paddingHorizontal: 10,
    shadowColor: colors.shadow,
    shadowOpacity: 0.25,
    shadowRadius: 10,
    shadowOffset: { width: 0, height: 6 },
    elevation: 3,
  },
  legendRow: {
    marginTop: 6,
    flexDirection: 'row',
    gap: 16,
    alignItems: 'center',
    justifyContent: 'center',
  },
  legendItem: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  dot: {
    width: 8,
    height: 8,
    borderRadius: 999,
  },
  legendText: {
    color: colors.muted,
    fontSize: 12,
  },
});
