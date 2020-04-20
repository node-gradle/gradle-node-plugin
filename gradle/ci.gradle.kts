extra.properties["isMaster"] = System.getenv()["TRAVIS_BRANCH"] == "master"

tasks.register("ci") {
    dependsOn("build")
    description = "Continuous integration tasks"
    group = "Build"
}
