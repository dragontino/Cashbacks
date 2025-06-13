pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Cashbacks"
include(":app")
include(":common:utils")
include(":common:composables")
include(":common:resources")
include(":common:navigation")
include(":core:database")
include(":features:settings:domain")
include(":features:settings:data")
include(":features:settings:presentation")
include(":features:bankcard:domain")
include(":features:bankcard:data")
include(":features:bankcard:presentation:api")
include(":features:bankcard:presentation:impl")
include(":features:cashback:domain")
include(":features:cashback:data")
include(":features:cashback:presentation:api")
include(":features:cashback:presentation:impl")
include(":features:shop:domain")
include(":features:shop:data")
include(":features:shop:presentation:api")
include(":features:shop:presentation:impl")
include(":features:category:domain")
include(":features:category:data")
include(":features:category:presentation:api")
include(":features:category:presentation:impl")
include(":features:home:api")
include(":features:home:impl")
include(":features:share:domain")
include(":features:share:data")
