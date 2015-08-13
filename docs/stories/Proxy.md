### Story: Integrate Eclipse proxy settings into Buildship model loading and task execution

This story is a follow-up on [Bug 471943 - Make Buildship work behind the firewall](https://bugs.eclipse.org/bugs/show_bug.cgi?id=471943). The goal is to forward Eclipse's proxy settings to Gradle upon all interactions, including the model retrievals and the task executions.

#### Implementation plan
1. Implement a helper class to retrieve the current proxy settings.
2. Extend ToolingApiJob and ToolingApiWorkspaceJob to offer default arguments (getDefaultJvmArguments()) and return the proxy setting there. 
3. Prepend the default JVM arguments to the JVM argument list in the concrete job implementations. 

#### Test Cases
See open questions

#### Open questions
- Will the proxy settings be picked up to download the a Gradle distribution? (Discussed with René - yes)
- How can be the proxy settings tested in Buildship?
- Should we verify that a new daemon is started when the proxy settings are changed?
