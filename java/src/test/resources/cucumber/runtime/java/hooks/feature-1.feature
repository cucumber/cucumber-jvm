Feature: Use global hooks to start a server and send some requests

  Scenario: Send request to a server to save some data and expect a success response code back
    Given that we have a http server up and running
    When I send a request to save some data
    Then I expect a success response code of 200

  Scenario: Retrieve data saved on the http server
    Given that we have a http server up and running
    When I send a request to get the existing data on http server
    Then I expect to get back some data
