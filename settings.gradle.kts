rootProject.name = "minecraft-crowd-control"

include(":sponge7-platform")
include(":sponge8-platform")
include(":paper-platform")
include(":common-platform")
project(":sponge7-platform").projectDir = file("sponge-7")
project(":sponge8-platform").projectDir = file("sponge-8")
project(":paper-platform").projectDir = file("paper")
project(":common-platform").projectDir = file("common")
