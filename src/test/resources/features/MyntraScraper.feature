Feature: Myntra Product Scraper

  Scenario: Scrape Van Heusen T-Shirts
    Given User opens the Myntra website
    When User navigates to "men" category and "tshirts" section
    Then validate that user is on "Men T-Shirts" page
    When User apply "Van Heusen" brand filter
    Then validate that "Van Heusen" filter is applied
    And User scrapes product data and prints them
    And close browser

  Scenario: Scrape Roadster Casual Shirts
    Given User opens the Myntra website
    When User navigates to "men" category and "casual-shirts" section
    Then validate that user is on "Casual Shirts For Men" page
    When User apply "Roadster" brand filter
    Then validate that "Roadster" filter is applied
    And User scrapes product data and prints them
    And close browser

  Scenario: Attempt to apply an invalid brand filter
    Given User opens the Myntra website
    When User navigates to "men" category and "tshirts" section
    And validate that user is on "Men T-Shirts" page
    And User apply "XYZ123Brand" brand filter
    And close browser
