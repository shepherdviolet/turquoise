# Exclude Dependencies

```gradle
    repositories {
		//Turquoise in jitpack
        maven { url 'https://jitpack.io' }
        //Thistle in mavenCentral
        mavenCentral()
    }
    dependencies {
        compile ('com.github.shepherdviolet.turquoise:turquoise:10.0') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support'
            exclude group:'com.squareup.okhttp3'
            exclude group:'com.google.zxing'
            exclude group:'com.google.code.gson'
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:10.0') {
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
		//Turquoise in jitpack
        maven { url 'https://jitpack.io' }
        //Thistle in mavenCentral
        mavenCentral()
    }
    dependencies {
        //thistle is required, if turquoise transitive = false
        compile ('com.github.shepherdviolet:thistle:10.0') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise:10.0') {
            transitive = false
        }
        compile ('com.github.shepherdviolet.turquoise:turquoise-imageloader:10.0') {
            transitive = false
        }
    }
```
