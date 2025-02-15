# Acceptance test data

The compatibility kit uses the examples from the [cucumber compatibility kit](https://github.com/cucumber/compatibility-kit)
for acceptance testing. These examples consist of `.feature` and `.ndjson` files created by
the [`fake-cucumber` reference implementation](https://github.com/cucumber/fake-cucumber).

* The files are copied in by running `npm install`.

* We ensure the `.ndjson` files stay up to date by running `npm install` in CI
and verifying nothing changed.
