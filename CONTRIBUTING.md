# How we work on this repo

Two developers, one codebase. These rules exist so we almost never hit a
merge conflict, and so `main` always builds.

> **Heads up, Raman:** this repo was repurposed from the Sanatan astrology
> app to the Bhakti devotional-content app per the product PRD, on branch
> `feature/bhakti-mvp`. `feature/kundli/` and `feature/astrologer/` were
> removed (still in git history) since they're outside the Bhakti scope.
> Please review before this merges to `main` - talk to Maneesha about
> whether/where astrology fits the new direction.

## The loop

```bash
git checkout main
git pull                            # start from what's on GitHub
git checkout -b feature/mantra-list  # your own branch, named after the work
# ... write code, commit as you go ...
git push -u origin feature/mantra-list
```

Then open a Pull Request on GitHub. The other person reviews and merges.
Delete the branch after merge.

**Nobody commits directly to `main`.** On GitHub, turn on
Settings > Branches > Add rule > `main` > "Require a pull request before
merging". That makes the rule real instead of a promise.

## Branch names

| Prefix | For |
|---|---|
| `feature/` | new work |
| `fix/` | bug fixes |
| `chore/` | dependency bumps, config, cleanup |

## Who owns what

Split by folder, so we rarely open the same file. Post-pivot, this table
needs a fresh split between the two of you - the rows below are a starting
point, not a final answer.

| Area | Owner |
|---|---|
| `feature/home/`, `feature/splash/`, `feature/auth/`, `feature/paywall/`, `feature/payment/` | Maneesha |
| `feature/explore/`, `feature/search/`, `feature/favourites/`, `feature/profile/` | Raman |
| `data/model/`, `data/repository/`, `core/session/`, `core/di/` | Shared - coordinate before editing |
| `notifications/` | Raman |

Swap any of these whenever you like - just update this table in the same PR.

## Shared files: announce before you touch them

These four are the only realistic sources of a painful conflict. Message the
other person before editing, and merge that PR the same day:

- `app/src/main/AndroidManifest.xml`
- `gradle/libs.versions.toml`
- `core/navigation/Destinations.kt`
- `ui/theme/` (Color, Type, Theme)

## Rules that actually prevent conflicts

1. **Pull every morning.** A branch that is three days behind `main` is where
   conflicts come from.
2. **Small PRs.** One screen or one fix. A 40-file PR is unreviewable and
   conflicts with everything.
3. **Rebase, don't let branches rot:** `git fetch origin && git rebase origin/main`
4. **Never commit `local.properties`, `build/`, `.idea/` or any `.jks`.**
   `.gitignore` handles this - don't force-add past it.
5. **If `main` is broken, fixing it beats whatever else you were doing.**

## Commit messages

One line, present tense, says what changed:

```
Add japa counter to mantra detail screen
Fix lagna calculation for southern latitudes
Bump navigation-compose to 2.8.4
```

## Pair programming

For debugging together in real time, use Android Studio's **Code With Me**
(Tools > Code With Me). One of you hosts, the other joins by link and edits
the same files live. Use it for hard bugs - Git stays the source of truth.

## Enable the push guard (once per clone)

```bash
git config core.hooksPath hooks
```

GitHub Free cannot enforce branch protection on a private repo, so
`hooks/pre-push` does it locally instead: it refuses a direct push to `main`
and tells you to use a branch. Both of us need to run the command above -
Git will not use a repo's hooks until you point it at them.

To override deliberately (rare, and say so in chat first):

```bash
ALLOW_PUSH_MAIN=1 git push origin main
```
