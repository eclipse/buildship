<!-- In the first section, explain what does your pull request do -->

### Testing the feature

#### Launch Eclipse with Buildship containing changes from this pull request

1. Install the Eclipse installer: go to https://www.eclipse.org/downloads/ and click the `Download 64` button and proceed with the installer installation.
2. Provision the Buildship IDE
  - Open the Eclipse installer
  - Select `Advanced mode...` in the menu in the top-right corner

<img width="400" alt="Screenshot 2019-07-31 at 14 27 50" src="https://user-images.githubusercontent.com/419883/62214328-20a26480-b3a5-11e9-9777-6839450e9108.png">

  - On the product page select Eclipse IDE for Eclipse Committers and change the product version to the latest release

<img width="400" alt="Screenshot 2019-07-31 at 14 30 09" src="https://user-images.githubusercontent.com/419883/62214378-3dd73300-b3a5-11e9-9883-ae918b2bd17a.png">

  - On the Projects page select `Eclipse Projects` > `Buildship`

<img width="400" alt="Screenshot 2019-07-31 at 14 30 49" src="https://user-images.githubusercontent.com/419883/62214391-462f6e00-b3a5-11e9-89ef-5092bbc82258.png">

  - On the variables page set the git branch to `TODO_SPECIFY_FEATURE_BRANCH`. Also, set the installation / workspace / git clone location to your liking. 

<img width="400" alt="Screenshot 2019-07-31 at 14 46 58" src="https://user-images.githubusercontent.com/419883/62214404-4cbde580-b3a5-11e9-84b1-3c0f9c4c7b66.png">

  - Click finish and wait for the provisioning to finish. Once done, you should see a fully configured IDE with no compilation errors.

3. Launch development version of Buildship to verify changes
 -  In Eclipse, Open the Run Configuration dialog and run the `Launch Buildship` configuration

<img width="400" alt="Screenshot 2019-07-31 at 15 01 08" src="https://user-images.githubusercontent.com/419883/62214426-58a9a780-b3a5-11e9-98ae-cc38240ab8c0.png">

 - Create a new Gradle project, try to use a test class in the main sources.
 - You can verify the classpath content by enabling tracing in the `Launch Buildship` configuration.
 
 #### Evaluate change from pull request
 
 <!-- Explain how can a project member try your changes -->
