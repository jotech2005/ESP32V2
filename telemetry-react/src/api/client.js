import Constants from 'expo-constants';

const base = (Constants?.expoConfig?.extra?.apiBase || '').replace(/\/$/, '') || 'http://192.168.1.249:8080';
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

// Normaliza la respuesta de la API para extraer los datos
const extractData = (response) => {
  if (response?.success && response?.data) {
    return response.data;
  }
  return response;
};

export const api = {
  // GET: Obtener todos los datos
  list: async (opts = {}) => {
    const response = await request('/api/sensor-data', opts);
    return extractData(response);
  },

  // GET: Obtener por ID
  getById: async (id) => {
    const response = await request(`/api/sensor-data/${id}`);
    return extractData(response);
  },

  // GET: Últimas N lecturas
  getLatest: async (limit = 10) => {
    const response = await request(`/api/sensor-data/latest/${limit}`);
    return extractData(response);
  },

  // GET: Buscar por RFID
  getByRfid: async (rfidTag) => {
    const response = await request(`/api/sensor-data/rfid/${rfidTag}`);
    return extractData(response);
  },

  // GET: Buscar por rango de fechas
  getByDateRange: async (startDate, endDate) => {
    const response = await request('/api/sensor-data/date-range', { startDate, endDate });
    return extractData(response);
  },

  // GET: Datos con luz detectada
  getLightDetected: async () => {
    const response = await request('/api/sensor-data/light-detected');
    return extractData(response);
  },

  // POST: Crear nuevo registro
  create: async (payload) => {
    const response = await request('/api/sensor-data', {}, { method: 'POST', body: payload });
    return response;
  },

  // PUT: Actualizar registro
  replace: async (id, payload) => {
    const response = await request(`/api/sensor-data/${id}`, {}, { method: 'PUT', body: payload });
    return response;
  },

  // DELETE: Eliminar registro
  remove: async (id) => {
    const response = await request(`/api/sensor-data/${id}`, {}, { method: 'DELETE' });
    return response;
  },

  // GET: Estadísticas - total de registros
  getTotalRecords: async () => {
    const response = await request('/api/sensor-data/stats/total-records');
    return response;
  },

  // GET: Estadísticas - temperatura máxima
  getMaxTemperature: async (startDate, endDate) => {
    const response = await request('/api/sensor-data/stats/temperature-max', { startDate, endDate });
    return response;
  },

  // GET: Estadísticas - humedad promedio
  getAverageHumidity: async (startDate, endDate) => {
    const response = await request('/api/sensor-data/stats/humidity-avg', { startDate, endDate });
    return response;
  },

  // GET: Health check
  health: async () => {
    const response = await request('/api/sensor-data/health');
    return response;
  },

  // GET: Última lectura (helper)
  latest: async () => {
    const list = await api.getLatest(1);
    if (!Array.isArray(list) || list.length === 0) return null;
    return list[0];
  },
};
