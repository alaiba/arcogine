type SkeletonLoaderProps = {
  className?: string;
};

export function SkeletonLoader({ className = '' }: SkeletonLoaderProps) {
  return (
    <div
      className={`animate-pulse rounded-md bg-zinc-700/50 dark:bg-zinc-600/40 ${className}`}
      aria-hidden
    />
  );
}
