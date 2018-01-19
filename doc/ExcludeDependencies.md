# Exclude Dependencies

```gradle
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }//repository of turquoise
    }
    dependencies {
        compile ('com.github.shepherdviolet.turquoise:turquoise:7.1') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support'
            exclude group:'com.squareup.okhttp3'
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:7.1') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support'
            exclude group:'com.squareup.okhttp3'
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
        compile ('com.github.shepherdviolet:thistle:7.1') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise:7.1') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:7.1') {
            transitive = false
        }
    }
```
