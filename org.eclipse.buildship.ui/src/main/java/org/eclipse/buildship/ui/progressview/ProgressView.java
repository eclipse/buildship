package org.eclipse.buildship.ui.progressview;

import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.beans.IBeanValueProperty;
import org.eclipse.core.databinding.observable.set.IObservableSet;
import org.eclipse.core.databinding.property.list.IListProperty;
import org.eclipse.jface.databinding.viewers.ObservableListTreeContentProvider;
import org.eclipse.jface.databinding.viewers.ObservableMapCellLabelProvider;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.ViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import org.eclipse.buildship.core.CorePlugin;
import org.eclipse.buildship.ui.progressview.listener.BuildLaunchRequestListener;
import org.eclipse.buildship.ui.progressview.listener.ProgressItemCreatedListener;
import org.eclipse.buildship.ui.progressview.model.ProgressItem;
import org.eclipse.buildship.ui.progressview.model.internal.ProgressChildrenListProperty;
import org.eclipse.buildship.ui.viewer.FilteredTree;
import org.eclipse.buildship.ui.viewer.PatternFilter;
import org.eclipse.buildship.ui.viewer.labelprovider.ObservableMapCellWithIconLabelProvider;

public class ProgressView extends ViewPart {

	private FilteredTree filteredTree;

	private TreeViewerColumn labelColumn;

	private TreeViewerColumn durationColumn;

	private ProgressItem root = new ProgressItem(null);

	@Override
	public void createPartControl(Composite parent) {

		filteredTree = new FilteredTree(parent, SWT.BORDER, new PatternFilter());
		filteredTree.setShowFilterControls(false);
		filteredTree.getViewer().getTree().setHeaderVisible(true);

		createViewerColumns();

		bindUI();

		registerBuildLaunchRequestListener();

		registerExpandTreeOnNewProgressListener();

		getSite().setSelectionProvider(filteredTree.getViewer());
	}


    @Override
    public void setFocus() {
    	filteredTree.getViewer().getControl().setFocus();
    }


    protected void createViewerColumns() {
        labelColumn = new TreeViewerColumn(
				filteredTree.getViewer(), SWT.NONE);
		labelColumn.getColumn().setText("Operations");
		labelColumn.getColumn().setWidth(450);

		durationColumn = new TreeViewerColumn(
				filteredTree.getViewer(), SWT.NONE);
		durationColumn.getColumn().setText("Duration");
		durationColumn.getColumn().setWidth(200);
    }


    protected void registerBuildLaunchRequestListener() {
		CorePlugin.eventBus().register(new BuildLaunchRequestListener(root));
	}

	protected void registerExpandTreeOnNewProgressListener() {
	    CorePlugin.eventBus().register(new ProgressItemCreatedListener(filteredTree.getViewer()));
	}

	private void bindUI() {
		IListProperty childrenProperty = new ProgressChildrenListProperty();

		ObservableListTreeContentProvider contentProvider = new ObservableListTreeContentProvider(
				childrenProperty.listFactory(), null);
		filteredTree.getViewer().setContentProvider(contentProvider);

		IObservableSet knownElements = contentProvider.getKnownElements();
		attachLabelProvider("label", "image", knownElements, labelColumn);
		attachLabelProvider("duration", null, knownElements, durationColumn);

		filteredTree.getViewer().setInput(root);
	}

	private void attachLabelProvider(String textProperty, String imageProperty,IObservableSet knownElements, ViewerColumn viewerColumn) {
		IBeanValueProperty txtProperty = BeanProperties.value(textProperty);
		if(imageProperty != null) {
		    IBeanValueProperty imgProperty = BeanProperties.value(imageProperty);
		    viewerColumn.setLabelProvider(new ObservableMapCellWithIconLabelProvider(txtProperty.observeDetail(knownElements), imgProperty.observeDetail(knownElements)));
		}else {
		    viewerColumn.setLabelProvider(new ObservableMapCellLabelProvider(txtProperty.observeDetail(knownElements)));
        }
	}

}
