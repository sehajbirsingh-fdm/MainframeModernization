# Code Review Checklist - CRECUST

## Architecture
- [ ] Controller is thin; no business logic in controller.
- [ ] Service orchestrates rule order exactly as spec.
- [ ] Repository interfaces isolate persistence and control-state concerns.
- [ ] Credit-check logic is isolated behind gateway interface.

## Rule Fidelity
- [ ] Title allowlist logic implemented exactly.
- [ ] DOB rule set implemented exactly.
- [ ] Customer-number generation is monotonic by sortcode.
- [ ] Legacy status mapping present on success and failures.

## Safety and Quality
- [ ] No live DB2/CICS integration introduced.
- [ ] Integer date to ISO conversion is correct and tested.
- [ ] Error envelope conforms to contract.
- [ ] Correlation ID present in responses and logs.
