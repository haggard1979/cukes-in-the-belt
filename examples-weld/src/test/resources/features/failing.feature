Feature: Failures are reported

  Scenario: A failing step
    Given I have 1 cukes in my belly
    Then my belly should contain 2 cukes

  Scenario: An undefined step
    Given I have 1 cukes in my belly
    When I drink a glass of water

  Scenario: A passing scenario
    Given I have 1 cukes in my belly
    Then my belly should contain 1 cukes
