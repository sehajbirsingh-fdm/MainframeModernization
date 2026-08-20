# Code Review Checklist - 006 Bank Statement Retrieval

Authority:
- legacy-bankofz/base/batch/pli/BNKSTMT.pli
- legacy-bankofz/base/batch/jcl/BNKSTMT.jcl
- spec.md
- supporting/mapping-matrix.md
- supporting/test-spec.md

- [ ] Endpoint contract matches statement retrieval scope.
- [ ] Period parsing and boundary logic align with BNKSTMT semantics.
- [ ] Service owns statement assembly logic; controller is thin.
- [ ] Auth/authz and 401/403 behavior are enforced.
- [ ] Error envelope consistency is preserved.
- [ ] Tests cover positive and negative paths.
