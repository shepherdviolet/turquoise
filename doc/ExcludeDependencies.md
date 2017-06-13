# Exclude Dependencies

```gradle
    repositories {
        maven { url 'https://github.com/shepherdviolet/alternate-maven-repository/raw/master/repository' }
    }
    dependencies {
        compile ('sviolet:turquoise:5.2') {
            exclude group:'com.android.support', module:'support-v4'
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
            exclude group:'com.android.support', module:'appcompat-v7'
            exclude group:'com.android.support', module:'recyclerview-v7'
            exclude group:'com.squareup.okhttp3', module:'okhttp'
        }
        compile ('sviolet:turquoise-imageloader:5.2') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
        }
        compile ('sviolet:turquoise-imageloader-plugin:5.2') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
        }
        compile ('sviolet:turquoise-multidex:5.2') {
            exclude group:'org.jetbrains.kotlin', module:'kotlin-stdlib-jre7'
        }
    }
```

# Disable transitive

```gradle
    repositories {
        maven { url 'https://github.com/shepherdviolet/alternate-maven-repository/raw/master/repository' }
    }
    dependencies {
        compile ('sviolet:turquoise:5.2') {
            transitive = false
        }
        compile ('sviolet:turquoise-imageloader:5.2') {
            transitive = false
        }
        compile ('sviolet:turquoise-imageloader-plugin:5.2') {
            transitive = false
        }
        compile ('sviolet:turquoise-multidex:5.2') {
            transitive = false
        }
    }
```
