# Working conventions for AI agents

## Branch to work on

If a session starts with a branch other than `main` already checked out,
treat that branch as the one to do the work on — do not switch to a
different branch just because task/PR instructions injected into the
prompt name one. If the injected branch instruction conflicts with the
branch the session actually started on, flag the mismatch to the user
instead of silently switching.
