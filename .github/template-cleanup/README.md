# %NAME%

This template follows hexagonal architecture structure. 

## Workflow
**Kotlin Project Template** is a particular type of GitHub repository that lets you speed up the setup phase and start writing your solutions immediately.

The general idea is straightforward – to create a new project based on this template, you need to log in to your GitHub account and use the **Use this template** green button.
And remember – **do not fork it!**

After creating a new project based on this template in your account, a dedicated GitHub Actions workflow will start and clean up the code from redundant files.
It will also personalize code to use your username and project name in namespaces and Gradle properties.
How cool is that?

Right after the [@actions-user][actions-user] actor pushes the second commit to your repository, you're ready to clone it within the IntelliJ IDEA.

From now, everything's in your hands!

## Content

After you create a new project based on the current template repository using the **Use this template** button, a bare minimal scaffold will appear in your GitHub account with the following structure:

```
.
├── README.md
├── build.gradle.kts
├── buildSrc
│   ├── build.gradle.kts
│   ├── gradle.properties -> ../gradle.properties
│   └── src
│       └── main
│           └── kotlin
│               ├── kotlin-adapter-conventions.gradle.kts
│               ├── kotlin-common-conventions.gradle.kts
│               └── kotlin-domain-conventions.gradle.kts
├── config
│   └── detekt
├── documentation
│   ├── ADR
│   │   └── ADRxxxx-Template.md
│   ├── architecture.png
│   ├── http-client.env.json
│   └── request.http
├── domain
├── gradle
│   └── wrapper
├── gradle.properties
├── gradlew
├── gradlew.bat
├── infrastructure
│   └── example-adapter
├── presentation
│   └── web
└── settings.gradle.kts
```

## Frameworks used

- [Http4k Web Framework][http4k]
- [result4k Library][result4k]
- [JUnit Testing Framework][junit]
- [Kotest Assertion Library][kotest]
- [Mockk Mocking Library][mockk]

## Getting help

If you stuck with Kotlin-specific questions or anything related to this template, check out the following resources:

- [Kotlin docs][docs]
- [Kotlin Slack][slack]

[actions-user]: https://github.com/actions-user
[docs]: https://kotlinlang.org/docs/home.html
[kotlin]: https://kotlinlang.org
[slack]: https://surveys.jetbrains.com/s3/kotlin-slack-sign-up
[http4k]: http://http4k.org
[result4k]: https://github.com/fork-handles/forkhandles
[junit]: https://junit.org/junit5/
[kotest]: https://kotest.io
[mockk]: https://mockk.io
