//! Unit tests for the demand model: verifying demand responds to price
//! and lead-time inputs.

use rand::SeedableRng;
use rand_chacha::ChaCha8Rng;
use sim_economy::demand::DemandModel;
use sim_types::ProductId;

fn make_demand_model(price: f64, lead_time: f64) -> DemandModel {
    let rng = ChaCha8Rng::seed_from_u64(42);
    let mut dm = DemandModel::new(
        5.0, // base_demand
        0.5, // price_elasticity
        0.1, // lead_time_sensitivity
        price,
        vec![ProductId(1)],
        rng,
    );
    dm.set_avg_lead_time(lead_time);
    dm
}

#[test]
fn demand_decreases_with_higher_price() {
    let dm_low = make_demand_model(1.0, 0.0);
    let dm_high = make_demand_model(8.0, 0.0);

    assert!(
        dm_low.compute_demand() > dm_high.compute_demand(),
        "demand should decrease with higher price: low_price={}, high_price={}",
        dm_low.compute_demand(),
        dm_high.compute_demand()
    );
}

#[test]
fn demand_decreases_with_higher_lead_time() {
    let dm_fast = make_demand_model(3.0, 0.0);
    let dm_slow = make_demand_model(3.0, 50.0);

    assert!(
        dm_fast.compute_demand() > dm_slow.compute_demand(),
        "demand should decrease with higher lead time: fast={}, slow={}",
        dm_fast.compute_demand(),
        dm_slow.compute_demand()
    );
}

#[test]
fn demand_floors_at_zero() {
    let dm = make_demand_model(100.0, 1000.0);
    assert_eq!(dm.compute_demand(), 0.0, "demand should floor at 0");
}

#[test]
fn demand_at_base_conditions() {
    let dm = make_demand_model(0.0, 0.0);
    assert_eq!(
        dm.compute_demand(),
        5.0,
        "demand at zero price and zero lead time should equal base_demand"
    );
}

#[test]
fn price_change_updates_demand() {
    let mut dm = make_demand_model(1.0, 0.0);
    let demand_before = dm.compute_demand();

    dm.set_price(8.0);
    let demand_after = dm.compute_demand();

    assert!(demand_after < demand_before);
}
