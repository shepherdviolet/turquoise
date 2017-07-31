# Exclude Dependencies

```gradle
    repositories {
        maven { url 'https://jitpack.io' }
        //Deprecated way, use jitpack.io instead, just for reference
        //maven { url 'https://github.com/shepherdviolet/alternate-maven-repository/raw/master/repository' }
    }
    dependencies {
        compile ('com.github.shepherdviolet.turquoise:turquoise:6.0') {
            exclude group:'com.android.support', module:'support-v4'
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support', module:'appcompat-v7'
            exclude group:'com.android.support', module:'recyclerview-v7'
            exclude group:'com.squareup.okhttp3', module:'okhttp'
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:6.0') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
        }
    }
```

# Disable transitive

```gradle
    repositories {
        maven { url 'https://jitpack.io' }
        //Deprecated way, use jitpack.io instead, just for reference
        //maven { url 'https://github.com/shepherdviolet/alternate-maven-repository/raw/master/repository' }
    }
    dependencies {
        //thistle is required, if turquoise transitive = false
        compile ('com.github.shepherdviolet:thistle:6.0') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise:6.0') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:6.0') {
            transitive = false
        }
    }
```
