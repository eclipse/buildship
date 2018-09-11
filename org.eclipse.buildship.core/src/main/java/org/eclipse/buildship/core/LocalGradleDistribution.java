package org.eclipse.buildship.core;

import java.io.File;

public interface LocalGradleDistribution extends GradleDistribution {

    File getLocation();
}
