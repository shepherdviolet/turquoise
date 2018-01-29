# Exclude Dependencies

```gradle
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }//repository of turquoise
        google()
    }
    dependencies {
        compile ('com.github.shepherdviolet.turquoise:turquoise:8.0') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support'
            exclude group:'com.squareup.okhttp3'
            exclude group:'com.google.zxing'
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:8.0') {
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
        google()
    }
    dependencies {
        //thistle is required, if turquoise transitive = false
        compile ('com.github.shepherdviolet:thistle:8.0') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise:8.0') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:8.0') {
            transitive = false
        }
    }
```
