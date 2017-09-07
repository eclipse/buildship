package org.eclipse.buildship.ui.editor;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;



public class GradleOutline extends ContentOutlinePage {

	private final IDocumentProvider fDocumentProvider;
	private final GradleEditor fTextEditor;
	private Object fInput = null;

	public GradleOutline(IDocumentProvider documentProvider, GradleEditor gradleEditor) {
		this.fDocumentProvider=documentProvider;
		this.fTextEditor=gradleEditor;
	}

	/**
	 * Sets the input of the outline page
	 *
	 * @param input
	 *            the input of this outline page
	 */
	public void setInput(Object input) {
		fInput = input;
		update();
	}

	/**
	 * Updates the outline page.
	 */
	public void update() {
		TreeViewer viewer = getTreeViewer();

		if (viewer != null) {
			Control control = viewer.getControl();
			if (control != null && !control.isDisposed()) {
				control.setRedraw(false);
				viewer.setInput(fInput);
				viewer.expandAll();
				control.setRedraw(true);
			}
		}
	}
	
	/*
	 * (non-Javadoc) Method declared on ContentOutlinePage
	 */
	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		TreeViewer viewer = getTreeViewer();
		viewer.setContentProvider(new ContentProvider());
		//...
		viewer.addSelectionChangedListener(this);

		if (fInput != null)
			viewer.setInput(fInput);
		

	}
	
	public final class ContentProvider implements ITreeContentProvider, IDocumentListener {
		
		private GradlePage fContent;
		// protected List fContent= new ArrayList(10);
		private GradleEditor fTextEditor;

		private void parse() {
			if (fTextEditor==null){
				System.out.println("fTextEditor==null");
				return;
			}
			fContent = fTextEditor.getGradlePage();
		}


		/*
		 * @see IContentProvider#inputChanged(Viewer, Object, Object)
		 */
		@Override
		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// Detach from old
			if (oldInput != null) {
				IDocument document = fDocumentProvider.getDocument(oldInput);
				if (document != null) {
					document.removeDocumentListener(this);
				}
			}
			fContent = null;
			// Attach to new
			if (newInput == null)
				return;
			IDocument document = fDocumentProvider.getDocument(newInput);
			if (document == null)
				return;
			fTextEditor = GradleEditor.getEditor(document);
			document.addDocumentListener(this);
			parse();
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
			// nothing
		}

		@Override
		public void documentChanged(DocumentEvent event) {
			parse();
			update();
		}
		
		/*
		 * @see IContentProvider#dispose
		 */
		@Override
		public void dispose() {
			fContent = null;
		}

//		/*
//		 * @see IContentProvider#isDeleted(Object)
//		 */
//		@Override
//		public boolean isDeleted(Object element) {
//			return false;
//		}

		/*
		 * @see IStructuredContentProvider#getElements(Object)
		 */
		@Override
		public Object[] getElements(Object inputElement) {
//			//TODO
//			if (fContent==null){
//				System.out.println("fContent==null");
//				return null;
//			}
//			if (fContent.nodes==null){
//				System.out.println("fContent.nodes==null");
//				return null;
//			}
			return fContent.nodes.toArray();
		}

		/*
		 * @see ITreeContentProvider#hasChildren(Object)
		 */
		@Override
		public boolean hasChildren(Object element) {
			if (element == fInput) {
				return true;
			}
			if (element instanceof GradlePage.Node) {
				GradlePage.Node node = (GradlePage.Node) element;
				//return header.getSubHeaders().size() > 0;
				return false;
			}
			;
			return false;
		}
		
		/*
		 * @see ITreeContentProvider#getParent(Object)
		 */
		@Override
		public Object getParent(Object element) {
			if (!(element instanceof GradlePage.Node))
				return null;
			//TODO
			return null;
		}
		
		/*
		 * @see ITreeContentProvider#getChildren(Object)
		 */
		@Override
		public Object[] getChildren(Object element) {
			if (element == fInput) {
				return getElements(null);
			}
			if (!(element instanceof GradlePage.Node))
				return null;
			//return ((MarkdownPage.Header) element).getSubHeaders().toArray();
			//TODO
			return null;
		}


		
	}
	
}
