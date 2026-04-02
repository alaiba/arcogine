use criterion::{criterion_group, criterion_main, Criterion};

fn scheduler_benchmark(_c: &mut Criterion) {
    // Phase 6: benchmark event scheduler throughput.
}

criterion_group!(benches, scheduler_benchmark);
criterion_main!(benches);
