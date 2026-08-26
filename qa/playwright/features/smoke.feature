Feature: Smoke check
  Scenario: Home page responds
    Given I open the application home page
    Then the page title should not be empty
