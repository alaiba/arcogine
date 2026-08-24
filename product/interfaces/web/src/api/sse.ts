import type { SimEvent } from './client';

const DEFAULT_URL = '/api/events/stream';

const SSE_EVENT_NAMES = [
  'OrderCreation',
  'TaskStart',
  'TaskEnd',
  'MachineAvailabilityChange',
  'PriceChange',
  'AgentDecision',
  'DemandEvaluation',
  'AgentEvaluation',
] as const;

export class SseClient {
  private es: EventSource | null = null;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private shouldReconnect = false;
  private delayMs: number;
  private readonly initialDelayMs: number;
  private readonly maxDelayMs: number;
  private readonly url: string;
  private readonly onEvent: (event: SimEvent) => void;

  constructor(
    onEvent: (event: SimEvent) => void,
    options?: { url?: string; initialReconnectDelayMs?: number; maxReconnectDelayMs?: number },
  ) {
    this.onEvent = onEvent;
    this.url = options?.url ?? DEFAULT_URL;
    this.initialDelayMs = options?.initialReconnectDelayMs ?? 1000;
    this.delayMs = this.initialDelayMs;
    this.maxDelayMs = options?.maxReconnectDelayMs ?? 30_000;
  }

  connect(): void {
    this.shouldReconnect = true;
    this.delayMs = this.initialDelayMs;
    if (this.es?.readyState === EventSource.OPEN || this.es?.readyState === EventSource.CONNECTING) {
      return;
    }
    this.clearReconnectTimer();
    this.open();
  }

  disconnect(): void {
    this.shouldReconnect = false;
    this.clearReconnectTimer();
    if (this.es) {
      this.es.close();
      this.es = null;
    }
  }

  private clearReconnectTimer(): void {
    if (this.reconnectTimer !== null) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
  }

  private scheduleReconnect(): void {
    if (!this.shouldReconnect) return;
    this.clearReconnectTimer();
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      this.open();
    }, this.delayMs);
    this.delayMs = Math.min(this.delayMs * 2, this.maxDelayMs);
  }

  private open(): void {
    if (!this.shouldReconnect) return;
    const es = new EventSource(this.url);
    this.es = es;

    const onData = (ev: MessageEvent<string>) => {
      try {
        const parsed = JSON.parse(ev.data) as SimEvent;
        this.onEvent(parsed);
      } catch {
        /* ignore malformed payloads */
      }
    };

    for (const name of SSE_EVENT_NAMES) {
      es.addEventListener(name, onData as EventListener);
    }

    es.onopen = () => {
      this.delayMs = this.initialDelayMs;
    };

    es.onerror = () => {
      es.close();
      if (this.es === es) {
        this.es = null;
      }
      if (this.shouldReconnect) {
        this.scheduleReconnect();
      }
    };
  }
}
