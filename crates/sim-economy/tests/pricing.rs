//! Unit tests for the pricing module.

use sim_economy::pricing::PricingState;

#[test]
fn initial_price_set_correctly() {
    let ps = PricingState::new(10.0);
    assert_eq!(ps.current_price, 10.0);
    assert_eq!(ps.price_history.len(), 1);
}

#[test]
fn price_change_recorded() {
    let mut ps = PricingState::new(10.0);
    ps.set_price(15.0, 50);

    assert_eq!(ps.current_price, 15.0);
    assert_eq!(ps.price_history.len(), 2);
    assert_eq!(ps.price_history[1], (50, 15.0));
}

#[test]
fn multiple_price_changes_tracked() {
    let mut ps = PricingState::new(10.0);
    ps.set_price(12.0, 10);
    ps.set_price(8.0, 20);
    ps.set_price(15.0, 30);

    assert_eq!(ps.current_price, 15.0);
    assert_eq!(ps.price_history.len(), 4);
}
