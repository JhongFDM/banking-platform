Feature: As a new user, I want to register an account so that I can access the platform.

@current
Scenario: User should be able to register a personal account with valid details
    Given the user is on the 'registration' page
    When I select 'personal' account and click continue

@current
Scenario: User should be able to register a business account with valid details
    Given the user is on the 'registration' page
    When I select 'business' account and click continue


#   Given I am on the registration page
#   When I fill in the registration form with valid details
#   And I submit the form
#   Then I should see a confirmation message indicating successful registration

# Scenario: User should see validation errors when registering a personal account with invalid details
#   Given I am on the registration page
#   When I fill in the registration form with missing details
#   And I submit the form
#   Then I should see an appropriate validation error messages

  