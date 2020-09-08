# Release Checklist

## Conditions

- [ ] All Tests Passing in the `master` branch

## Preparation

- [ ] Checkout the `master` branch
- [ ] Add a link to this new version to the Release History in the `README.md` file and mark it as the current one
- [ ] Add the version and its changes to the `CHANGELOG.md` file
- [ ] Replace all the occurrences of the former latest version by this version in all the docs files
- [ ] Commit these changes and Push this Commit

## Release

- [ ] Create and push an **annotated** tag from the commit created just before thanks to the command:
 `git tag -a 3.0.0 -m "Set version to 3.0.0"`

A GitHub Actions workflow will trigger automatically. It will build and deploy the plugin to the Gradle plugins portal.

## After Plugin Publication

- [ ] Paste the changelog of this version to the new GitHub release created automatically from the tag
