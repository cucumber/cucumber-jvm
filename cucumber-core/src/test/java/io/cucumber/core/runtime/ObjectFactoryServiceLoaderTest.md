# Testcases for `ObjectFactoryServiceLoader`

| # | object-factory property | Available services                                    | Result                                                                           |
|---|-------------------------|-------------------------------------------------------|----------------------------------------------------------------------------------|
| 1 | undefined               | none                                                  | exception, no generators available                                               |
| 2 | undefined               | DefaultObjectFactory                                  | DefaultObjectFactory used                                                        |
| 3 | DefaultObjectFactory    | DefaultObjectFactory                                  | DefaultObjectFactory used                                                        |
| 4 | undefined               | DefaultObjectFactory, OtherFactory                    | OtherFactory used                                                                |
| 5 | DefaultObjectFactory    | DefaultObjectFactory, OtherFactory                    | DefaultObjectFactory used                                                        |
| 6 | undefined               | DefaultObjectFactory, OtherFactory, YetAnotherFactory | exception, cucumber couldn't  decide multiple (non default) generators available |
| 7 | OtherFactory            | DefaultObjectFactory, OtherFactory, YetAnotherFactory | OtherFactory used                                                                |
| 8 | OtherFactory            | DefaultObjectFactory                                  | exception, class not found through SPI                                           |
| 9 | undefined               | OtherFactory                                          | OtherFactory used                                                                |

Essentially this means that
* (2) Cucumber works by default
* (4) When adding a custom implementation to the class path it is used automatically
* When cucumber should not guess (5) or can not guess (7), the property is used to force a choice
