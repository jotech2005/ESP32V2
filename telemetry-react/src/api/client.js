import Constants from 'expo-constants';

const base = (Constants?.expoConfig?.extra?.apiBase || '').replace(/\/$/, '') || 'http://192.168.1.249:8080/api/telemetry/batch';
const DEFAULT_TIMEOUT_MS = 8000;

async function request(path, params = {}, { method = 'GET', body, timeoutMs = DEFAULT_TIMEOUT_MS } = {}) {
  const url = new URL(`${base}${path || ''}`);
  Object.entries(params).forEach(([k, v]) => {
    if (v !== undefined && v !== null && v !== '') url.searchParams.set(k, v);
  });

  const controller = new AbortController();
  const timeout = setTimeout(() => controller.abort(), timeoutMs);

  let res;
  try {
    res = await fetch(url.toString(), {
      method,
      headers: {
        Accept: 'application/json',
        ...(body ? { 'Content-Type': 'application/json' } : null),
      },
      body: body ? JSON.stringify(body) : undefined,
      signal: controller.signal,
    });
  } catch (err) {
    if (err?.name === 'AbortError') {
      throw new Error('Tiempo de espera agotado');
    }
    throw new Error('No se pudo conectar con la API');
  } finally {
    clearTimeout(timeout);
  }

  if (!res.ok) throw new Error(`API ${res.status}`);
  if (res.status === 204) return null;
  return res.json();
}

export const api = {
  list: (opts = {}) => request('', opts),
  getById: (id) => request(`/${id}`),
  create: (payload) => request('', {}, { method: 'POST', body: payload }),
  replace: (id, payload) => request(`/${id}`, {}, { method: 'PUT', body: payload }),
  patch: (id, payload) => request(`/${id}`, {}, { method: 'PATCH', body: payload }),
  remove: (id) => request(`/${id}`, {}, { method: 'DELETE' }),
  latest: async () => {
    const list = await request('');
    if (!Array.isArray(list) || list.length === 0) return null;
    return list.reduce((acc, item) => {
      if (!acc) return item;
      const accDate = new Date(acc.datetime || acc.date || acc.timestamp || 0).getTime();
      const itemDate = new Date(item.datetime || item.date || item.timestamp || 0).getTime();
      if (!Number.isFinite(accDate) || !Number.isFinite(itemDate)) return acc;
      return itemDate > accDate ? item : acc;
    }, null);
  },
};
