/*******************************************************************************
 * Copyright (c) 2014 vogella GmbH and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Simon Scholz <simon.scholz@vogella.com> - initial API and implementation based on org.eclipse.ui.dialogs.FilteredTree
 *******************************************************************************/

package org.eclipse.buildship.ui.internal.extviewer;

import java.net.URL;

import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.jface.viewers.IContentProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;

/**
 * Based on org.eclipse.ui.dialogs.FilteredTree.
 */
public class FilteredTree extends Composite {

    /**
     * The filter text widget to be used by this tree. This value may be <code>null</code> if there
     * is no filter widget, or if the controls have not yet been created.
     */
    private Text filterText;

    /**
     * The control representing the clear button for the filter text entry. This value may be
     * <code>null</code> if no such button exists, or if the controls have not yet been created.
     */
    private Control clearButtonControl;

    /**
     * The viewer for the filtered tree. This value should never be <code>null</code> after the
     * widget creation methods are complete.
     */
    private TreeViewer treeViewer;

    /**
     * The Composite on which the filter controls are created. This is used to set the background
     * color of the filter controls to match the surrounding controls.
     */
    private Composite filterComposite;

    /**
     * The pattern filter for the tree. This value must not be <code>null</code> .
     */
    private PatternFilter patternFilter;

    /**
     * The text to initially show in the filter text control.
     */
    private String initialText = ""; //$NON-NLS-1$

    /**
     * The job used to refresh the tree.
     */
    private Job refreshJob;

    /**
     * Whether or not to show the filter controls (text and clear button). The default is to show
     * these controls.
     */
    private boolean showFilterControls;

    private Composite treeComposite;

    /**
     * Image descriptor for enabled clear button.
     */
    private static final String CLEAR_ICON = "org.eclipse.ui.internal.dialogs.CLEAR_ICON"; //$NON-NLS-1$

    /**
     * Image descriptor for disabled clear button.
     */
    private static final String DISABLED_CLEAR_ICON = "org.eclipse.ui.internal.dialogs.DCLEAR_ICON"; //$NON-NLS-1$

    /**
     * Image descriptor for disabled clear button.
     */
    private static final String PRESSED_CLEAR_ICON = "org.eclipse.ui.internal.dialogs.PRESSED_CLEAR_ICON"; //$NON-NLS-1$

    /**
     * Maximum time spent expanding the tree after the filter text has been updated (this is only
     * used if we were able to at least expand the visible nodes).
     */
    private static final long SOFT_MAX_EXPAND_TIME = 200;

    /**
     * Get image descriptors for the clear button.
     */
    static {
        Bundle bundle = FrameworkUtil.getBundle(FilteredTree.class);
        IPath enabledPath = new Path("$nl$/icons/full/etool16/clear_co.png");
        URL enabledURL = FileLocator.find(bundle, enabledPath, null);
        ImageDescriptor enabledDesc = ImageDescriptor.createFromURL(enabledURL);
        if (enabledDesc != null) {
            JFaceResources.getImageRegistry().put(CLEAR_ICON, enabledDesc);
            JFaceResources.getImageRegistry().put(PRESSED_CLEAR_ICON, ImageDescriptor.createWithFlags(enabledDesc, SWT.IMAGE_GRAY));
        }

        IPath disabledPath = new Path("$nl$/icons/full/dtool16/clear_co.png");
        URL disabledURL = FileLocator.find(bundle, disabledPath, null);
        ImageDescriptor disabledDesc = ImageDescriptor.createFromURL(disabledURL);
        if (disabledDesc != null) {
            JFaceResources.getImageRegistry().put(DISABLED_CLEAR_ICON, disabledDesc);
        }
    }

    /**
     * Create a new instance of the receiver.
     *
     * @param parent the parent <code>Composite</code>
     * @param treeStyle the style bits for the <code>Tree</code>
     * @param filter the filter to be used
     */
    public FilteredTree(Composite parent, int treeStyle, PatternFilter filter) {
        super(parent, SWT.NONE);
        init(treeStyle, filter);
    }

    /**
     * Create a new instance of the receiver. Subclasses that wish to override the default creation
     * behavior may use this constructor, but must ensure that the
     * <code>init(composite, int, PatternFilter)</code> method is called in the overriding
     * constructor.
     *
     * @param parent the parent <code>Composite</code>
     * @see #init(int, PatternFilter)
     *
     */
    protected FilteredTree(Composite parent) {
        super(parent, SWT.NONE);
    }

