Feature: Myntra Product Scraper

  Scenario: Scrape Van Heusen T-Shirts
    Given User open the Myntra website
    When User navigate to "Men T-Shirts" section
    And User apply "Van Heusen" brand filter
    Then User scrape product data

  Scenario: Scrape Roadster Casual Shirts
    Given User open the Myntra website
    When User navigate to "Men Casual Shirts" section
    And User apply "Roadster" brand filter
    Then User scrape product data

  Scenario: Attempt to apply an invalid brand filter
    Given User open the Myntra website
    When User navigate to "Men T-Shirts" section
    And User apply "XYZ123Brand" brand filter
    Then User should see an error message "Brand not found"
