import { Component, type ErrorInfo, type ReactNode } from 'react';

type ErrorBoundaryProps = {
  children: ReactNode;
};

type ErrorBoundaryState = {
  error: Error | null;
};

export class ErrorBoundary extends Component<ErrorBoundaryProps, ErrorBoundaryState> {
  state: ErrorBoundaryState = { error: null };

  static getDerivedStateFromError(error: Error): ErrorBoundaryState {
    return { error };
  }

  componentDidCatch(error: Error, info: ErrorInfo): void {
    console.error(error, info.componentStack);
  }

  handleRetry = (): void => {
    this.setState({ error: null });
  };

  render(): ReactNode {
    if (this.state.error) {
      return (
        <div className="flex min-h-[12rem] flex-col items-center justify-center gap-4 rounded-xl border border-red-900/50 bg-red-950/30 p-8 text-center">
          <p className="max-w-md text-sm text-red-200">{this.state.error.message}</p>
          <button
            type="button"
            onClick={this.handleRetry}
            className="rounded-md border border-red-700 bg-red-900/40 px-4 py-2 text-sm font-medium text-red-100 transition hover:bg-red-900/60"
          >
            Retry
          </button>
        </div>
      );
    }
    return this.props.children;
  }
}
