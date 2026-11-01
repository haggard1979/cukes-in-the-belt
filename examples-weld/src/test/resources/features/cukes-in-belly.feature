Feature: Cukes in my belly

  Scenario: Eating a few cukes
    Given I have 3 cukes in my belly
    When I eat 4 more cukes
    Then my belly should contain 7 cukes

  Scenario Outline: Eating cukes in several rounds
    Given I have <start> cukes in my belly
    When I eat <more> more cukes
    Then my belly should contain <total> cukes

    Examples:
      | start | more | total |
      | 0     | 1    | 1     |
      | 5     | 5    | 10    |

  @ignored
  Scenario: Never executed because of the tag filter
    Given I have 1 cukes in my belly
    Then my belly should contain 1000 cukes
