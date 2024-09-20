import org.tribot.gradle.plugin.TribotPlugin

dependencies {
    implementation(project(":libraries:my-library"))
}

apply<TribotPlugin>()