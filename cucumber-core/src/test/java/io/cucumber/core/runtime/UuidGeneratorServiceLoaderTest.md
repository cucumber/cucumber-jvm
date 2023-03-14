# Testcases for `UuidGeneratorServiceLoader`

| #   | uuid-generator property   | Available services                                                                  | Result                                                                           |
|-----|---------------------------|-------------------------------------------------------------------------------------|----------------------------------------------------------------------------------|
| 1   | undefined                 | none                                                                                | exception, no generators available                                               |
| 2   | undefined                 | RandomUuidGenerator, IncrementingUuidGenerator                                      | RandomUuidGenerator used                                                         |
| 3   | RandomUuidGenerator       | RandomUuidGenerator, IncrementingUuidGenerator                                      | RandomUuidGenerator used                                                         |
| 4   | undefined                 | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator                      | OtherGenerator used                                                              |
| 5   | RandomUuidGenerator       | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator                      | RandomUuidGenerator used                                                         |
| 6   | undefined                 | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator, YetAnotherGenerator | exception, cucumber couldn't  decide multiple (non default) generators available |
| 7   | OtherGenerator            | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator, YetAnotherGenerator | OtherGenerator used                                                              |
| 8   | IncrementingUuidGenerator | RandomUuidGenerator, IncrementingUuidGenerator, OtherGenerator, YetAnotherGenerator | IncrementingUuidGenerator used                                                   |
| 9   | IncrementingUuidGenerator | RandomUuidGenerator, IncrementingUuidGenerator                                      | IncrementingUuidGenerator used                                                   |
| 10  | OtherGenerator            | none                                                                                | exception, generator OtherGenerator not available                                |
| 11  | undefined                 | OtherGenerator                                                                      | OtherGenerator used                                                              |
| 12  | undefined                 | IncrementingUuidGenerator, OtherGenerator                                           | OtherGenerator used                                                              |
| 13  | undefined                 | IncrementingUuidGenerator                                                           | IncrementingUuidGenerator used                                                   |
