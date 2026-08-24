import { describe, it, expect, beforeEach, vi } from 'vitest';
import {
  getHealth,
  getKpis,
  getTopology,
  getJobs,
  getExportEvents,
  postScenario,
  postPrice,
  postSimRun,
  postSimPause,
  postSimStep,
  postSimReset,
  postMachines,
  postAgent,
  getSnapshot,
} from './client';

const fetchMock = vi.fn();
vi.stubGlobal('fetch', fetchMock);

function jsonResponse(body: unknown, status = 200, statusText = 'OK') {
  return new Response(JSON.stringify(body), {
    status,
    statusText,
    headers: { 'Content-Type': 'application/json' },
  });
}

function textResponse(text: string, status = 500, statusText = 'Internal Server Error') {
  return new Response(text, { status, statusText });
}

describe('client', () => {
  beforeEach(() => {
    fetchMock.mockReset();
  });

  describe('readErrorMessage (via jsonRequest)', () => {
    it('extracts error field from JSON response', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ error: 'bad input' }, 400, 'Bad Request'));
      await expect(getHealth()).rejects.toThrow('bad input');
    });

    it('extracts message field as fallback', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ message: 'not found' }, 404, 'Not Found'));
      await expect(getHealth()).rejects.toThrow('not found');
    });

    it('falls back to status text for non-JSON', async () => {
      fetchMock.mockResolvedValue(textResponse('not json', 500, 'Server Error'));
      await expect(getHealth()).rejects.toThrow('Server Error');
    });

    it('falls back to status text when JSON has no message', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ details: 'internal detail' }, 500, 'Server Error'));
      await expect(getHealth()).rejects.toThrow('Server Error');
    });
  });

  describe('jsonRequest', () => {
    it('throws on non-OK response with server message', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ error: 'conflict' }, 409));
      await expect(postSimRun()).rejects.toThrow('conflict');
    });
  });

  describe('postScenario', () => {
    it('sends correct payload and returns parsed response', async () => {
      const payload = { success: true, message: 'loaded' };
      fetchMock.mockResolvedValue(jsonResponse(payload));
      const result = await postScenario('toml-content');
      expect(result).toEqual(payload);
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/scenario',
        expect.objectContaining({
          method: 'POST',
        }),
      );
    });
  });

  describe('getHealth', () => {
    it('returns health status', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ status: 'ok' }));
      const result = await getHealth();
      expect(result).toEqual({ status: 'ok' });
    });
  });

  describe('getSnapshot', () => {
    it('returns snapshot', async () => {
      const snap = { run_state: 'Idle', current_time: 0 };
      fetchMock.mockResolvedValue(jsonResponse(snap));
      const result = await getSnapshot();
      expect(result).toEqual(snap);
    });
  });

  describe('postPrice', () => {
    it('sends price and returns snapshot', async () => {
      const snap = { run_state: 'Paused', current_price: 15 };
      fetchMock.mockResolvedValue(jsonResponse(snap));
      const result = await postPrice(15);
      expect(result).toEqual(snap);
    });

    it('handles error response', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ error: 'invalid price' }, 400));
      await expect(postPrice(-1)).rejects.toThrow('invalid price');
    });
  });

  describe('request helpers', () => {
    it('adds json content-type for request bodies', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ run_state: 'Idle', current_time: 0 }));
      await postPrice(15);
      const headers = fetchMock.mock.calls[0]?.[1]?.headers as Headers;
      expect(headers.get('Content-Type')).toBe('application/json');
    });

    it('posts sim control endpoints', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ run_state: 'Paused', current_time: 1 }));
      await postSimPause();
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/sim/pause',
        expect.objectContaining({ method: 'POST' }),
      );

      fetchMock.mockResolvedValue(jsonResponse({ run_state: 'Running', current_time: 2 }));
      await postSimStep();
      expect(fetchMock).toHaveBeenCalledWith('/api/sim/step', expect.objectContaining({ method: 'POST' }));

      fetchMock.mockResolvedValue(jsonResponse({ run_state: 'Idle', current_time: 0 }));
      await postSimReset();
      expect(fetchMock).toHaveBeenCalledWith('/api/sim/reset', expect.objectContaining({ method: 'POST' }));
    });

    it('posts machine and agent updates', async () => {
      fetchMock.mockResolvedValue(jsonResponse({ run_state: 'Running', current_time: 3 }));
      await postMachines(42, false);
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/machines',
        expect.objectContaining({
          method: 'POST',
          body: JSON.stringify({ machine_id: 42, online: false }),
        }),
      );

      fetchMock.mockResolvedValue(jsonResponse({ run_state: 'Running', current_time: 4 }));
      await postAgent(true);
      expect(fetchMock).toHaveBeenCalledWith(
        '/api/agent',
        expect.objectContaining({ method: 'POST', body: JSON.stringify({ enabled: true }) }),
      );
    });

    it('fetches collection endpoints', async () => {
      fetchMock.mockResolvedValue(jsonResponse([{ time: 0, value: 1 }]));
      await getKpis();
      expect(fetchMock).toHaveBeenCalledWith('/api/kpis', expect.anything());

      fetchMock.mockResolvedValue(jsonResponse({ machines: [], edges: [] }));
      await getTopology();
      expect(fetchMock).toHaveBeenCalledWith('/api/factory/topology', expect.anything());

      fetchMock.mockResolvedValue(jsonResponse([{ job_id: 1 }]));
      await getJobs();
      expect(fetchMock).toHaveBeenCalledWith('/api/jobs', expect.anything());

      fetchMock.mockResolvedValue(jsonResponse({ events: [] }));
      await getExportEvents();
      expect(fetchMock).toHaveBeenCalledWith('/api/export/events', expect.anything());

      fetchMock.mockResolvedValue(jsonResponse({ events: [] }));
      expect(await getExportEvents()).toEqual({ events: [] });
    });

    it('returns jobs list on getJobs', async () => {
      const jobs = [
        {
          job_id: 1,
          product_id: 2,
          quantity: 5,
          status: 'Completed',
          current_step: 2,
          total_steps: 2,
          created_at: 1,
          completed_at: 3,
          revenue: 42,
        },
      ];
      fetchMock.mockResolvedValue(jsonResponse(jobs));
      const result = await getJobs();
      expect(result).toEqual(jobs);
    });
  });
});
