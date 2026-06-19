# Third-Party Notices

Private DNS Switch is licensed under the MIT License. The following third-party components may be included in built APKs or committed build helper files.

## Runtime Dependencies

The current `debugRuntimeClasspath` includes:

| Component | Version | License | Notes |
| --- | --- | --- | --- |
| Kotlin Standard Library (`org.jetbrains.kotlin:kotlin-stdlib`) | 2.2.10 | Apache License 2.0 | Pulled in by the Android Gradle Plugin toolchain. |
| JetBrains Annotations (`org.jetbrains:annotations`) | 13.0 | Apache License 2.0 | Transitive dependency of Kotlin Standard Library. |

## Build Helper Files

| Component | Version | License | Notes |
| --- | --- | --- | --- |
| Gradle Wrapper | 9.4.1 | Apache License 2.0 | Wrapper files are committed so the project can be built without a preinstalled Gradle. |

The Apache License 2.0 text is included at [licenses/APACHE-2.0.txt](licenses/APACHE-2.0.txt).
