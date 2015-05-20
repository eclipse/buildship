package org.eclipse.buildship.ui.editor.completionproposals;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;

import org.eclipse.buildship.core.model.taskmetadata.GradleTaskMetaDataManager;
import org.eclipse.buildship.ui.PluginImage.ImageState;
import org.eclipse.buildship.ui.PluginImages;
import org.eclipse.buildship.ui.UiPlugin;
import org.eclipse.buildship.ui.wizard.task.NewGradleTaskWizard;
import org.eclipse.buildship.ui.wizard.task.TaskCreationModel;


public class GradleTaskCompletionProposal implements ICompletionProposal {

    private int offset;
    private Shell shell;

    public GradleTaskCompletionProposal(Shell shell, int offset) {
        this.shell = shell;
        this.offset = offset;
    }

    @Override
    public void apply(IDocument document) {

        GradleTaskMetaDataManager gradleTaskMetaDataManager = new GradleTaskMetaDataManager();
        NewGradleTaskWizard newGradleTaskWizard = new NewGradleTaskWizard(gradleTaskMetaDataManager.getTaskMetaData());

        WizardDialog wizardDialog = new WizardDialog(shell, newGradleTaskWizard);

        int open = wizardDialog.open();
        if (Window.OK == open) {
            TaskCreationModel taskCreationModel = newGradleTaskWizard.getTaskCreationModel();
            String taskTypeFunction = taskCreationModel.getTaskTypeFunction();
            try {
                document.replace(offset, 0, taskTypeFunction);
            } catch (BadLocationException e) {
                UiPlugin.logger().error(e.getMessage(), e);
            }
        }
    }

    @Override
    public Point getSelection(IDocument document) {
        return new Point(offset, 0);
    }

    @Override
    public String getAdditionalProposalInfo() {
        return "Use this to add Gradle tasks. It opens a wizard for the creation of a Gradle task.";
    }

    @Override
    public String getDisplayString() {
        return "Wizard - Create Gradle Task";
    }

    @Override
    public Image getImage() {
        return PluginImages.GRADLE_LOGO.withState(ImageState.ENABLED).getImage();
    }

    @Override
    public IContextInformation getContextInformation() {
        return null;
    }

}
