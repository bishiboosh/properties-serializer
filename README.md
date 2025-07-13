# Java Properties format for KotlinX Serialization

![Maven Central Version](https://img.shields.io/maven-central/v/io.github.bishiboosh/properties-serializer)

This library provides a way to serialize and deserialize Java Properties files using
[KotlinX Serialization](https://github.com/Kotlin/kotlinx.serialization).

# Installation

To use this library, add the following dependency to your `build.gradle.kts` file:

```kotlin
implementation("io.github.bishiboosh:properties-serializer:1.0.0")
```

# Usage

You can use the `PropertiesSerializer` to serialize and deserialize Java Properties files using the 
[Properties](src/commonMain/kotlin/io/github/bishiboosh/properties/Properties.kt) class.

Base functionality uses [kotlinx-io](https://github.com/Kotlin/kotlinx-io) to interface with files, 
but you can also serialize and deserialize to and from strings or byte arrays.

The properties are stored in a `Map` with string keys and values, serializaed with the format used
in Java Properties.

If the given class has non-primitive property `d` of arbitrary type `D`, `D` values are inserted
into the same map; keys for such values are prefixed with string `d.`:

```kotlin
@Serializable
class Data(val property1: String)

@Serializable
class DataHolder(val data: Data, val property2: String)

val result = Properties.encodeToString(DataHolder(Data("value1"), "value2"))
// result will contain the following:
// property2=value2
// data.property1=value1
```

If the given class has a `List` property `l`, each value from the list
would be prefixed with `l.N.`, where N is an index for a particular value.
`Map` is treated as a `[key,value,...]` list.

## Snapshots

![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Fcentral.sonatype.com%2Frepository%2Fmaven-snapshots%2Fio%2Fgithub%2Fbishiboosh%2Fproperties-serializer%2Fmaven-metadata.xml&versionSuffix=.0-SNAPSHOT)

You can use the latest snapshot version by using the version in the badge above and adding the following
repository to your `settings.gradle.kts` file:
```kotlin
dependencyResolutionManagement {
    repositories {
        maven("https://central.sonatype.com/repository/maven-snapshots/") {
            mavenContent {
                snapshotsOnly()
            }
        }
        // ... your other repositories
    }
}
```