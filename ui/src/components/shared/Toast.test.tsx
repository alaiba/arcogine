import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest';
import { render, screen, fireEvent, act } from '@testing-library/react';
import { Toast } from './Toast';

describe('Toast', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders message text', () => {
    render(<Toast message="Something happened" type="info" onDismiss={() => {}} />);
    expect(screen.getByText('Something happened')).toBeInTheDocument();
  });

  it('auto-dismisses after timeout', () => {
    const onDismiss = vi.fn();
    render(<Toast message="Auto" type="error" onDismiss={onDismiss} />);
    expect(onDismiss).not.toHaveBeenCalled();

    act(() => {
      vi.advanceTimersByTime(5000);
    });
    expect(onDismiss).toHaveBeenCalledTimes(1);
  });

  it('manual dismiss calls onDismiss', () => {
    const onDismiss = vi.fn();
    render(<Toast message="Manual" type="success" onDismiss={onDismiss} />);
    fireEvent.click(screen.getByLabelText('Dismiss'));
    expect(onDismiss).toHaveBeenCalledTimes(1);
  });
});
