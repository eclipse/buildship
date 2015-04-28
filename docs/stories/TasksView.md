# Task View

## ~~Revisit current Task View~~

### Requested Change

* By default, do not show the project tasks in the tree.
* By default, do not show the private tasks in the tree.
* Move the buttons to show the project and private tasks to the menu such that they are less prominent.
* Use the wording _show all tasks_ instead of _include private tasks_.

### Motivation

The current display and configuration of what is displayed provides maximum flexibility but is
potentially confusing to the user.


## Show tasks grouped by their _group_ attribute

### Requested Change

By default, show the tasks grouped by their _group_ attribute in the tree. Have a configuration option in
the Task View menu to show the tasks without considering the _group_ attribute. Enhance the `TaskViewContentProvider`
to return the elements with the new grouping.

### Motivation

Grouping the tasks by their _group_ attribute is very similar to how tasks are displayed on the command line.


## Provide more options on how to display the task view

### Requested Change

Provide the following options on how to show the projects in the Task View:

1. Structure projects like in the Explorer View (default)
1. Show projects in nested view
1. Show projects in flat view
    1. alphabetically
    1. alphabetically by root project

### Motivation

Give the user more flexibility in how to display the projects in the Task View.


## Configurable task filter

### Requested Change

Provide a menu item in the Task View toolbar to filter out tasks that should not show up in the Task View, and provide
an option to apply/ignore the filter. Configuring and applying the filter could be combined into a split button, where the
click-behavior is to toggle the filter and the menu item in the popup menu is to launch the filter configuration dialog.

### Motivation

Not all tasks of a project are relevant to the user. Allow the user to hide those tasks that are of no interest to him.


## Task favorites

### Requested Change

Provide a UI to define and run task favorites.

### Motivation

Typically, a user runs the same tasks repeatedly and there should be as little overhead as possible to execute a favorite task.


## Refresh tasks of a single multi-project

### Requested Change

Provide a context-menu item for project nodes that allows to refresh the tasks of only the multi-project to which
the selected project belongs.

### Motivation

The current Refresh button refreshes all multi-projects. Through the new context menu item, the refresh
can be triggered more specifically to a single multi-project.


## Selection of project tasks across projects

### Requested Change

Allow multi-selection of project tasks from different parent projects. Set the project directory to the
directory of the closest shared parent project.

### Motivation

Restricting the selection of project tasks to a single parent project is unnecessary. Only the selection of
task selectors needs to remain restricted to a single parent project.


## Show Gradle run configuration history in toolbar via dropdown

### Requested Change

Show a dropdown in the toolbar of the Task View that contains the existing Gradle run configurations.

### Motivation

Rerunning an existing Gradle run configuration is already possible via the dropdown in the global toolbar. Make
it more convenient to the user to re-run existing run configurations by providing him with a dropdown in
the Task View that contains solely Gradle run configurations.
