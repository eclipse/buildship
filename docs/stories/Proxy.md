### ~~Story: Integrate Eclipse proxy settings into Buildship model loading and task execution~~

This story is a follow-up on [Bug 471943 - Make Buildship work behind the firewall](https://bugs.eclipse.org/bugs/show_bug.cgi?id=471943). The goal is to forward Eclipse's proxy settings to Gradle upon all interactions, including the model retrievals and the task executions.

Eclipse offers a simple API to query the current proxy settings.

    IProxyService service = ... // get the service
    for (IProxyData proxyData : service.getProxyData()) {
        String type = proxyData.getType(); // can be IProxyData.HTTP_PROXY_TYPE for instance
        String host = proxyData.getHost();
        int port = proxyData.getPort();
        String[] nonProxiedHosts = service.getNonProxiedHosts();
        boolean authenticationRequired = proxyData.isRequiresAuthentication();
        String user = proxyData.getUserId();
        String pass = proxyData.getPassword();
    }

Gradle has two phase we should consider: 1) time when accessing the tooling API and 2) after the IPC connection is established. The first case is where we specify proxies for the distribution download. The second is where we query models and execute tasks via the TAPI.

Gradle defines hard-coded places where it loads the proxy settings from (in ascending priority):
- `~/.gradle/gradle.properties` file
- The `gradle.properties` file under the project root
- In the system properties
If we get the proxy settings from these locations, we can configure Buildship before accessing the tooling API.

Buildship should set the proxy setting if anything defined in the Eclipse preferences and ignore the settings from Gradle.

#### Implementation plan
1. Collect preferences from Eclipse
2. Set the proxy setting as system properties before a `ToolingApiJob` or `ToolingApiWorkspaceJob` instance is scheduled and the revert them when finished
3. Specify the proxy settings as default JVM arguments for all `ToolingApiJob` and `ToolingApiWorkspaceJob` instances

#### Test Cases
- Simple tests for collecting preferences
- Test if the system properties are temporary changed when the jobs are running
- Test if the JVM arguments are automatically set when proxy settings are available
- Verify that subsequent build can have different proxy settings (~the daemon could cache the settings by reading the system properties only at startup)
- Test if the proxies are accessed upon task execution
  - Copy over the `TestProxyServer` test fixture from Gradle core to do the integration testing
