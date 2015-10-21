# Web Tools

## Configure web application directory in WTP configurations for web application projects

### Motivation
The `ide-integration.md` document in Gradle core specifies a story ["Expose web application directory"](...). The goal is to define an initial end-to-end framework for Buidship/WTP integration, and to
communicate the location of the web application directory to WTP.

### Estimate

- ? day(s)

### Implementation
- Create branch in buildship repository `wtp-integration`
- Define `org.eclipse.buildship.wtp` plugin in `wtp-integration`
- Define `org.eclipse.buildship.wtp.test` plugin in `wtp-integration`
- Wait for corresponding story in TAPI to be completed
- Upgrade Tooling Commons (if necessary)
- Define configuration class in `org.eclipse.buildship.wtp`, either configurator class, or a similar class to `DefaultWorkspaceGradleOperations`.
    - Retrieve Web application model from TAPI
    - If a project is a web application project (model will be `null` if project doesn't apply `war` plugin)
        - Add Java and Dynamic Web Facets to project, which will generate component file
        - Configure web app dir location in component file
    - Otherwise, do nothing
- Invoke WTP configuration from Buildship core

### Test cases
- Configuration only invoked on web application projects
- Java facet is added to project
- Dynamic Web Facet is added to project
- If Java Facet already on project but has incorrect version, version is updated to correct version based on source Java version
- If Dynamic Web Facet already on project but has incorrect version, version is updated to correct version
- Default Dynamic Web Facet version is 2.5.
- If Java Facet exists on project with correct version, Facet is not affected.
- If Dynamic Web Facet exists on project with correct version, Facet is not affected.
- Dynamic Web Facet version inferred from _web.xml_ file.
- Custom web application directory location is communicated successfully to component file, file exists.
- Default web application directory location is communicated successfully to component file, file exists.
- Custom web application directory location is communicated successfuly to component file, file does not exist
- Default web application directory location is communicated successfully to component file, file does not exist
- Existing `wb-resource` entries in component file not overwritten. 

### Open Questions

- Buildship is now a default plugin of Eclipse. WTP contains a lot of dependencies, which should not be dragged into the Java distribution.

- What should the default facet versions be? The Dynamic Web Facet version does have constraints based on which source Java version is used by the project.

- The Java Facet relies on information about the source/target Java versions. Should this story be blocked by the [Java source level story](https://github.com/eclipse/buildship/blob/master/docs/stories/ToolingAPI.md#set-source-level-for-java-projects)?

- What changes need to be made to the tooling-commons?

- How should buildship core call the WTP configuration plugin?

### Notes

The default Dynamic Web Facet version is 2.5, as it allows for a missing web.xml file, and doens't have a strong constraint on the Java version (
at least Java 5).
