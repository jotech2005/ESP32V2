import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import AsyncStorage from '@react-native-async-storage/async-storage';
import { api } from '../api/client';

const CACHE_KEY_LATEST = 'telemetry/latest';
const CACHE_KEY_HISTORY = 'telemetry/history';

export function useTelemetry() {
  const [latest, setLatest] = useState(null);
  const [history, setHistory] = useState([]);
  const [loadingLatest, setLoadingLatest] = useState(false);
  const [loadingHistory, setLoadingHistory] = useState(false);
  const [refreshing, setRefreshing] = useState(false);
  const [error, setError] = useState(null);
  const [lastUpdated, setLastUpdated] = useState(null);
  const mountedRef = useRef(true);

  const toNumber = value => {
    if (value === null || value === undefined || value === '') return null;
    const n = Number(value);
    return Number.isFinite(n) ? n : null;
  };

  const parseDate = value => {
    if (!value) return null;
    if (value instanceof Date) return value;
    if (typeof value === 'number') return new Date(value);
    const asNumber = Number(value);
    if (Number.isFinite(asNumber)) return new Date(asNumber);
    const parsed = new Date(value);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  };

  const normalizeItem = useCallback((item) => {
    if (!item || typeof item !== 'object') return item;
    const temp = toNumber(item.temp);
    const hum = toNumber(item.hum);
    const luz = item.luz === 1 || item.luz === '1' ? 1 : item.luz === 0 || item.luz === '0' ? 0 : item.luz ?? null;
    const dateValue = item.datetime ?? item.date ?? item.timestamp ?? item.time;
    const parsedDate = parseDate(dateValue);

    return {
      ...item,
      temp,
      hum,
      luz,
      datetime: parsedDate ? parsedDate.toISOString() : item.datetime,
      formattedDate: parsedDate ? parsedDate.toLocaleString() : (item.datetime ?? '—'),
      _date: parsedDate ? parsedDate.getTime() : 0,
    };
  }, []);

  const normalizeHistory = useCallback((list) => {
    if (!Array.isArray(list)) return [];
    return list
      .map(normalizeItem)
      .sort((a, b) => (b?._date ?? 0) - (a?._date ?? 0));
  }, [normalizeItem]);

  const loadCache = useCallback(async () => {
    const [cachedLatest, cachedHistory] = await Promise.all([
      AsyncStorage.getItem(CACHE_KEY_LATEST),
      AsyncStorage.getItem(CACHE_KEY_HISTORY),
    ]);
    if (!mountedRef.current) return;
    if (cachedLatest) {
      setLatest(normalizeItem(JSON.parse(cachedLatest)));
    }
    if (cachedHistory) {
      setHistory(normalizeHistory(JSON.parse(cachedHistory)));
    }
  }, [normalizeHistory, normalizeItem]);

  const refreshAll = useCallback(async (filters = {}) => {
    setRefreshing(true);
    setError(null);
    setLoadingLatest(true);
    setLoadingHistory(true);

    const [historyRes] = await Promise.allSettled([
      api.list({ limit: 50, sort: 'desc', ...filters }),
    ]);

    if (!mountedRef.current) return;

    if (historyRes.status === 'fulfilled') {
      const normalized = normalizeHistory(historyRes.value);
      setHistory(normalized);
      const latestItem = normalized[0] || null;
      setLatest(latestItem);
      await AsyncStorage.setItem(CACHE_KEY_HISTORY, JSON.stringify(normalized));
      await AsyncStorage.setItem(CACHE_KEY_LATEST, JSON.stringify(latestItem));
    }

    if (historyRes.status === 'rejected') {
      const message = historyRes.reason?.message;
      setError(message || 'Error cargando datos');
    } else {
      setLastUpdated(Date.now());
    }

    setLoadingLatest(false);
    setLoadingHistory(false);
    setRefreshing(false);
  }, [normalizeHistory, normalizeItem]);

  const refreshHistory = useCallback(async (filters = {}) => {
    setLoadingHistory(true);
    setError(null);
    try {
      const list = await api.list({ limit: 100, sort: 'desc', ...filters });
      const normalized = normalizeHistory(list);
      if (mountedRef.current) {
        setHistory(normalized);
        setLastUpdated(Date.now());
      }
      await AsyncStorage.setItem(CACHE_KEY_HISTORY, JSON.stringify(normalized));
    } catch (err) {
      if (mountedRef.current) setError(err.message || 'Error cargando histórico');
    }
    if (mountedRef.current) setLoadingHistory(false);
  }, [normalizeHistory]);

  const createTelemetry = useCallback(async (payload, filters = {}) => {
    setError(null);
    try {
      const res = await api.create(payload);
      await refreshHistory(filters);
      return res;
    } catch (err) {
      if (mountedRef.current) setError(err.message || 'Error creando telemetría');
      throw err;
    }
  }, [refreshHistory]);

  const replaceTelemetry = useCallback(async (id, payload, filters = {}) => {
    setError(null);
    try {
      const res = await api.replace(id, payload);
      await refreshHistory(filters);
      return res;
    } catch (err) {
      if (mountedRef.current) setError(err.message || 'Error reemplazando telemetría');
      throw err;
    }
  }, [refreshHistory]);

  const patchTelemetry = useCallback(async (id, payload, filters = {}) => {
    setError(null);
    try {
      const res = await api.patch(id, payload);
      await refreshHistory(filters);
      return res;
    } catch (err) {
      if (mountedRef.current) setError(err.message || 'Error modificando telemetría');
      throw err;
    }
  }, [refreshHistory]);

  const deleteTelemetry = useCallback(async (id, filters = {}) => {
    setError(null);
    try {
      const res = await api.remove(id);
      await refreshHistory(filters);
      return res;
    } catch (err) {
      if (mountedRef.current) setError(err.message || 'Error eliminando telemetría');
      throw err;
    }
  }, [refreshHistory]);

  useEffect(() => {
    mountedRef.current = true;
    loadCache().then(refreshAll);
    return () => {
      mountedRef.current = false;
    };
  }, [loadCache, refreshAll]);

  const loading = useMemo(() => loadingLatest || loadingHistory, [loadingLatest, loadingHistory]);

  return {
    latest,
    history,
    loading,
    loadingLatest,
    loadingHistory,
    refreshing,
    error,
    lastUpdated,
    refreshAll,
    refreshHistory,
    createTelemetry,
    replaceTelemetry,
    patchTelemetry,
    deleteTelemetry,
  };
}
