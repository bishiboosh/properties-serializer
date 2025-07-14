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

## Difference with the `Properties` format distributed by KotlinX Serialization

As you may have noticed, the implementation and API surface of this library is very similar to the
one provided by KotlinX Serialization in the [`kotlinx.serialization.properties` package](https://kotlinlang.org/api/kotlinx.serialization/kotlinx-serialization-properties/kotlinx.serialization.properties/-properties/).

However, where the KotlinX Serialization implementation just serializes and deserializes from and to
a `Map`, this library actually takes the step to write to or read from an actual data source, which is
way more useful for you if you're dealing with configuration files using the Java Properties format.

This library is primarily intended for use in Kotlin Multiplatform projects, where the `Properties` 
class from Java cannot be used direcly. If your project is JVM-only, using the KotlinX Serialization
library and converting the `Map` to a `Properties` object is probably the better option.

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