package org.eclipse.buildship.ui.internal.view.execution

import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem

class ExecutionViewDoubleClickTest extends BaseExecutionViewTest {

    def "Double-clicking group nodes expand and collapse children"() {
        setup:
        File projectDir = sampleTestProject()
        importAndWait(projectDir)
        launchTaskAndWait(projectDir, 'build')

        SWTBotTree tree = getCurrentTree()
        SWTBotTreeItem root = tree.getTreeItem('Run build')

        expect:
        root.expanded

        when:
        root.doubleClick()

        then:
        !root.expanded

        when:
        root.doubleClick()

        then:
        root.expanded

        when:
        SWTBotTreeItem runNode = root.getNode('Run tasks')

        then:
        !runNode.expanded

        when:
        runNode.doubleClick()

        then:
        runNode.expanded
    }

    def "Double-clicking test class and method open editor"() {
        setup:
        File projectDir = sampleTestProject()
        importAndWait(projectDir)
        launchTaskAndWait(projectDir, 'build')
        waitForGradleJobsToFinish()

        SWTBotTree tree = getCurrentTree()
        SWTBotTreeItem root = tree.getTreeItem('Run build')

        when:
        root.expand()
        SWTBotTreeItem runNode = root.getNode('Run tasks')
        runNode.expand()
        SWTBotTreeItem testNode = runNode.getNode(':test')
        testNode.expand()
        SWTBotTreeItem fileNode = testNode.getNode('LibraryTest')

        then:
        bot.editors().size == 0

        when:
        fileNode.doubleClick()
        waitForGradleJobsToFinish()

        then:
        bot.editorByTitle('LibraryTest.java') != null

        when:
        bot.editorByTitle('LibraryTest.java').close()
        fileNode.expand()
        SWTBotTreeItem methodNode = fileNode.getNode('testSomeLibraryMethod')

        then:
        bot.editors().size == 0

        when:
        methodNode.doubleClick()

        then:
        bot.editorByTitle('LibraryTest.java') != null

        cleanup:
        bot.closeAllEditors()
    }

    File sampleTestProject() {
       dir('sampleTest') {
           dir('src/main/java') {
               file 'Library.java', """
                    public class Library {
                    public boolean someLibraryMethod() {
                        return true;
                        }
                    }
               """
           }
           dir('src/test/java') {
               file 'LibraryTest.java', """
                  import org.junit.Test;
                  import static org.junit.Assert.*;

                  public class LibraryTest {
                      @Test public void testSomeLibraryMethod() {
                          Library classUnderTest = new Library();
                          assertTrue("someLibraryMethod should return 'true'", classUnderTest.someLibraryMethod());
                      }
                  }
                  """
            }

            file 'build.gradle', """
                apply plugin: 'java'

                ${jcenterRepositoryBlock}

                dependencies {
                    testCompile 'junit:junit:4.12'
                }
            """
        }
    }

}
