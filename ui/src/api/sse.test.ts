import { describe, it, expect, beforeEach, vi, afterEach } from 'vitest';
import { SseClient } from './sse';
import type { SimEvent } from './client';

class MockEventSource {
  static readonly CONNECTING = 0;
  static readonly OPEN = 1;
  static readonly CLOSED = 2;
  readonly CONNECTING = 0;
  readonly OPEN = 1;
  readonly CLOSED = 2;

  readyState = MockEventSource.CONNECTING;
  onopen: ((ev: Event) => void) | null = null;
  onerror: ((ev: Event) => void) | null = null;
  onmessage: ((ev: MessageEvent) => void) | null = null;
  url: string;

  private listeners: Record<string, EventListener[]> = {};

  constructor(url: string) {
    this.url = url;
    queueMicrotask(() => {
      this.readyState = MockEventSource.OPEN;
      this.onopen?.(new Event('open'));
    });
  }

  addEventListener(name: string, cb: EventListener) {
    if (!this.listeners[name]) this.listeners[name] = [];
    this.listeners[name].push(cb);
  }

  removeEventListener(name: string, cb: EventListener) {
    this.listeners[name] = (this.listeners[name] ?? []).filter((fn) => fn !== cb);
  }

  dispatchEvent(event: Event): boolean {
    const cbs = this.listeners[(event as MessageEvent).type] ?? [];
    for (const cb of cbs) cb(event);
    return true;
  }

  emit(name: string, data: string) {
    const event = new MessageEvent(name, { data });
    this.dispatchEvent(event);
  }

  simulateError() {
    this.readyState = MockEventSource.CLOSED;
    this.onerror?.(new Event('error'));
  }

  close() {
    this.readyState = MockEventSource.CLOSED;
  }
}

vi.stubGlobal('EventSource', MockEventSource);

describe('SseClient', () => {
  let eventSpy: ReturnType<typeof vi.fn<(event: SimEvent) => void>>;
  let client: SseClient;

  beforeEach(() => {
    vi.useFakeTimers();
    eventSpy = vi.fn<(event: SimEvent) => void>();
    client = new SseClient(eventSpy, {
      initialReconnectDelayMs: 100,
      maxReconnectDelayMs: 800,
    });
  });

  afterEach(() => {
    client.disconnect();
    vi.useRealTimers();
  });

  it('connect is idempotent when already open', async () => {
    client.connect();
    await vi.advanceTimersByTimeAsync(0);
    client.connect();
    expect(eventSpy).not.toHaveBeenCalled();
  });

  it('onEvent callback fires for valid JSON payloads', async () => {
    client.connect();
    await vi.advanceTimersByTimeAsync(0);

    const es = (client as unknown as { es: MockEventSource }).es;
    es.emit('TaskEnd', JSON.stringify({ time: { '0': 10 }, event_type: 'TaskEnd', payload: {} }));
    expect(eventSpy).toHaveBeenCalledTimes(1);
    expect(eventSpy).toHaveBeenCalledWith({
      time: { '0': 10 },
      event_type: 'TaskEnd',
      payload: {},
    });
  });

  it('malformed JSON is silently ignored', async () => {
    client.connect();
    await vi.advanceTimersByTimeAsync(0);

    const es = (client as unknown as { es: MockEventSource }).es;
    es.emit('TaskEnd', 'not-json');
    expect(eventSpy).not.toHaveBeenCalled();
  });

  it('disconnect closes the connection', async () => {
    client.connect();
    await vi.advanceTimersByTimeAsync(0);

    const es = (client as unknown as { es: MockEventSource }).es;
    client.disconnect();
    expect(es.readyState).toBe(MockEventSource.CLOSED);
  });

  it('reconnect delay doubles on each failure up to cap', async () => {
    client.connect();
    await vi.advanceTimersByTimeAsync(0);

    const es1 = (client as unknown as { es: MockEventSource }).es;
    es1.simulateError();

    await vi.advanceTimersByTimeAsync(100);
    const es2 = (client as unknown as { es: MockEventSource }).es;
    expect(es2).not.toBeNull();
    es2.simulateError();

    await vi.advanceTimersByTimeAsync(200);
    const es3 = (client as unknown as { es: MockEventSource }).es;
    expect(es3).not.toBeNull();
    es3.simulateError();

    await vi.advanceTimersByTimeAsync(400);
    const es4 = (client as unknown as { es: MockEventSource }).es;
    expect(es4).not.toBeNull();
    es4.simulateError();

    await vi.advanceTimersByTimeAsync(800);
    const es5 = (client as unknown as { es: MockEventSource }).es;
    expect(es5).not.toBeNull();
    es5.simulateError();

    await vi.advanceTimersByTimeAsync(800);
    expect((client as unknown as { es: MockEventSource | null }).es).not.toBeNull();
  });
});
