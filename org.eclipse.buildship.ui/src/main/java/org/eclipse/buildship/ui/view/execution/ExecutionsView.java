package org.eclipse.buildship.ui.view.execution;

import org.eclipse.buildship.ui.taskview.TaskViewMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.part.ViewPart;

/**
 * A view displaying the Gradle executions.
 */
public final class ExecutionsView extends ViewPart {

    private PageBook pages;
    private Label emptyInputPage;
    private Composite nonEmptyInputPage;

    @Override
    public void createPartControl(Composite parent) {
        // the top-level control changing its content depending on whether the content provider
        // contains task data to display or not
        this.pages = new PageBook(parent, SWT.NONE);

        // if there is no execution data to display, show only a label
        this.emptyInputPage = new Label(this.pages, SWT.NONE);
        this.emptyInputPage.setText(TaskViewMessages.Label_No_Gradle_Projects);

        // if there is execution data to display, show the execution tre
        this.nonEmptyInputPage = new Composite(this.pages, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        this.nonEmptyInputPage.setLayout(layout);

        this.pages.showPage(this.nonEmptyInputPage);
    }

    public Composite getControl() {
        return this.nonEmptyInputPage;
    }

    @Override
    public void setFocus() {
        this.pages.setFocus();
    }

}
