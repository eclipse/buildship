# General

## Fill info on eclipse.org

### Requested Change

1. Describe in more detail what the goal of Buildship is.
1. Provide links to the GitHub repository
    1. how to use install Buildship
    1. how to use build Buildship
    1. how to help out with Buildship
1. Provide a link to Bugzilla bug tracking.
1. Describe the layout of the Downloads section (milestones, snapshots).

### Motivation

We currently do not provide a lot of information, links, etc. on eclipse.org.


## Support Oomph setup

### Requested Change

Provide an Oomph model for the Buildship project.

### Motivation

As of Eclipse Mars, Oomph is the default Eclipse installer. Having an Oomph model for Buildship will allow developers
to easily get to a working setup of the Buildship project.


## Reference Orbit for external dependencies

### Requested Change

Once the external dependencies are available in Orbit, replace the physical jar files of the external
dependencies (located in the _lib_ folder) with references to Orbit.

### Motivation

Currently, the external dependencies of Buildship (Tooling API, Guava, etc.) live in the _lib_ folder of
the _core_ plugin. This is not the Eclipse policy for features shipped with the core distribution.


## Sign plugins with Eclipse certificate

### Requested Change

Use the Eclipse build infrastructure to ensure that the Buildship plugins are signed with the real certificate.

### Motivation

Currently, the Buildship plugins are signed with a self-signed test certificate. In order to ship with the core
distribution, the plugins must be signed with the _real_ Eclipse certificate.


## Professionally designed icons

### Requested Change

Have professionally designed icons for all views, buttons, and other UI elements.

### Motivation

As part of the UX, we need professionally designed icons.


## Buildship logo

### Requested Change

Design a Buildship logo and integrate it into the plugins.

### Motivation

Each Eclipse core application should have its own logo for easy recognition in the Eclipse ecosystem.


## Optionally track plugin usage

### Requested Change

Collect data on the used functionality of Buildship and upload it in an anonymous way (iff the user has
given his consent). In Buildship, display usage statistics to the user. Online, display overall statistics
gathered from all participating users.

### Motivation

In order to constantly improve Buildship, we need to understand what functionality of Buildship people are
using and how they are using it.
