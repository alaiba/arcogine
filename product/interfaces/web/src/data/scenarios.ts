export const SCENARIO_BASIC = `[simulation]
rng_seed = 42
max_ticks = 500
demand_eval_interval = 10
agent_eval_interval = 50

[[equipment]]
id = 1
name = "Mill"

[[equipment]]
id = 2
name = "Lathe"

[[equipment]]
id = 3
name = "QC"

[[material]]
id = 1
name = "Widget A"
routing_id = 1

[[material]]
id = 2
name = "Widget B"
routing_id = 2

[[process_segment]]
id = 1
name = "Rough milling"
equipment_id = 1
duration = 5

[[process_segment]]
id = 2
name = "Turning"
equipment_id = 2
duration = 3

[[process_segment]]
id = 3
name = "Quality check"
equipment_id = 3
duration = 2

[[process_segment]]
id = 4
name = "Fine milling"
equipment_id = 1
duration = 4

[[operations_definition]]
id = 1
name = "Widget A routing"
steps = [1, 2, 3]

[[operations_definition]]
id = 2
name = "Widget B routing"
steps = [4, 3]

[economy]
initial_price = 10.0
base_demand = 3.0
price_elasticity = 0.3
lead_time_sensitivity = 0.05
`;

export const SCENARIO_OVERLOAD = `[simulation]
rng_seed = 42
max_ticks = 500
demand_eval_interval = 10
agent_eval_interval = 50

[[equipment]]
id = 1
name = "Mill"

[[equipment]]
id = 2
name = "Lathe"

[[equipment]]
id = 3
name = "QC"

[[material]]
id = 1
name = "Widget A"
routing_id = 1

[[material]]
id = 2
name = "Widget B"
routing_id = 2

[[process_segment]]
id = 1
name = "Rough milling"
equipment_id = 1
duration = 5

[[process_segment]]
id = 2
name = "Turning"
equipment_id = 2
duration = 3

[[process_segment]]
id = 3
name = "Quality check"
equipment_id = 3
duration = 2

[[process_segment]]
id = 4
name = "Fine milling"
equipment_id = 1
duration = 4

[[operations_definition]]
id = 1
name = "Widget A routing"
steps = [1, 2, 3]

[[operations_definition]]
id = 2
name = "Widget B routing"
steps = [4, 3]

[economy]
initial_price = 2.0
base_demand = 8.0
price_elasticity = 0.3
lead_time_sensitivity = 0.05
`;

export const SCENARIO_CAPACITY_EXPANSION = `[simulation]
rng_seed = 42
max_ticks = 500
demand_eval_interval = 10
agent_eval_interval = 50

[[equipment]]
id = 1
name = "Mill-1"

[[equipment]]
id = 2
name = "Mill-2"

[[equipment]]
id = 3
name = "Lathe"

[[equipment]]
id = 4
name = "QC"

[[equipment]]
id = 5
name = "Mill-3"

[[material]]
id = 1
name = "Widget A"
routing_id = 1

[[material]]
id = 2
name = "Widget B"
routing_id = 2

[[process_segment]]
id = 1
name = "Milling (line 1)"
equipment_id = 1
duration = 6

[[process_segment]]
id = 2
name = "Milling (line 2)"
equipment_id = 2
duration = 6

[[process_segment]]
id = 3
name = "Milling (line 3)"
equipment_id = 5
duration = 6

[[process_segment]]
id = 4
name = "Turning"
equipment_id = 3
duration = 4

[[process_segment]]
id = 5
name = "Quality check"
equipment_id = 4
duration = 2

[[operations_definition]]
id = 1
name = "Widget A routing"
steps = [1, 4, 5]

[[operations_definition]]
id = 2
name = "Widget B routing"
steps = [2, 4, 5]

[economy]
initial_price = 2.0
base_demand = 8.0
price_elasticity = 0.2
lead_time_sensitivity = 0.02
`;

export const SCENARIOS = {
  basic: { label: 'Basic', toml: SCENARIO_BASIC },
  overload: { label: 'Overload', toml: SCENARIO_OVERLOAD },
  capacity_expansion: {
    label: 'Capacity Expansion',
    toml: SCENARIO_CAPACITY_EXPANSION,
  },
} as const;

export type ScenarioKey = keyof typeof SCENARIOS;
