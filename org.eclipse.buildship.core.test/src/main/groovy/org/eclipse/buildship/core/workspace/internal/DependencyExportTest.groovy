package org.eclipse.buildship.core.workspace.internal

import com.google.common.base.Predicate
import com.google.common.collect.FluentIterable
import com.gradleware.tooling.toolingclient.GradleDistribution
import org.eclipse.buildship.core.CorePlugin
import org.eclipse.buildship.core.projectimport.ProjectImportConfiguration
import org.eclipse.buildship.core.projectimport.ProjectImportJob
import org.eclipse.buildship.core.test.fixtures.LegacyEclipseSpockTestHelper
import org.eclipse.buildship.core.util.gradle.GradleDistributionWrapper
import org.eclipse.buildship.core.util.progress.AsyncHandler
import org.eclipse.buildship.core.workspace.WorkspaceOperations
import org.eclipse.core.resources.IMarker
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IWorkspaceDescription
import org.eclipse.core.resources.IncrementalProjectBuilder
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.NullProgressMonitor
import org.eclipse.core.runtime.OperationCanceledException
import org.eclipse.core.runtime.jobs.Job
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Ignore
import spock.lang.Shared
import spock.lang.Specification


@Ignore
class DependencyExportTest extends Specification {

    @Shared
    WorkspaceOperations workspaceOperations = CorePlugin.workspaceOperations()

    @Rule
    TemporaryFolder tempFolder

    def cleanup() {
        workspaceOperations.deleteAllProjects(new NullProgressMonitor())
    }

    def "Exported transitive dependencies from project dependency are included"() {
        setup:
        File projectLocation = newMultiProjectWithGuavaDependency(false)
        ProjectImportJob job = newProjectImportJob(projectLocation)

        when:
        job.schedule()
        job.join()

        then:
        def project = workspaceOperations.findProjectByName('dependent-project')
        project.present
        getProblemMarkers(project.get()).length == 0
    }

    def "Excluded transitive dependencies from project dependency with exclusion are not resolved"() {
        setup:
        File projectLocation = newMultiProjectWithGuavaDependency(true)
        ProjectImportJob job = newProjectImportJob(projectLocation)

        when:
        job.schedule()
        job.join()

        then:
        def project = workspaceOperations.findProjectByName('dependent-project')
        project.present

        def problemMarkers = getProblemMarkers(project.get())
        problemMarkers.length == 2 // todo (etst) maybe checking for size >0 is more robust?

        boolean foundCompileError = FluentIterable.of(problemMarkers).anyMatch(new Predicate<IMarker>() {

                    @Override
                    public boolean apply(IMarker marker) {
                        String message = marker.getAttribute(IMarker.MESSAGE, "")
                        return message.contains("The type com.google.common.collect.ImmutableList cannot be resolved");
                    }
                });
        foundCompileError
    }

    def newMultiProjectWithGuavaDependency(boolean excludeGuavaProjectDependency) {
        // >>> create multi-project root
        def rootProject = tempFolder.newFolder('multi-project-withguava')
        new File(rootProject, 'build.gradle') << ''
        new File(rootProject, 'settings.gradle') << '''include "guava-project"
include "dependent-project"'''

        // >>> Create project with guava dependency and code
        def guavaDependentProjectRoot = new File(rootProject, 'guava-project')
        guavaDependentProjectRoot.mkdir()

        new File(guavaDependentProjectRoot, 'settings.gradle') << ''
        new File(guavaDependentProjectRoot, 'build.gradle') << '''apply plugin: 'java'

version = '1.0'

repositories {
    mavenCentral()
}

dependencies {
    compile 'com.google.guava:guava:19.0-rc1'
}'''
        def sourceFolder = new File(guavaDependentProjectRoot, 'src/main/java')
        sourceFolder.mkdirs()

        new File(sourceFolder, "GuavaSample.java") << '''import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class GuavaSample
{
    public List<String> getNames() {
        Builder<String> names = ImmutableList.builder();
        names.add("Gradle");
        names.add("Buildship");

        return names.build();
    }

    public ImmutableList<String> getImmutableNames() {
        Builder<String> names = ImmutableList.builder();
        names.add("Gradle");
        names.add("Buildship");

        return names.build();
    }
}
'''

        // >>> Create project with project dependency to the guava-project
        def dependentProjectRoot = new File(rootProject, 'dependent-project')
        dependentProjectRoot.mkdir()

        def projectDependency = excludeGuavaProjectDependency ? '''compile (project(":guava-project")) {
        exclude group: "com.google.guava"
        }''' : 'compile (project(":guava-project"))'
        new File(dependentProjectRoot, 'build.gradle') << """apply plugin: 'java'

version = '1.0'

repositories {
    mavenCentral()
}

dependencies {
    $projectDependency
}"""
        sourceFolder = new File(dependentProjectRoot, 'src/main/java')
        sourceFolder.mkdirs()

        new File(sourceFolder, "DependentSample.java") << '''import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

public class DependentSample
{
    public void useGuavaSample() {
        GuavaSample applicationB = new GuavaSample();
        List<String> names = applicationB.getNames();
        System.out.println(names);

        ImmutableList<String> immutableNames = applicationB.getImmutableNames();
        System.out.println(immutableNames);

        Builder<Object> builder = ImmutableList.builder();
        builder.build();
    }
}
                '''

        rootProject
    }

    def newProjectImportJob(File location) {
        ProjectImportConfiguration configuration = new ProjectImportConfiguration()
        configuration.gradleDistribution = GradleDistributionWrapper.from(GradleDistribution.fromBuild())
        configuration.projectDir = location
        configuration.applyWorkingSets = true
        configuration.workingSets = []
        new ProjectImportJob(configuration, AsyncHandler.NO_OP)
    }

    IMarker[] getProblemMarkers(IProject project) throws CoreException, InterruptedException {
        setAutoBuilding(false);
        LegacyEclipseSpockTestHelper.workspace.build(IncrementalProjectBuilder.CLEAN_BUILD, null);
        LegacyEclipseSpockTestHelper.workspace.build(IncrementalProjectBuilder.FULL_BUILD, null);
        waitForJobFamily(LegacyEclipseSpockTestHelper.resourcePluginFamilyManualBuildField())
        waitForJobFamily(LegacyEclipseSpockTestHelper.resourcePluginFamilyAutoBuildField())

        IMarker[] findMarkers = waitForMarkers(project, 3000, 300);

        return findMarkers;
    }

    void waitForJobFamily(Object jobFamily) {
        boolean wasInterrupted = true;
        while (wasInterrupted) {
            try {
                Job.getJobManager().join(jobFamily, null);
                wasInterrupted = false;
            } catch (OperationCanceledException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                wasInterrupted = true;
            }
        }
    }

    void setAutoBuilding(boolean enabled) throws CoreException {
        IWorkspaceDescription wsd = LegacyEclipseSpockTestHelper.workspace.description;
        if (!wsd.isAutoBuilding() == enabled) {
            wsd.setAutoBuilding(enabled);
            LegacyEclipseSpockTestHelper.workspace.setDescription(wsd);
        }
    }


    public IMarker[] waitForMarkers(IProject project, long timeout, long interval) {
        long limit = System.currentTimeMillis() + timeout;
        while (true) {
            try {
                IMarker[] markers = project.findMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
                if (markers.length > 0) {
                    return markers;
                }
            } catch (Throwable e) {
                // do nothing
            }
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e) {
                throw new RuntimeException("Could not sleep", e); //$NON-NLS-1$
            }
            if (System.currentTimeMillis() > limit) {
                return new IMarker[0];
            }
        }
    }

}
