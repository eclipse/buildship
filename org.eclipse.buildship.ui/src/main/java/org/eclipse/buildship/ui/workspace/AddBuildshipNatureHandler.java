package org.eclipse.buildship.ui.workspace;

import java.util.List;

import com.google.common.collect.Lists;

import com.gradleware.tooling.toolingclient.GradleDistribution;
import com.gradleware.tooling.toolingmodel.repository.FixedRequestAttributes;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.core.configuration.GradleProjectNature;
import org.eclipse.buildship.core.workspace.NewProjectHandler;

/**
 * Synchronizes the given projects as if the user had run the import wizard on their location.
 *
 * @author Stefan Oehme
 *
 */
public class AddBuildshipNatureHandler extends AbstractHandler{

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        if (selection instanceof StructuredSelection) {
            List<?> elements = ((StructuredSelection) selection).toList();
            for (Object element : elements) {
                IProject project = Platform.getAdapterManager().getAdapter(element, IProject.class);
                if (project != null && !GradleProjectNature.isPresentOn(project)) {
                    synchronize(project);
                }

            }
        }
        return null;
    }

    private void synchronize(IProject project) {
        IPath location = project.getLocation();
        if (location == null) {
            return;
        }
        FixedRequestAttributes attributes = new FixedRequestAttributes(location.toFile(), null, GradleDistribution.fromBuild(), null, Lists.<String>newArrayList(), Lists.<String>newArrayList());
        CorePlugin.gradleWorkspaceManager().getCompositeBuild().withBuild(attributes).synchronize(NewProjectHandler.IMPORT_AND_MERGE);
    }

}
