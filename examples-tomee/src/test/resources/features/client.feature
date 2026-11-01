Feature: The belly over HTTP

  Scenario: Eating through the servlet
    Given the belly endpoint is empty
    When I post 6 cukes to the belly endpoint
    Then the belly endpoint reports 6 cukes
