# Exclude Dependencies

```gradle
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }//repository of turquoise
    }
    dependencies {
        compile ('com.github.shepherdviolet.turquoise:turquoise:6.2') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support'
            exclude group:'com.squareup.okhttp3'
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:6.2') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
        }
    }
```

# Disable transitive

```gradle
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }//repository of turquoise
    }
    dependencies {
        //thistle is required, if turquoise transitive = false
        compile ('com.github.shepherdviolet:thistle:6.2') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise:6.2') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:6.2') {
            transitive = false
        }
    }
```
