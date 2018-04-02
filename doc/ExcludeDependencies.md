# Exclude Dependencies

```gradle
    repositories {
        jcenter()
        maven { url 'https://jitpack.io' }//repository of turquoise
        google()
    }
    dependencies {
        compile ('com.github.shepherdviolet.turquoise:turquoise:9.0') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support'
            exclude group:'com.squareup.okhttp3'
            exclude group:'com.google.zxing'
            exclude group:'com.google.code.gson'
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:9.0') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support'
            exclude group:'com.squareup.okhttp3'
            exclude group:'com.google.zxing'
            exclude group:'com.google.code.gson'
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
        compile ('com.github.shepherdviolet:thistle:9.0') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise:9.0') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:9.0') {
            transitive = false
        }
    }
```
