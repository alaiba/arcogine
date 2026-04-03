import { describe, it, expect, beforeEach, vi } from 'vitest';
import { getHealth, postScenario, getSnapshot, postPrice, postSimRun } from './client';

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
});
