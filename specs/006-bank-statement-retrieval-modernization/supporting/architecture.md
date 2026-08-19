# Architecture - 006 Bank Statement Retrieval

Client -> Statement Controller -> Statement Service -> Statement Repository -> Local persistence adapter

- Controller validates path inputs and security context.
- Service computes period boundaries and assembles statement response.
- Repository retrieves account and period transactions.
- No mutation operations are allowed.
