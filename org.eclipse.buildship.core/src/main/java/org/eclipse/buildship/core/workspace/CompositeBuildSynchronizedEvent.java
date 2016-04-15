/*
 * Copyright (c) 2016 the original author or authors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.buildship.core.workspace;

import org.eclipse.buildship.core.event.Event;

/**
 * An event indicatting that the {@link CompositeGradleBuild} has been synchronized with the
 * workspace.
 *
 * @author Stefan Oehme
 */
public interface CompositeBuildSynchronizedEvent extends Event {

    public CompositeGradleBuild getCompositeBuild();
}
