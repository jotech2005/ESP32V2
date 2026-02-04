import React, { useEffect, useMemo, useRef, useState } from 'react';
import { ActivityIndicator, Animated, Pressable, RefreshControl, ScrollView, StatusBar, StyleSheet, TextInput, View } from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { useTelemetry } from './src/hooks/useTelemetry';
import MetricCard from './src/components/MetricCard';
import HistoryList from './src/components/HistoryList';
import LineChart from './src/components/LineChart';
import { colors } from './src/theme/colors';
import { Text } from './src/components/Text';
import { api } from './src/api/client';

export default function App() {
  const {
    latest,
    history,
    loading,
    refreshing,
    error,
    lastUpdated,
    refreshAll,
    refreshHistory,
    createTelemetry,
    replaceTelemetry,
    patchTelemetry,
    deleteTelemetry,
  } = useTelemetry();
  const [limit, setLimit] = useState(50);
  const [query, setQuery] = useState('');
  const [formId, setFormId] = useState('');
  const [formDatetime, setFormDatetime] = useState('');
  const [formUid, setFormUid] = useState('');
  const [formLuz, setFormLuz] = useState('');
  const [formTemp, setFormTemp] = useState('');
  const [formHum, setFormHum] = useState('');
  const [actionMessage, setActionMessage] = useState('');
  const [formError, setFormError] = useState('');
  const [actionLoading, setActionLoading] = useState(false);
  const [actionType, setActionType] = useState(null);
  const [showIdHint, setShowIdHint] = useState(false);
  const [range, setRange] = useState('24h');
  const [smooth, setSmooth] = useState(true);
  const [demoLoading, setDemoLoading] = useState(false);
  const pulseAnim = useRef(new Animated.Value(0)).current;

  const handleRefreshAll = () => refreshAll({ limit });

  const handleLimitChange = (value) => {
    if (value === limit) return;
    setLimit(value);
    refreshHistory({ limit: value, sort: 'desc' });
  };

  const filteredHistory = useMemo(() => {
    const q = query.trim().toLowerCase();
    if (!q) return history;
    return history.filter(item => String(item.uid ?? '').toLowerCase().includes(q));
  }, [history, query]);

  const rangeFilteredHistory = useMemo(() => {
    if (range === 'all') return filteredHistory;
    const now = Date.now();
    const rangeMs = range === '24h' ? 24 * 60 * 60 * 1000 : range === '7d' ? 7 * 24 * 60 * 60 * 1000 : 30 * 24 * 60 * 60 * 1000;
    return filteredHistory.filter(item => (item?._date ?? 0) >= now - rangeMs);
  }, [filteredHistory, range]);

  const statsSource = useMemo(() => rangeFilteredHistory, [rangeFilteredHistory]);

  const parseOptionalNumber = (value) => {
    if (value === '' || value === null || value === undefined) return null;
    const num = Number(value);
    return Number.isFinite(num) ? num : null;
  };

  const buildPayload = (allowNulls = false) => {
    const payload = {};
    if (formDatetime) payload.datetime = formDatetime;
    else if (allowNulls) payload.datetime = null;

    if (formUid) payload.uid = formUid;
    else if (allowNulls) payload.uid = null;

    const luz = parseOptionalNumber(formLuz);
    if (luz !== null) payload.luz = luz;
    else if (allowNulls) payload.luz = null;

    const temp = parseOptionalNumber(formTemp);
    if (temp !== null) payload.temp = temp;
    else if (allowNulls) payload.temp = null;

    const hum = parseOptionalNumber(formHum);
    if (hum !== null) payload.hum = hum;
    else if (allowNulls) payload.hum = null;

    return payload;
  };

  const handleCreate = async () => {
    setActionMessage('');
    setFormError('');
    setActionLoading(true);
    setActionType('create');
    try {
      const res = await createTelemetry(buildPayload(false), { limit, sort: 'desc' });
      setActionMessage(`Creado ID: ${res?.id ?? 'ok'}`);
    } catch (err) {
      setActionMessage(err.message || 'Error creando');
    } finally {
      setActionLoading(false);
      setActionType(null);
    }
  };

  const handleReplace = async () => {
    if (!formId) {
      setFormError('ID requerido para reemplazar');
      setActionMessage('');
      setShowIdHint(true);
      return;
    }
    setActionMessage('');
    setFormError('');
    setShowIdHint(false);
    setActionLoading(true);
    setActionType('replace');
    try {
      await replaceTelemetry(formId, buildPayload(true), { limit, sort: 'desc' });
      setActionMessage(`Reemplazado ID: ${formId}`);
    } catch (err) {
      setActionMessage(err.message || 'Error reemplazando');
    } finally {
      setActionLoading(false);
      setActionType(null);
    }
  };

  const handlePatch = async () => {
    if (!formId) {
      setFormError('ID requerido para modificar');
      setActionMessage('');
      setShowIdHint(true);
      return;
    }
    setActionMessage('');
    setFormError('');
    setShowIdHint(false);
    setActionLoading(true);
    setActionType('patch');
    try {
      await patchTelemetry(formId, buildPayload(false), { limit, sort: 'desc' });
      setActionMessage(`Modificado ID: ${formId}`);
    } catch (err) {
      setActionMessage(err.message || 'Error modificando');
    } finally {
      setActionLoading(false);
      setActionType(null);
    }
  };

  const handleDelete = async () => {
    if (!formId) {
      setFormError('ID requerido para borrar');
      setActionMessage('');
      setShowIdHint(true);
      return;
    }
    setActionMessage('');
    setFormError('');
    setShowIdHint(false);
    setActionLoading(true);
    setActionType('delete');
    try {
      await deleteTelemetry(formId, { limit, sort: 'desc' });
      setActionMessage(`Eliminado ID: ${formId}`);
    } catch (err) {
      setActionMessage(err.message || 'Error eliminando');
    } finally {
      setActionLoading(false);
      setActionType(null);
    }
  };

  const stats = useMemo(() => {
    const temps = statsSource.map(h => h.temp).filter(v => typeof v === 'number');
    const hums = statsSource.map(h => h.hum).filter(v => typeof v === 'number');
    const avg = arr => arr.length ? arr.reduce((a, b) => a + b, 0) / arr.length : null;
    const min = arr => arr.length ? Math.min(...arr) : null;
    const max = arr => arr.length ? Math.max(...arr) : null;
    return {
      temp: { avg: avg(temps), min: min(temps), max: max(temps) },
      hum: { avg: avg(hums), min: min(hums), max: max(hums) },
    };
  }, [statsSource]);

  const chartData = useMemo(() => {
    const ordered = [...statsSource].sort((a, b) => (a?._date ?? 0) - (b?._date ?? 0));
    const last = ordered.slice(-40); // cap para móvil
    return {
      temp: last.map(item => ({ value: item.temp ?? 0 })),
      hum: last.map(item => ({ value: item.hum ?? 0 })),
    };
  }, [statsSource]);

  const lastUpdatedLabel = lastUpdated ? new Date(lastUpdated).toLocaleTimeString() : '—';
  const isFormIdMissing = !formId;
  const hasHistory = rangeFilteredHistory.length > 0;
  const lastReadingLabel = latest?.formattedDate || latest?.datetime || '—';
  const isOnline = lastUpdated ? Date.now() - lastUpdated < 2 * 60 * 1000 : false;
  const statusLabel = error ? 'Error' : isOnline ? 'En línea' : 'Sin actualización';
  const statusTone = error ? 'error' : isOnline ? 'success' : 'warning';

  const handleLoadDemo = async () => {
    setDemoLoading(true);
    setActionMessage('');
    setFormError('');
    try {
      const baseDate = Date.now();
      const demoItems = Array.from({ length: 6 }).map((_, idx) => {
        const offset = (5 - idx) * 10 * 60 * 1000;
        return {
          uid: 'demo-esp32',
          temp: 22 + Math.random() * 6,
          hum: 45 + Math.random() * 20,
          luz: Math.random() > 0.5 ? 1 : 0,
          datetime: new Date(baseDate - offset).toISOString(),
        };
      });
      for (const payload of demoItems) {
        await api.create(payload);
      }
      await refreshHistory({ limit, sort: 'desc' });
      setActionMessage('Datos demo cargados');
    } catch (err) {
      setActionMessage(err.message || 'Error cargando demo');
    } finally {
      setDemoLoading(false);
    }
  };

  useEffect(() => {
    pulseAnim.setValue(0);
    Animated.timing(pulseAnim, {
      toValue: 1,
      duration: 450,
      useNativeDriver: true,
    }).start();
  }, [latest, statsSource.length, pulseAnim]);

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar barStyle="light-content" backgroundColor={colors.bg} />
      <ScrollView
        contentContainerStyle={styles.scroll}
        refreshControl={
          <RefreshControl
            refreshing={refreshing}
            onRefresh={handleRefreshAll}
            tintColor={colors.accent}
          />
        }
      >
        <View style={styles.hero}>
          <View>
            <Text style={styles.title}>Telemetría</Text>
            <Text style={styles.heroSubtitle}>Panel en tiempo real</Text>
          </View>
          <Pressable
            onPress={handleRefreshAll}
            disabled={refreshing}
            style={({ pressed }) => [styles.ghostButton, pressed && styles.ghostButtonPressed]}
          >
            <Text style={[styles.ghostButtonText, refreshing && styles.linkDisabled]}>
              {refreshing ? 'Actualizando…' : 'Actualizar'}
            </Text>
          </Pressable>
        </View>
        <View style={styles.metaRow}>
          <View style={[styles.pill, statusTone === 'success' && styles.pillSuccess, statusTone === 'warning' && styles.pillWarning, statusTone === 'error' && styles.pillError]}>
            <View style={[styles.statusDot, statusTone === 'success' && styles.dotSuccess, statusTone === 'warning' && styles.dotWarning, statusTone === 'error' && styles.dotError]} />
            <Text style={styles.pillText}>Estado: {statusLabel}</Text>
          </View>
          <View style={styles.pill}>
            <Text style={styles.pillText}>Última lectura: {lastReadingLabel}</Text>
          </View>
          <View style={styles.pill}>
            <Text style={styles.pillText}>{rangeFilteredHistory.length} registros</Text>
          </View>
        </View>
        <View style={styles.statusRow}>
          <Text style={styles.muted}>Estado</Text>
          {loading ? <ActivityIndicator size="small" color={colors.accent} /> : null}
        </View>
        {error ? (
          <View style={styles.errorBanner}>
            <Text style={styles.errorText}>{error}</Text>
            <Pressable onPress={handleRefreshAll}>
              <Text style={styles.link}>Reintentar</Text>
            </Pressable>
          </View>
        ) : null}

        <View style={styles.filterCard}>
          <View style={styles.filterHeader}>
            <Text style={styles.filterTitle}>Filtros</Text>
            {query ? (
              <Pressable onPress={() => setQuery('')}>
                <Text style={styles.clearBtn}>Limpiar</Text>
              </Pressable>
            ) : null}
          </View>
          <View style={styles.searchRow}>
            <TextInput
              placeholder="Filtrar por UID"
              placeholderTextColor={colors.muted}
              value={query}
              onChangeText={setQuery}
              style={styles.searchInput}
              autoCapitalize="none"
              autoCorrect={false}
              accessibilityLabel="Filtrar por UID"
            />
          </View>
          <View style={styles.filterRow}>
            <Text style={styles.muted}>Registros:</Text>
            {[20, 50, 100].map((value) => (
              <Pressable
                key={value}
                onPress={() => handleLimitChange(value)}
                style={({ pressed }) => [
                  styles.chip,
                  limit === value && styles.chipActive,
                  pressed && styles.chipPressed,
                ]}
              >
                <Text style={[styles.chipText, limit === value && styles.chipTextActive]}>{value}</Text>
              </Pressable>
            ))}
          </View>
          <View style={styles.filterRow}>
            <Text style={styles.muted}>Rango:</Text>
            {['24h', '7d', '30d', 'all'].map((value) => (
              <Pressable
                key={value}
                onPress={() => setRange(value)}
                style={({ pressed }) => [
                  styles.chip,
                  range === value && styles.chipActive,
                  pressed && styles.chipPressed,
                ]}
              >
                <Text style={[styles.chipText, range === value && styles.chipTextActive]}>
                  {value === 'all' ? 'Todo' : value}
                </Text>
              </Pressable>
            ))}
          </View>
        </View>
        <Animated.View style={[styles.animatedBlock, { opacity: pulseAnim, transform: [{ translateY: pulseAnim.interpolate({ inputRange: [0, 1], outputRange: [6, 0] }) }] }]}>
          <View style={styles.row}>
            <MetricCard title="Fecha" value={latest?.formattedDate || latest?.datetime || '—'} subtitle="Última lectura" />
            <MetricCard title="UID" value={latest?.uid || '—'} subtitle="Dispositivo" />
          </View>
          <View style={styles.row}>
            <MetricCard
              title="Temperatura"
              value={latest?.temp != null ? `${latest.temp.toFixed(1)} °C` : '—'}
              tone="accent"
            />
            <MetricCard
              title="Humedad"
              value={latest?.hum != null ? `${latest.hum.toFixed(1)} %` : '—'}
              tone="success"
            />
          </View>
          <MetricCard
            title="Luz"
            value={latest?.luz === 1 ? 'Encendida' : latest?.luz === 0 ? 'Apagada' : '—'}
            tone="warning"
            full
          />
        </Animated.View>
        <View style={styles.sectionHeader}>
          <Text style={styles.subtitle}>Tendencia</Text>
          <View style={styles.toggleRow}>
            <Text style={styles.muted}>Últimos {Math.min(rangeFilteredHistory.length, 40)} puntos</Text>
            <Pressable onPress={() => setSmooth((prev) => !prev)} style={styles.toggleButton}>
              <Text style={styles.toggleText}>{smooth ? 'Suavizado ON' : 'Suavizado OFF'}</Text>
            </Pressable>
          </View>
        </View>
        <Animated.View style={[styles.animatedBlock, { opacity: pulseAnim, transform: [{ translateY: pulseAnim.interpolate({ inputRange: [0, 1], outputRange: [6, 0] }) }] }]}>
          <LineChart tempPoints={chartData.temp} humPoints={chartData.hum} smooth={smooth} />
        </Animated.View>
        <View style={styles.sectionHeader}>
          <Text style={styles.subtitle}>Analítica</Text>
          <Text style={styles.muted}>{rangeFilteredHistory.length} registros</Text>
        </View>
        <View style={styles.rowWrap}>
          <MetricCard title="Temp prom" value={stats.temp.avg != null ? `${stats.temp.avg.toFixed(1)} °C` : '—'} />
          <MetricCard title="Temp min" value={stats.temp.min != null ? `${stats.temp.min.toFixed(1)} °C` : '—'} />
          <MetricCard title="Temp máx" value={stats.temp.max != null ? `${stats.temp.max.toFixed(1)} °C` : '—'} />
        </View>
        <View style={styles.rowWrap}>
          <MetricCard title="Hum prom" value={stats.hum.avg != null ? `${stats.hum.avg.toFixed(1)} %` : '—'} />
          <MetricCard title="Hum min" value={stats.hum.min != null ? `${stats.hum.min.toFixed(1)} %` : '—'} />
          <MetricCard title="Hum máx" value={stats.hum.max != null ? `${stats.hum.max.toFixed(1)} %` : '—'} />
        </View>
        <View style={styles.sectionHeader}>
          <Text style={styles.subtitle}>Histórico</Text>
          <Pressable onPress={() => refreshHistory({ limit, sort: 'desc' })}>
            <Text style={styles.link}>Refrescar</Text>
          </Pressable>
        </View>
        {!loading && !hasHistory ? (
          <View style={styles.emptyCard}>
            <Text style={styles.emptyTitle}>Sin datos todavía</Text>
            <Text style={styles.emptyText}>Carga datos demo para ver la interfaz en acción.</Text>
            <Pressable
              onPress={handleLoadDemo}
              disabled={demoLoading}
              style={[styles.emptyButton, demoLoading && styles.actionBtnDisabled]}
            >
              <Text style={styles.emptyButtonText}>{demoLoading ? 'Cargando…' : 'Cargar demo'}</Text>
            </Pressable>
          </View>
        ) : (
          <HistoryList data={rangeFilteredHistory} loading={loading} />
        )}

        <View style={styles.sectionHeader}>
          <Text style={styles.subtitle}>Operaciones</Text>
          <Text style={styles.muted}>Crear / Editar / Borrar</Text>
        </View>
        <View style={styles.formCard}>
          <View style={styles.formRow}>
            <TextInput
              placeholder="ID (para editar/borrar)"
              placeholderTextColor={colors.muted}
              value={formId}
              onChangeText={(value) => {
                setFormId(value);
                if (value) setFormError('');
                if (value) setShowIdHint(false);
              }}
              keyboardType="numeric"
              style={[styles.formInput, formError && isFormIdMissing && styles.formInputError]}
            />
            <TextInput
              placeholder="UID"
              placeholderTextColor={colors.muted}
              value={formUid}
              onChangeText={setFormUid}
              style={styles.formInput}
            />
          </View>
          {formError ? <Text style={styles.formError}>{formError}</Text> : null}
          <TextInput
            placeholder="datetime (YYYY-MM-DDTHH:mm:ss)"
            placeholderTextColor={colors.muted}
            value={formDatetime}
            onChangeText={setFormDatetime}
            style={styles.formInput}
          />
          <View style={styles.formRow}>
            <TextInput
              placeholder="luz"
              placeholderTextColor={colors.muted}
              value={formLuz}
              onChangeText={setFormLuz}
              keyboardType="numeric"
              style={styles.formInput}
            />
            <TextInput
              placeholder="temp"
              placeholderTextColor={colors.muted}
              value={formTemp}
              onChangeText={setFormTemp}
              keyboardType="numeric"
              style={styles.formInput}
            />
            <TextInput
              placeholder="hum"
              placeholderTextColor={colors.muted}
              value={formHum}
              onChangeText={setFormHum}
              keyboardType="numeric"
              style={styles.formInput}
            />
          </View>
          <View style={styles.actionRow}>
            <Pressable
              onPress={handleCreate}
              disabled={actionLoading}
              style={[
                styles.actionBtn,
                styles.actionPrimary,
                actionLoading && styles.actionBtnDisabled,
              ]}
            >
              <Text style={styles.actionText}>
                {actionLoading && actionType === 'create' ? 'Creando…' : 'Crear'}
              </Text>
            </Pressable>
            <Pressable
              onPress={handleReplace}
              disabled={actionLoading || isFormIdMissing}
              style={[
                styles.actionBtn,
                (actionLoading || isFormIdMissing) && styles.actionBtnDisabled,
              ]}
            >
              <Text style={styles.actionText}>
                {actionLoading && actionType === 'replace' ? 'Reemplazando…' : 'Reemplazar'}
              </Text>
            </Pressable>
            <Pressable
              onPress={handlePatch}
              disabled={actionLoading || isFormIdMissing}
              style={[
                styles.actionBtn,
                (actionLoading || isFormIdMissing) && styles.actionBtnDisabled,
              ]}
            >
              <Text style={styles.actionText}>
                {actionLoading && actionType === 'patch' ? 'Modificando…' : 'Modificar'}
              </Text>
            </Pressable>
            <Pressable
              onPress={handleDelete}
              disabled={actionLoading || isFormIdMissing}
              style={[
                styles.actionBtn,
                styles.actionDanger,
                (actionLoading || isFormIdMissing) && styles.actionBtnDisabled,
              ]}
            >
              <Text style={styles.actionText}>
                {actionLoading && actionType === 'delete' ? 'Borrando…' : 'Borrar'}
              </Text>
            </Pressable>
          </View>
          {showIdHint && isFormIdMissing ? (
            <Text style={styles.helperText}>Ingresa un ID para reemplazar, modificar o borrar.</Text>
          ) : null}
          {actionMessage ? <Text style={styles.actionMessage}>{actionMessage}</Text> : null}
        </View>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: colors.bg,
  },
  scroll: {
    padding: 16,
    paddingBottom: 48,
  },
  hero: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  row: {
    flexDirection: 'row',
    gap: 12,
    marginBottom: 12,
  },
  rowWrap: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 12,
    marginBottom: 12,
  },
  title: {
    fontSize: 26,
    fontWeight: '800',
    color: colors.text,
  },
  heroSubtitle: {
    color: colors.muted,
    marginTop: 4,
  },
  ghostButton: {
    borderWidth: 1,
    borderColor: colors.border,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999,
    backgroundColor: colors.surfaceAlt,
  },
  ghostButtonPressed: {
    opacity: 0.8,
  },
  ghostButtonText: {
    color: colors.accent,
    fontWeight: '600',
  },
  subtitle: {
    fontSize: 18,
    fontWeight: '600',
    color: colors.text,
  },
  link: {
    color: colors.accent,
    fontWeight: '600',
  },
  linkDisabled: {
    opacity: 0.6,
  },
  metaRow: {
    flexDirection: 'row',
    gap: 8,
    flexWrap: 'wrap',
    marginBottom: 8,
  },
  pill: {
    backgroundColor: colors.surfaceAlt,
    borderColor: colors.border,
    borderWidth: 1,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 999,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
  },
  pillText: {
    color: colors.muted,
    fontSize: 12,
  },
  pillSuccess: {
    borderColor: colors.success,
  },
  pillWarning: {
    borderColor: colors.warning,
  },
  pillError: {
    borderColor: colors.error,
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 999,
    backgroundColor: colors.muted,
  },
  dotSuccess: {
    backgroundColor: colors.success,
  },
  dotWarning: {
    backgroundColor: colors.warning,
  },
  dotError: {
    backgroundColor: colors.error,
  },
  statusRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 14,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 8,
  },
  muted: {
    color: colors.muted,
  },
  errorBanner: {
    backgroundColor: '#3B0D0C',
    borderColor: colors.error,
    borderWidth: 1,
    borderRadius: 10,
    padding: 10,
    marginBottom: 12,
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  errorText: {
    color: colors.error,
    flex: 1,
    marginRight: 8,
  },
  filterCard: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 12,
    marginBottom: 12,
    gap: 8,
  },
  filterHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
  },
  filterTitle: {
    color: colors.text,
    fontWeight: '600',
  },
  searchRow: {
    flexDirection: 'row',
    gap: 8,
    marginBottom: 12,
    alignItems: 'center',
  },
  searchInput: {
    flex: 1,
    backgroundColor: colors.surfaceAlt,
    borderColor: colors.border,
    borderWidth: 1,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 8,
    color: colors.text,
  },
  clearBtn: {
    color: colors.accent,
    fontWeight: '600',
  },
  filterRow: {
    flexDirection: 'row',
    gap: 8,
    alignItems: 'center',
    marginBottom: 12,
  },
  chip: {
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surfaceAlt,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 999,
  },
  chipActive: {
    borderColor: colors.accent,
    backgroundColor: colors.accentSoft,
  },
  chipPressed: {
    opacity: 0.8,
  },
  chipText: {
    color: colors.muted,
    fontWeight: '600',
  },
  chipTextActive: {
    color: colors.accent,
  },
  formCard: {
    marginTop: 8,
    backgroundColor: colors.surface,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 12,
    gap: 10,
  },
  formRow: {
    flexDirection: 'row',
    gap: 10,
  },
  formInput: {
    flex: 1,
    backgroundColor: colors.surfaceAlt,
    borderColor: colors.border,
    borderWidth: 1,
    borderRadius: 10,
    paddingHorizontal: 12,
    paddingVertical: 8,
    color: colors.text,
  },
  formInputError: {
    borderColor: colors.error,
  },
  formError: {
    color: colors.error,
    marginTop: -2,
  },
  actionRow: {
    flexDirection: 'row',
    gap: 8,
    flexWrap: 'wrap',
  },
  actionBtn: {
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surfaceAlt,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 10,
  },
  actionBtnDisabled: {
    opacity: 0.6,
  },
  actionPrimary: {
    backgroundColor: colors.accentSoft,
    borderColor: colors.accent,
  },
  actionDanger: {
    backgroundColor: '#3B0D0C',
    borderColor: colors.error,
  },
  actionText: {
    color: colors.text,
    fontWeight: '600',
  },
  actionMessage: {
    color: colors.muted,
  },
  helperText: {
    color: colors.muted,
    fontSize: 12,
  },
  toggleRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  toggleButton: {
    borderWidth: 1,
    borderColor: colors.border,
    backgroundColor: colors.surfaceAlt,
    paddingHorizontal: 10,
    paddingVertical: 6,
    borderRadius: 999,
  },
  toggleText: {
    color: colors.muted,
    fontSize: 12,
  },
  emptyCard: {
    backgroundColor: colors.surface,
    borderRadius: 12,
    borderWidth: 1,
    borderColor: colors.border,
    padding: 16,
    gap: 10,
    alignItems: 'flex-start',
  },
  emptyTitle: {
    color: colors.text,
    fontSize: 16,
    fontWeight: '700',
  },
  emptyText: {
    color: colors.muted,
  },
  emptyButton: {
    borderWidth: 1,
    borderColor: colors.accent,
    backgroundColor: colors.accentSoft,
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 10,
  },
  emptyButtonText: {
    color: colors.text,
    fontWeight: '600',
  },
  animatedBlock: {
    marginBottom: 4,
  },
});
