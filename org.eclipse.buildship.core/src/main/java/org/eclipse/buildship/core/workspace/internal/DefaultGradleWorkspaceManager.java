/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.eclipse.buildship.core.workspace.internal;

import java.util.Set;

import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.resources.IProject;

import org.eclipse.buildship.core.util.progress.AsyncHandler;
import org.eclipse.buildship.core.workspace.GradleWorkspaceManager;
import org.eclipse.buildship.core.workspace.NewProjectHandler;


/**
 * @author Stefan Oehme
 *
 */
public class DefaultGradleWorkspaceManager implements GradleWorkspaceManager {

    @Override
    public void importGradleBuild(FixedRequestAttributes attributes, NewProjectHandler newProjectHandler) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void createGradleBuild(FixedRequestAttributes attributes, NewProjectHandler newProjectHandler, AsyncHandler initializer) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void synchronizeProjects(Set<IProject> projects, NewProjectHandler newProjectHandler) {
        // TODO Auto-generated method stub
        
    }


}
