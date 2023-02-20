# GitHub Releaser

Every time you make+push a new git tag, it'll create a GitHub release for you.

## Steps

1. Extract zip into your mod directory (which must be hosted on GitHub).
2. Open `~/.github/workflows/runner.sh` and change `MOD_FOLDER_NAME=My-Mod` to your mod folder name.
3. Commit and push these changes, then create and push a new tag.

Optionally, you may edit "blacklist.txt", which contains regex expressions of files that will NOT be included in the released mod folder (eg psd files)

```text
1.1
- Release message is now your tagged commit's message

1.0 
- Initial commit
```
