# Spotless Formatting Rules

This file documents the custom formatting rules for the ActivityFinderMobile project.

## Current Configuration

### Java Formatting
- **Formatter**: Google Java Format (AOSP style)
- **Version**: 1.19.1
- **Import Order**: android → androidx → com → junit → net → org → java → javax → (blank)
- **Line Length**: Long strings are automatically reflowed
- **Trailing Whitespace**: Removed
- **File Ending**: All files end with newline
- **Unused Imports**: Automatically removed

### XML Formatting
- **Indentation**: 4 spaces
- **Trailing Whitespace**: Removed
- **File Ending**: All files end with newline

### Other Files (Gradle, Markdown, .gitignore)
- **Trailing Whitespace**: Removed
- **File Ending**: All files end with newline

## How to Use

### Check formatting issues:
```bash
./gradlew spotlessCheck
```

### Auto-format all files:
```bash
./gradlew spotlessApply
```

### Format only Java files:
```bash
./gradlew spotlessJavaApply
```

## Customizing Rules

You can customize the formatting rules in `app/build.gradle` in the `spotless {}` block.

### Common Customizations:

#### Change line length:
```groovy
googleJavaFormat('1.19.1').aosp().reflowLongStrings().maxLineLength(120)
```

#### Change indentation style:
```groovy
indentWithTabs()  // Instead of spaces
indentWithSpaces(2)  // Use 2 spaces instead of 4
```

#### Add license headers:
```groovy
licenseHeader '/* Copyright (C) 2024 - All Rights Reserved */'
```

#### Custom import order:
```groovy
importOrder('java', 'javax', '', 'android', 'androidx', '\\#')
```

#### Replace Google Java Format with Eclipse formatter:
```groovy
eclipse('4.26').configFile('eclipse-formatter.xml')
```

## Pre-commit Hook (Optional)

To automatically format files before commit, create `.git/hooks/pre-commit`:

```bash
#!/bin/sh
./gradlew spotlessApply
git add -u
```

Then make it executable:
```bash
chmod +x .git/hooks/pre-commit
```

## CI/CD Integration

Add this to your CI pipeline to ensure code is properly formatted:

```yaml
- name: Check code formatting
  run: ./gradlew spotlessCheck
```
