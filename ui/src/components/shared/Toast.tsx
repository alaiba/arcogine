import { useEffect } from 'react';

export type ToastType = 'error' | 'success' | 'info';

type ToastProps = {
  message: string;
  type: ToastType;
  onDismiss: () => void;
};

const typeStyles: Record<ToastType, string> = {
  error: 'border-red-800/80 bg-red-950/95 text-red-100 shadow-red-900/20',
  success: 'border-emerald-800/80 bg-emerald-950/95 text-emerald-50 shadow-emerald-900/20',
  info: 'border-sky-800/80 bg-sky-950/95 text-sky-50 shadow-sky-900/20',
};

export function Toast({ message, type, onDismiss }: ToastProps) {
  useEffect(() => {
    const id = window.setTimeout(onDismiss, 5000);
    return () => window.clearTimeout(id);
  }, [message, onDismiss]);

  return (
    <div
      role="status"
      className={`fixed right-4 top-4 z-[100] max-w-sm rounded-lg border px-4 py-3 text-sm shadow-lg backdrop-blur-sm ${typeStyles[type]}`}
    >
      <div className="flex items-start gap-3">
        <p className="flex-1 leading-snug">{message}</p>
        <button
          type="button"
          onClick={onDismiss}
          className="shrink-0 rounded p-0.5 opacity-70 transition hover:opacity-100"
          aria-label="Dismiss"
        >
          <svg className="h-4 w-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
            <path d="M6 18L18 6M6 6l12 12" strokeLinecap="round" />
          </svg>
        </button>
      </div>
    </div>
  );
}
