# kotlin-inject-viewmodel-factory

Viewmodel factory class generator for [kotlin-inject](https://github.com/evant/kotlin-inject)

This generator will create a view model factory that can be used
to instantiate view models annotated with `@Inject`.

Any view model you wish to include in the factory needs to be annotated with `AndroidViewModel`

Follow the ksp setup instructions from [kotlin-inject](https://github.com/evant/kotlin-inject)

Next configure gradle with some required parameters:

```kotlin
dependencies {
    ksp("com.chimerapps.kotlin-inject-viewmodelfactory:compiler:0.1.0")
    
    // Multiplatform is supported as well, just untested. Remove the jvm prefix and it may work in commonMain
    implementation("com.chimerapps.kotlin-inject-viewmodelfactory:runtime-jvm:0.1.0")
}

// Set generator parameters
ksp {
    arg("viewModelFactoryPackage", "<package name to generate the factory in>")
    arg("viewModelFactoryParent", "<fully qualified name of the main component that knows how to create view models>")
}
```

You will then be able to access the factory and create view models.

Example:
```kotlin
    //In some base activity

    override fun getDefaultViewModelProviderFactory(): ViewModelProvider.Factory {
        return object : ViewModelProvider.Factory {

            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return viewModelFactory.createInstance(modelClass.kotlin)
            }

        }
    }
```