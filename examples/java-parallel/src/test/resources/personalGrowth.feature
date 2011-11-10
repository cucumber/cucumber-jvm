Feature: Personal growth
        As a person
        I would like to be able to grow
        So that I reach a normal average height

    Scenario: Growth of infant
        Given a person of 0 feet
        When the person grows by 1 feet
        Then the person will be 1 feet tall
    
    Scenario: Growth of adult
        Given a person of 5 feet
        When the person grows by 1 feet
        Then the person will be 6 feet tall

