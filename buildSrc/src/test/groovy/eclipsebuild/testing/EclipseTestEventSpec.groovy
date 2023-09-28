package eclipsebuild.testing

import spock.lang.Specification

class EclipseTestEventSpec extends Specification {

    def "TestTreeEntry root"() {
        given:
        def testTreeEntry = new EclipseTestEvent.TestTreeEntry("2,GradleImportTaskTest,true,5,false,1,GradleImportTaskTest,,$COMMON")

        expect:
        testTreeEntry.isSpock()
        testTreeEntry.spockSpec == "org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest"
        testTreeEntry.spockFeature == null
        testTreeEntry.spockIteration == null
    }

    def "TestTreeEntry with feature"() {
        given:
        def testTreeEntry = new EclipseTestEvent.TestTreeEntry("3,Imports project into workspace,false,1,false,2,Imports project into workspace,,$COMMON/[feature:\$spock_feature_2_0]")

        expect:
        testTreeEntry.isSpock()
        testTreeEntry.spockSpec == "org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest"
        testTreeEntry.spockFeature == "\$spock_feature_2_0"
    }

    def "TestTreeEntry with feature and iteration"() {
        given:
        def testTreeEntry = new EclipseTestEvent.TestTreeEntry("$COMMON/[feature:\$spock_feature_2_4]/[iteration:3]")

        expect:
        testTreeEntry.isSpock()
        testTreeEntry.spockSpec == "org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest"
        testTreeEntry.spockFeature == "\$spock_feature_2_4"
        testTreeEntry.spockIteration == "3"
    }

    final static String COMMON = "[engine:spock]/[spec:org.eclipse.buildship.oomph.internal.test.GradleImportTaskTest]"

    def "TestTreeEntry all known"(description) {
        given:
        def testTreeEntry = new EclipseTestEvent.TestTreeEntry(description)

        expect:
        testTreeEntry.isSpock()

        where:
        description                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        | _
        "2,GradleImportTaskTest,true,5,false,1,GradleImportTaskTest,,$COMMON"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              | _
        "3,Imports project into workspace,false,1,false,2,Imports project into workspace,,$COMMON/[feature:\$spock_feature_2_0]"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | _
        "4,Manual trigger causes synchronization,false,1,false,2,Manual trigger causes synchronization,,$COMMON/[feature:\$spock_feature_2_1]"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                             | _
        "5,Startup trigger does not synchronize existing projects,false,1,false,2,Startup trigger does not synchronize existing projects,,$COMMON/[feature:\$spock_feature_2_2]"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | _
        "6,Startup trigger imports projects that are not already in the workspace,false,1,false,2,Startup trigger imports projects that are not already in the workspace,,$COMMON/[feature:\$spock_feature_2_3]"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                           | _
        "7,new build configuration can override workspace settings,false,1,false,2,new build configuration can override workspace settings,,$COMMON/[feature:\$spock_feature_2_4]"                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                         | _
        "8,new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(WRAPPER)\\, distributionType: GRADLE_WRAPPER\\, offlineMode: false\\, buildScansEnabled: false\\, autoSync: true\\, showConsole: false\\, showExecutions: true\\, customGradleHome: false\\, #0],false,1,true,7,new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(WRAPPER)\\, distributionType: GRADLE_WRAPPER\\, offlineMode: false\\, buildScansEnabled: false\\, autoSync: true\\, showConsole: false\\, showExecutions: true\\, customGradleHome: false\\, #0],,$COMMON/[feature:\$spock_feature_2_4]/[iteration:0]"                                                                                                                                                                                                                    | _
        "9,new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(https://services.gradle.org/distributions/gradle-5.4-bin.zip))\\, distributionType: REMOTE_DISTRIBUTION\\, offlineMode: false\\, buildScansEnabled: false\\, autoSync: true\\, showConsole: false\\, showExecutions: true\\, customGradleHome: false\\, #1],false,1,true,7,new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(REMOTE_DISTRIBUTION(https://services.gradle.org/distributions/gradle-5.4-bin.zip))\\, distributionType: REMOTE_DISTRIBUTION\\, offlineMode: false\\, buildScansEnabled: false\\, autoSync: true\\, showConsole: false\\, showExecutions: true\\, customGradleHome: false\\, #1],,$COMMON/[feature:\$spock_feature_2_4]/[iteration:1]"                                                      | _
        "10,new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(VERSION(5.4.1))\\, distributionType: SPECIFIC_GRADLE_VERSION\\, offlineMode: false\\, buildScansEnabled: true\\, autoSync: false\\, showConsole: true\\, showExecutions: false\\, customGradleHome: false\\, #2],false,1,true,7,new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(VERSION(5.4.1))\\, distributionType: SPECIFIC_GRADLE_VERSION\\, offlineMode: false\\, buildScansEnabled: true\\, autoSync: false\\, showConsole: true\\, showExecutions: false\\, customGradleHome: false\\, #2],,$COMMON/[feature:\$spock_feature_2_4]/[iteration:2]"                                                                                                                                                                                   | _
        "11,new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(/Users/user/.gradle/wrapper/dists/gradle-5.4.1-bin/e75iq110yv9r9wt1a6619x2xm/gradle-5.4.1))\\, distributionType: LOCAL_INSTALLATION\\, offlineMode: false\\, buildScansEnabled: true\\, autoSync: false\\, showConsole: true\\, showExecutions: false\\, customGradleHome: true\\, #3],false,1,true,7,new build configuration can override workspace settings [distribution: GRADLE_DISTRIBUTION(LOCAL_INSTALLATION(/Users/user/.gradle/wrapper/dists/gradle-5.4.1-bin/e75iq110yv9r9wt1a6619x2xm/gradle-5.4.1))\\, distributionType: LOCAL_INSTALLATION\\, offlineMode: false\\, buildScansEnabled: true\\, autoSync: false\\, showConsole: true\\, showExecutions: false\\, customGradleHome: true\\, #3],,$COMMON/[feature:\$spock_feature_2_4]/[iteration:3]" | _
    }


}
