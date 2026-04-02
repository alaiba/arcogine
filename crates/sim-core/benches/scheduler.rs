use criterion::{black_box, criterion_group, criterion_main, Criterion};
use sim_core::event::{Event, EventPayload};
use sim_core::queue::Scheduler;
use sim_types::{ProductId, SimTime};

fn bench_schedule_and_dequeue(c: &mut Criterion) {
    c.bench_function("schedule_1000_events", |b| {
        b.iter(|| {
            let mut scheduler = Scheduler::new();
            for i in 0..1000u64 {
                scheduler
                    .schedule(Event::new(
                        SimTime(i),
                        EventPayload::OrderCreation {
                            product_id: ProductId(1),
                            quantity: 1,
                        },
                    ))
                    .unwrap();
            }
            black_box(&scheduler);
        });
    });

    c.bench_function("dequeue_1000_events", |b| {
        b.iter_with_setup(
            || {
                let mut scheduler = Scheduler::new();
                for i in 0..1000u64 {
                    scheduler
                        .schedule(Event::new(
                            SimTime(i),
                            EventPayload::OrderCreation {
                                product_id: ProductId(1),
                                quantity: 1,
                            },
                        ))
                        .unwrap();
                }
                scheduler
            },
            |mut scheduler| {
                while scheduler.next_event().is_some() {}
                black_box(&scheduler);
            },
        );
    });

    c.bench_function("interleaved_schedule_dequeue", |b| {
        b.iter(|| {
            let mut scheduler = Scheduler::new();
            for i in 0..500u64 {
                scheduler
                    .schedule(Event::new(
                        SimTime(i * 2),
                        EventPayload::OrderCreation {
                            product_id: ProductId(1),
                            quantity: 1,
                        },
                    ))
                    .unwrap();
                if i % 3 == 0 {
                    scheduler.next_event();
                }
            }
            while scheduler.next_event().is_some() {}
            black_box(&scheduler);
        });
    });
}

criterion_group!(benches, bench_schedule_and_dequeue);
criterion_main!(benches);
