Feature: Use before feature and after feature hooks to save/retrieve data from server

  Scenario: No data on the server but we are gone send some in this scenario
    Given that we have a http server up and running and no data on it
    When I send a request to save some data
    Then I expect a success response code of 200

  Scenario: Retrieve data saved on the http server
    Given that we have a http server up and running
    When I send a request to get the existing data on http server
    Then I expect to get back some data
