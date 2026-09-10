# Setting up your machine

One-time setup. Should take about 30 minutes, most of it downloads.

## 1. Install Android Studio

Get the latest stable build from
<https://developer.android.com/studio>. It bundles its own JDK, so you do
**not** need to install Java separately.

On first launch, accept the SDK licences and let it download the default
components.

## 2. Accept the repo invite

Check the email for `raman5/sanatan` and accept it, or go to
<https://github.com/raman5/sanatan/invitations> while signed in.

## 3. Clone the project

In Android Studio's welcome screen: **Get from VCS** → paste
`https://github.com/raman5/sanatan.git` → pick a folder → **Clone**.

Android Studio will sign you into GitHub in a browser window the first time.
That stores your credentials, so you never deal with access tokens.

From a terminal instead, if you prefer:

```bash
git clone https://github.com/raman5/sanatan.git
cd sanatan
```

## 4. Tell Git who you are

Run these inside the project folder, using the email on your GitHub account
so your commits link to your profile:

```bash
git config user.name "Maneesha"
git config user.email "you@example.com"
```

## 5. Turn on the shared Git hooks

This makes Git refuse an accidental direct push to `main`:

```bash
git config core.hooksPath hooks
```

Do this once per clone. It is not automatic — Git never runs hooks from a
repo without being told to, for security reasons.

Check it works:

```bash
git push origin main
# should print "BLOCKED: direct push to 'main'"
```

## 6. First build

Let Gradle sync finish (progress bar, bottom right). The first sync
downloads Gradle 8.13 and the Android SDK pieces, so expect several minutes.

Then create an emulator: **Tools → Device Manager → Add a new device** →
pick any phone with API 35 → **Finish**. Press **Run** (green triangle).

You should see the Sanatan home screen with five tabs along the bottom.

If Gradle sync fails, copy the error and send it over rather than
hand-editing versions — the versions in `gradle/libs.versions.toml` are
pinned deliberately.

## 7. Optional: Claude Code in the IDE

**Settings → Plugins → Marketplace** → search "Claude Code" → Install →
restart. Requires the `claude` CLI on your PATH first.

Point it at `CONTRIBUTING.md` so it follows the ownership split.

## 8. Your first change

```bash
git checkout main
git pull
git checkout -b feature/your-thing
# ... make a change ...
git add -A
git commit -m "Describe what changed"
git push -u origin feature/your-thing
```

Then open a pull request on GitHub and ask for a review.

Read `CONTRIBUTING.md` before you start real work — it covers who owns
which folders and which four files to announce before touching.
