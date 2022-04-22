/*******************************************************************************
 * Copyright (c) 2019 Gradle Inc.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/
package org.eclipse.buildship.core.internal.util.gradle;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.google.common.collect.ImmutableList;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.service.component.annotations.Component;
import org.eclipse.buildship.core.internal.CorePlugin;
import org.eclipse.buildship.core.internal.util.gradle.PublishedGradleVersions.LookupStrategy;

/**
 * Wraps the {@link PublishedGradleVersions} functionality in a background job
 * that handles all exceptions gracefully. If an exception occurs while calling
 * the underlying {@link PublishedGradleVersions} instance, default version
 * information is provided. This handles, for example, those scenarios where the
 * versions cannot be retrieved because the user is behind a proxy or offline.
 */
@Component(property = { "service.ranking:Integer=1" }, service = PublishedGradleVersionsWrapper.class)
public final class PublishedGradleVersionsWrapper {

	private final AtomicReference<PublishedGradleVersions> publishedGradleVersions;

	public PublishedGradleVersionsWrapper() {
		this.publishedGradleVersions = new AtomicReference<PublishedGradleVersions>();
		new LoadVersionsJob().schedule();
	}

	public List<GradleVersion> getVersions() {
		PublishedGradleVersions versions = this.publishedGradleVersions.get();
		return versions != null ? versions.getVersions() : ImmutableList.of(GradleVersion.current());
	}

	/**
	 * Loads the published Gradle versions in the background.
	 * 
	 * @author Stefan Oehme
	 */
	private final class LoadVersionsJob extends Job {

		public LoadVersionsJob() {
			super("Loading available Gradle versions");
			setSystem(true);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				PublishedGradleVersions versions = PublishedGradleVersions.create(LookupStrategy.REMOTE_IF_NOT_CACHED);
				PublishedGradleVersionsWrapper.this.publishedGradleVersions.set(versions);
			} catch (RuntimeException e) {
				CorePlugin.logger().warn("Could not load Gradle version information", e);
			}
			return Status.OK_STATUS;
		}

		@Override
		protected void canceling() {
			Thread.currentThread().interrupt();
		}

	}

}