    /**
     * Create the filtered tree.
     *
     * @param treeStyle the style bits for the <code>Tree</code>
     * @param filter the filter to be used
     *
     * @since 3.3
     */
    protected void init(int treeStyle, PatternFilter filter) {
        this.patternFilter = filter;
        setShowFilterControls(true);
        createControl(getParent(), treeStyle);
        createRefreshJob();
        setInitialText(ViewerMessages.FilteredTree_FilterMessage);
        setFont(getParent().getFont());
    }

    /**
     * Create the filtered tree's controls. Subclasses should override.
     *
     * @param parent
     * @param treeStyle
     */
    protected void createControl(Composite parent, int treeStyle) {
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        setLayout(layout);
        setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        if (useNativeSearchField(parent)) {
            this.filterComposite = new Composite(this, SWT.NONE);
        } else {
            this.filterComposite = new Composite(this, SWT.BORDER);
            this.filterComposite.setBackground(getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
        }
        GridLayout filterLayout = new GridLayout(2, false);
        filterLayout.marginHeight = 0;
        filterLayout.marginWidth = 0;
        this.filterComposite.setLayout(filterLayout);
        this.filterComposite.setFont(parent.getFont());

        createFilterControls(this.filterComposite);
        GridData gridData = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
        this.filterComposite.setVisible(isShowFilterControls());
        gridData.exclude = !isShowFilterControls();
        this.filterComposite.setLayoutData(gridData);

        this.treeComposite = new Composite(this, SWT.NONE);
        GridLayout treeCompositeLayout = new GridLayout();
        treeCompositeLayout.marginHeight = 0;
        treeCompositeLayout.marginWidth = 0;
        this.treeComposite.setLayout(treeCompositeLayout);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.treeComposite.setLayoutData(data);
        createTreeControl(this.treeComposite, treeStyle);
    }

    private static Boolean useNativeSearchField;

    private static boolean useNativeSearchField(Composite composite) {
        if (useNativeSearchField == null) {
            useNativeSearchField = Boolean.FALSE;
            Text testText = null;
            try {
                testText = new Text(composite, SWT.SEARCH | SWT.ICON_CANCEL);
                useNativeSearchField = new Boolean((testText.getStyle() & SWT.ICON_CANCEL) != 0);
            } finally {
                if (testText != null) {
                    testText.dispose();
                }
            }

        }
        return useNativeSearchField.booleanValue();
    }

    /**
     * Create the filter controls. By default, a text and corresponding tool bar button that clears
     * the contents of the text is created. Subclasses may override.
     *
     * @param parent parent <code>Composite</code> of the filter controls
     * @return the <code>Composite</code> that contains the filter controls
     */
    protected Composite createFilterControls(Composite parent) {
        createFilterText(parent);
        createClearText(parent);
        if (this.clearButtonControl != null) {
            // initially there is no text to clear
            this.clearButtonControl.setVisible(false);
        }
        return parent;
    }

    /**
     * Creates and set up the tree and tree viewer. This method calls
     * {@link #doCreateTreeViewer(Composite, int)} to create the tree viewer. Subclasses should
     * override {@link #doCreateTreeViewer(Composite, int)} instead of overriding this method.
     *
     * @param parent parent <code>Composite</code>
     * @param style SWT style bits used to create the tree
     * @return the tree
     */
    protected Control createTreeControl(Composite parent, int style) {
        this.treeViewer = doCreateTreeViewer(parent, style);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        this.treeViewer.getControl().setLayoutData(data);
        this.treeViewer.getControl().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                FilteredTree.this.refreshJob.cancel();
            }
        });
        if (this.treeViewer instanceof NotifyingTreeViewer) {
            this.patternFilter.setUseCache(true);
        }
        this.treeViewer.addFilter(this.patternFilter);
        return this.treeViewer.getControl();
    }

    /**
     * Creates the tree viewer. Subclasses may override.
     *
     * @param parent the parent composite
     * @param style SWT style bits used to create the tree viewer
     * @return the tree viewer
     *
     * @since 3.3
     */
    protected TreeViewer doCreateTreeViewer(Composite parent, int style) {
        return new NotifyingTreeViewer(parent, style);
    }

    /**
     * Return the first item in the tree that matches the filter pattern.
     *
     * @param items
     * @return the first matching TreeItem
     */
    private TreeItem getFirstMatchingItem(TreeItem[] items) {
        for (TreeItem item : items) {
            if (this.patternFilter.isLeafMatch(this.treeViewer, item.getData()) && this.patternFilter.isElementSelectable(item.getData())) {
                return item;
            }
            TreeItem treeItem = getFirstMatchingItem(item.getItems());
            if (treeItem != null) {
                return treeItem;
            }
        }
        return null;
    }

    /**
     * Create the refresh job for the receiver.
     *
     */
    private void createRefreshJob() {
        this.refreshJob = doCreateRefreshJob();
        this.refreshJob.setSystem(true);
    }

    /**
     * Creates a workbench job that will refresh the tree based on the current filter text.
     * Subclasses may override.
     *
     * @return a workbench job that can be scheduled to refresh the tree
     *
     * @since 3.4
     */
    protected BasicUIJob doCreateRefreshJob() {
        return new BasicUIJob("Refresh Filter", getDisplay()) {//$NON-NLS-1$

            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (FilteredTree.this.treeViewer.getControl().isDisposed()) {
                    return Status.CANCEL_STATUS;
                }

                String text = getFilterString();
                if (text == null) {
                    return Status.OK_STATUS;
                }

                boolean initial = FilteredTree.this.initialText != null && FilteredTree.this.initialText.equals(text);
                if (initial) {
                    FilteredTree.this.patternFilter.setPattern(null);
                } else if (text != null) {
                    FilteredTree.this.patternFilter.setPattern(text);
                }

                Control redrawFalseControl = FilteredTree.this.treeComposite != null ? FilteredTree.this.treeComposite : FilteredTree.this.treeViewer.getControl();
                try {
                    // don't want the user to see updates that will be made to
                    // the tree
                    // we are setting redraw(false) on the composite to avoid
                    // dancing scrollbar
                    redrawFalseControl.setRedraw(false);
                    if (!FilteredTree.this.narrowingDown) {
                        // collapse all
                        TreeItem[] is = FilteredTree.this.treeViewer.getTree().getItems();
                        for (TreeItem item : is) {
                            if (item.getExpanded()) {
                                FilteredTree.this.treeViewer.setExpandedState(item.getData(), true);
                            }
                        }
                    }
                    FilteredTree.this.treeViewer.refresh(true);

                    if (text.length() > 0 && !initial) {
                        /*
                         * Expand elements one at a time. After each is expanded, check to see if
                         * the filter text has been modified. If it has, then cancel the refresh job
                         * so the user doesn't have to endure expansion of all the nodes.
                         */
                        TreeItem[] items = getViewer().getTree().getItems();
                        int treeHeight = getViewer().getTree().getBounds().height;
                        int numVisibleItems = treeHeight / getViewer().getTree().getItemHeight();
                        long stopTime = SOFT_MAX_EXPAND_TIME + System.currentTimeMillis();
                        boolean cancel = false;
                        if (items.length > 0 && recursiveExpand(items, monitor, stopTime, new int[] { numVisibleItems })) {
                            cancel = true;
                        }

                        // enabled toolbar - there is text to clear
                        // and the list is currently being filtered
                        updateToolbar(true);

                        if (cancel) {
                            return Status.CANCEL_STATUS;
                        }
                    } else {
                        // disabled toolbar - there is no text to clear
                        // and the list is currently not filtered
                        updateToolbar(false);
                    }
                } finally {
                    // done updating the tree - set redraw back to true
                    TreeItem[] items = getViewer().getTree().getItems();
                    if (items.length > 0 && getViewer().getTree().getSelectionCount() == 0) {
                        FilteredTree.this.treeViewer.getTree().setTopItem(items[0]);
                    }
                    redrawFalseControl.setRedraw(true);
                }
                return Status.OK_STATUS;
            }

            /**
             * Returns true if the job should be canceled (because of timeout or actual
             * cancellation).
             *
             * @param items
             * @param monitor
             * @param cancelTime
             * @param numItemsLeft
             * @return true if canceled
             */
            private boolean recursiveExpand(TreeItem[] items, IProgressMonitor monitor, long cancelTime, int[] numItemsLeft) {
                boolean canceled = false;
                for (int i = 0; !canceled && i < items.length; i++) {
                    TreeItem item = items[i];
                    boolean visible = numItemsLeft[0]-- >= 0;
                    if (monitor.isCanceled() || (!visible && System.currentTimeMillis() > cancelTime)) {
                        canceled = true;
                    } else {
                        Object itemData = item.getData();
                        if (itemData != null) {
                            if (!item.getExpanded()) {
                                // do the expansion through the viewer so that
                                // it can refresh children appropriately.
                                FilteredTree.this.treeViewer.setExpandedState(itemData, true);
                            }
                            TreeItem[] children = item.getItems();
                            if (items.length > 0) {
                                canceled = recursiveExpand(children, monitor, cancelTime, numItemsLeft);
                            }
                        }
                    }
                }
                return canceled;
            }

        };
    }

    protected void updateToolbar(boolean visible) {
        if (this.clearButtonControl != null) {
            this.clearButtonControl.setVisible(visible);
        }
    }

    /**
     * Creates the filter text and adds listeners. This method calls
     * {@link #doCreateFilterText(Composite)} to create the text control. Subclasses should override
     * {@link #doCreateFilterText(Composite)} instead of overriding this method.
     *
     * @param parent <code>Composite</code> of the filter text
     */
    protected void createFilterText(Composite parent) {
        this.filterText = doCreateFilterText(parent);
        this.filterText.getAccessible().addAccessibleListener(new AccessibleAdapter() {

            @Override
            public void getName(AccessibleEvent e) {
                String filterTextString = FilteredTree.this.filterText.getText();
                if (filterTextString.length() == 0 || filterTextString.equals(FilteredTree.this.initialText)) {
                    e.result = FilteredTree.this.initialText;
                } else {
                    e.result = NLS.bind(ViewerMessages.FilteredTree_AccessibleListenerFiltered, new String[] { filterTextString, String.valueOf(getFilteredItemsCount()) });
                }
            }

            /**
             * Return the number of filtered items
             *
             * @return int
             */
            private int getFilteredItemsCount() {
                int total = 0;
                TreeItem[] items = getViewer().getTree().getItems();
                for (TreeItem item : items) {
                    total += itemCount(item);

                }
                return total;
            }

            /**
             * Return the count of treeItem and it's children to infinite depth.
             *
             * @param treeItem
             * @return int
             */
            private int itemCount(TreeItem treeItem) {
                int count = 1;
                TreeItem[] children = treeItem.getItems();
                for (TreeItem element : children) {
                    count += itemCount(element);

                }
                return count;
            }
        });

        this.filterText.addFocusListener(new FocusAdapter() {

            @Override
            public void focusLost(FocusEvent e) {
                if (FilteredTree.this.filterText.getText().equals(FilteredTree.this.initialText)) {
                    setFilterText(""); //$NON-NLS-1$
                    textChanged();
                }
            }
        });

        this.filterText.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseDown(MouseEvent e) {
                if (FilteredTree.this.filterText.getText().equals(FilteredTree.this.initialText)) {
                    // XXX: We cannot call clearText() due to
                    // https://bugs.eclipse.org/bugs/show_bug.cgi?id=260664
                    setFilterText(""); //$NON-NLS-1$
                    textChanged();
                }
            }
        });

        this.filterText.addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {
                // on a CR we want to transfer focus to the list
                boolean hasItems = getViewer().getTree().getItemCount() > 0;
                if (hasItems && e.keyCode == SWT.ARROW_DOWN) {
                    FilteredTree.this.treeViewer.getTree().setFocus();
                    return;
                }
            }
        });

        // enter key set focus to tree
        this.filterText.addTraverseListener(new TraverseListener() {

            @Override
            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_RETURN) {
                    e.doit = false;
                    if (getViewer().getTree().getItemCount() == 0) {
                        Display.getCurrent().beep();
                    } else {
                        // if the initial filter text hasn't changed, do not try
                        // to match
                        boolean hasFocus = getViewer().getTree().setFocus();
                        boolean textChanged = !getInitialText().equals(FilteredTree.this.filterText.getText().trim());
                        if (hasFocus && textChanged && FilteredTree.this.filterText.getText().trim().length() > 0) {
                            Tree tree = getViewer().getTree();
                            TreeItem item;
                            if (tree.getSelectionCount() > 0) {
                                item = getFirstMatchingItem(tree.getSelection());
                            } else {
                                item = getFirstMatchingItem(tree.getItems());
                            }
                            if (item != null) {
                                tree.setSelection(new TreeItem[] { item });
                                ISelection sel = getViewer().getSelection();
                                getViewer().setSelection(sel, true);
                            }
                        }
                    }
                }
            }
        });

        this.filterText.addModifyListener(new ModifyListener() {

            @Override
            public void modifyText(ModifyEvent e) {
                textChanged();
            }
        });

        // if we're using a field with built in cancel we need to listen for
        // default selection changes (which tell us the cancel button has been
        // pressed)
        if ((this.filterText.getStyle() & SWT.ICON_CANCEL) != 0) {
            this.filterText.addSelectionListener(new SelectionAdapter() {

                @Override
                public void widgetDefaultSelected(SelectionEvent e) {
                    if (e.detail == SWT.ICON_CANCEL) {
                        clearText();
                    }
                }
            });
        }

        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        // if the text widget supported cancel then it will have it's own
        // integrated button. We can take all of the space.
        if ((this.filterText.getStyle() & SWT.ICON_CANCEL) != 0) {
            gridData.horizontalSpan = 2;
        }
        this.filterText.setLayoutData(gridData);
    }

    /**
     * Creates the text control for entering the filter text. Subclasses may override.
     *
     * @param parent the parent composite
     * @return the text widget
     *
     * @since 3.3
     */
    protected Text doCreateFilterText(Composite parent) {
        if (useNativeSearchField(parent)) {
            return new Text(parent, SWT.SINGLE | SWT.BORDER | SWT.SEARCH | SWT.ICON_CANCEL);
        }
        return new Text(parent, SWT.SINGLE);
    }

    private String previousFilterText;

    private boolean narrowingDown;

    /**
     * Update the receiver after the text has changed.
     */
    protected void textChanged() {
        this.narrowingDown = this.previousFilterText == null || this.previousFilterText.equals(ViewerMessages.FilteredTree_FilterMessage) || getFilterString().startsWith(this.previousFilterText);
        this.previousFilterText = getFilterString();
        // cancel currently running job first, to prevent unnecessary redraw
        this.refreshJob.cancel();
        this.refreshJob.schedule(getRefreshJobDelay());
    }

    /**
     * Return the time delay that should be used when scheduling the filter refresh job. Subclasses
     * may override.
     *
     * @return a time delay in milliseconds before the job should run
     *
     * @since 3.5
     */
    protected long getRefreshJobDelay() {
        return 200;
    }

    /**
     * Set the background for the widgets that support the filter text area.
     *
     * @param background background <code>Color</code> to set
     */
    @Override
    public void setBackground(Color background) {
        super.setBackground(background);
        if (this.filterComposite != null && (useNativeSearchField(this.filterComposite))) {
            this.filterComposite.setBackground(background);
        }
    }


    /**
     * Create the button that clears the text.
     *
     * @param parent parent <code>Composite</code> of toolbar button
     */
    private void createClearText(Composite parent) {
        // only create the button if the text widget doesn't support one
        // natively
        if ((this.filterText.getStyle() & SWT.ICON_CANCEL) == 0) {

            final Label clearButton = new Label(parent, SWT.NONE);
            ResourceManager resourceManager = new LocalResourceManager(JFaceResources.getResources(), clearButton);

            final Image inactiveImage = resourceManager.createImage(JFaceResources.getImageRegistry().getDescriptor(DISABLED_CLEAR_ICON));
            final Image activeImage = resourceManager.createImage(JFaceResources.getImageRegistry().getDescriptor(CLEAR_ICON));
            final Image pressedImage = resourceManager.createImage(JFaceResources.getImageRegistry().getDescriptor(PRESSED_CLEAR_ICON));

            clearButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
            clearButton.setImage(inactiveImage);
            clearButton.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
            clearButton.setToolTipText(ViewerMessages.FilteredTree_ClearToolTip);
            clearButton.addMouseListener(new MouseAdapter() {

                private MouseMoveListener fMoveListener;

                @Override
                public void mouseDown(MouseEvent e) {
                    clearButton.setImage(pressedImage);
                    this.fMoveListener = new MouseMoveListener() {

                        private boolean fMouseInButton = true;

                        @Override
                        public void mouseMove(MouseEvent e) {
                            boolean mouseInButton = isMouseInButton(e);
                            if (mouseInButton != this.fMouseInButton) {
                                this.fMouseInButton = mouseInButton;
                                clearButton.setImage(mouseInButton ? pressedImage : inactiveImage);
                            }
                        }
                    };
                    clearButton.addMouseMoveListener(this.fMoveListener);
                }

                @Override
                public void mouseUp(MouseEvent e) {
                    if (this.fMoveListener != null) {
                        clearButton.removeMouseMoveListener(this.fMoveListener);
                        this.fMoveListener = null;
                        boolean mouseInButton = isMouseInButton(e);
                        clearButton.setImage(mouseInButton ? activeImage : inactiveImage);
                        if (mouseInButton) {
                            clearText();
                            FilteredTree.this.filterText.setFocus();
                        }
                    }
                }

                private boolean isMouseInButton(MouseEvent e) {
                    Point buttonSize = clearButton.getSize();
                    return 0 <= e.x && e.x < buttonSize.x && 0 <= e.y && e.y < buttonSize.y;
                }
            });
            clearButton.addMouseTrackListener(new MouseTrackListener() {

                @Override
                public void mouseEnter(MouseEvent e) {
                    clearButton.setImage(activeImage);
                }

                @Override
                public void mouseExit(MouseEvent e) {
                    clearButton.setImage(inactiveImage);
                }

                @Override
                public void mouseHover(MouseEvent e) {
                }
            });
            clearButton.getAccessible().addAccessibleListener(new AccessibleAdapter() {

                @Override
                public void getName(AccessibleEvent e) {
                    e.result = ViewerMessages.FilteredTree_AccessibleListenerClearButton;
                }
            });
            clearButton.getAccessible().addAccessibleControlListener(new AccessibleControlAdapter() {

                @Override
                public void getRole(AccessibleControlEvent e) {
                    e.detail = ACC.ROLE_PUSHBUTTON;
                }
            });
            this.clearButtonControl = clearButton;
        }
    }

    /**
     * Clears the text in the filter text widget.
     */
    protected void clearText() {
        setFilterText(""); //$NON-NLS-1$
        textChanged();
    }

    /**
     * Set the text in the filter control.
     *
     * @param string
     */
    protected void setFilterText(String string) {
        if (this.filterText != null) {
            this.filterText.setText(string);
            selectAll();
        }
    }

    /**
     * Returns the pattern filter used by this tree.
     *
     * @return The pattern filter; never <code>null</code>.
     */
    public final PatternFilter getPatternFilter() {
        return this.patternFilter;
    }

    /**
     * Get the tree viewer of the receiver.
     *
     * @return the tree viewer
     */
    public TreeViewer getViewer() {
        return this.treeViewer;
    }

    /**
     * Get the filter text for the receiver, if it was created. Otherwise return <code>null</code>.
     *
     * @return the filter Text, or null if it was not created
     */
    public Text getFilterControl() {
        return this.filterText;
    }

    /**
     * Convenience method to return the text of the filter control. If the text widget is not
     * created, then null is returned.
     *
     * @return String in the text, or null if the text does not exist
     */
    protected String getFilterString() {
        return this.filterText != null ? this.filterText.getText() : null;
    }

    /**
     * Set the text that will be shown until the first focus. A default value is provided, so this
     * method only need be called if overriding the default initial text is desired.
     *
     * @param text initial text to appear in text field
     */
    public void setInitialText(String text) {
        this.initialText = text;
        if (this.filterText != null) {
            this.filterText.setMessage(text);
            if (this.filterText.isFocusControl()) {
                setFilterText(this.initialText);
                textChanged();
            } else {
                getDisplay().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        if (!FilteredTree.this.filterText.isDisposed() && FilteredTree.this.filterText.isFocusControl()) {
                            setFilterText(FilteredTree.this.initialText);
                            textChanged();
                        }
                    }
                });
            }
        } else {
            setFilterText(this.initialText);
            textChanged();
        }
    }

    /**
     * Select all text in the filter text field.
     *
     */
    protected void selectAll() {
        if (this.filterText != null) {
            this.filterText.selectAll();
        }
    }

    /**
     * Get the initial text for the receiver.
     *
     * @return String
     */
    protected String getInitialText() {
        return this.initialText;
    }

    /**
     * Return a bold font if the given element matches the given pattern. Clients can opt to call
     * this method from a Viewer's label provider to get a bold font for which to highlight the
     * given element in the tree.
     *
     * @param element element for which a match should be determined
     * @param tree FilteredTree in which the element resides
     * @param filter PatternFilter which determines a match
     *
     * @return bold font
     */
    public static Font getBoldFont(Object element, FilteredTree tree, PatternFilter filter) {
        String filterText = tree.getFilterString();

        if (filterText == null) {
            return null;
        }

        // Do nothing if it's empty string
        String initialText = tree.getInitialText();
        if (!filterText.equals("") && !filterText.equals(initialText)) {//$NON-NLS-1$
            if (tree.getPatternFilter() != filter) {
                boolean initial = initialText != null && initialText.equals(filterText);
                if (initial) {
                    filter.setPattern(null);
                } else if (filterText != null) {
                    filter.setPattern(filterText);
                }
            }
            if (filter.isElementVisible(tree.getViewer(), element) && filter.isLeafMatch(tree.getViewer(), element)) {
                return JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT);
            }
        }
        return null;
    }

    public boolean isShowFilterControls() {
        return this.showFilterControls;
    }

    public void setShowFilterControls(boolean showFilterControls) {
        if (isShowFilterControls() == showFilterControls) {
            return;
        }

        this.showFilterControls = showFilterControls;
        if (this.filterComposite != null) {
            Object filterCompositeLayoutData = this.filterComposite.getLayoutData();
            if (filterCompositeLayoutData instanceof GridData) {
                ((GridData) filterCompositeLayoutData).exclude = !isShowFilterControls();
            } else if (filterCompositeLayoutData instanceof RowData) {
                ((RowData) filterCompositeLayoutData).exclude = !isShowFilterControls();
            }
            this.filterComposite.setVisible(isShowFilterControls());
            layout();
        }
    }

    /**
     * Custom tree viewer subclass that clears the caches in patternFilter on any change to the
     * tree. See bug 187200.
     *
     * @since 3.3
     *
     */
    class NotifyingTreeViewer extends TreeViewer {

        /**
         * @param parent
         * @param style
         */
        public NotifyingTreeViewer(Composite parent, int style) {
            super(parent, style);
        }

        @Override
        public void add(Object parentElementOrTreePath, Object childElement) {
            getPatternFilter().clearCaches();
            super.add(parentElementOrTreePath, childElement);
        }

        @Override
        public void add(Object parentElementOrTreePath, Object[] childElements) {
            getPatternFilter().clearCaches();
            super.add(parentElementOrTreePath, childElements);
        }

        @Override
        protected void inputChanged(Object input, Object oldInput) {
            getPatternFilter().clearCaches();
            super.inputChanged(input, oldInput);
        }

        @Override
        public void insert(Object parentElementOrTreePath, Object element, int position) {
            getPatternFilter().clearCaches();
            super.insert(parentElementOrTreePath, element, position);
        }

        @Override
        public void refresh() {
            getPatternFilter().clearCaches();
            super.refresh();
        }

        @Override
        public void refresh(boolean updateLabels) {
            getPatternFilter().clearCaches();
            super.refresh(updateLabels);
        }

        @Override
        public void refresh(Object element) {
            getPatternFilter().clearCaches();
            super.refresh(element);
        }

        @Override
        public void refresh(Object element, boolean updateLabels) {
            getPatternFilter().clearCaches();
            super.refresh(element, updateLabels);
        }

        @Override
        public void remove(Object elementsOrTreePaths) {
            getPatternFilter().clearCaches();
            super.remove(elementsOrTreePaths);
        }

        @Override
        public void remove(Object parent, Object[] elements) {
            getPatternFilter().clearCaches();
            super.remove(parent, elements);
        }

        @Override
        public void remove(Object[] elementsOrTreePaths) {
            getPatternFilter().clearCaches();
            super.remove(elementsOrTreePaths);
        }

        @Override
        public void replace(Object parentElementOrTreePath, int index, Object element) {
            getPatternFilter().clearCaches();
            super.replace(parentElementOrTreePath, index, element);
        }

        @Override
        public void setChildCount(Object elementOrTreePath, int count) {
            getPatternFilter().clearCaches();
            super.setChildCount(elementOrTreePath, count);
        }

        @Override
        public void setContentProvider(IContentProvider provider) {
            getPatternFilter().clearCaches();
            super.setContentProvider(provider);
        }

        @Override
        public void setHasChildren(Object elementOrTreePath, boolean hasChildren) {
            getPatternFilter().clearCaches();
            super.setHasChildren(elementOrTreePath, hasChildren);
        }
    }

}
