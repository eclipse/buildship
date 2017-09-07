package org.eclipse.buildship.ui.editor;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * copied from NodeclipseNodejsEditor
 * TODO consider base classes
 * 
 * @author Paul Verest
 */
public class GradleEditor extends TextEditor {

    public static final String EDITOR_ID = "org.nodeclipse.enide.editors.gradle.editors.GradleEditor";
    public static final String RULER_CONTEXT = EDITOR_ID + ".ruler";
    public final static String EDITOR_MATCHING_BRACKETS = "matchingBrackets";
    public final static String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor";

    private DefaultCharacterPairMatcher matcher;
    
    //+
    private GradleOutline fOutlinePage = null;
    private boolean pageDirty = true;
	private GradlePage page;
	IDocument oldDoc = null; //used only in doSetInput(IEditorInput input)

    public GradleEditor() {
        setSourceViewerConfiguration(new NodeSourceViewerConfiguration());
    }

    @Override
    protected void initializeEditor() {
        super.initializeEditor();
        setRulerContextMenuId(RULER_CONTEXT);
        setDocumentProvider(new NodeDocumentProvider());
    }

    @Override
    protected void configureSourceViewerDecorationSupport(SourceViewerDecorationSupport support) {
        super.configureSourceViewerDecorationSupport(support);

        char[] matchChars = { '(', ')', '[', ']', '{', '}' }; // which brackets
                                                              // to match
        matcher = new DefaultCharacterPairMatcher(matchChars, IDocumentExtension3.DEFAULT_PARTITIONING);
        support.setCharacterPairMatcher(matcher);
        support.setMatchingCharacterPainterPreferenceKeys(EDITOR_MATCHING_BRACKETS, EDITOR_MATCHING_BRACKETS_COLOR);

        // Enable bracket highlighting in the preference store
        IPreferenceStore store = getPreferenceStore();
        store.setDefault(EDITOR_MATCHING_BRACKETS, true);
        store.setDefault(EDITOR_MATCHING_BRACKETS_COLOR, "128,128,128");
    }
    
    //+ block for Outline taken from Winterstein Markdown Editor {
	public Object getAdapter(Class required) {
		if (IContentOutlinePage.class.equals(required)) {
			if (fOutlinePage == null) {
				fOutlinePage= new GradleOutline(getDocumentProvider(), this);
				if (getEditorInput() != null)
					fOutlinePage.setInput(getEditorInput());
			}
			return fOutlinePage;
		}
		return super.getAdapter(required);
	}

	public GradlePage getGradlePage() {
		if (pageDirty) updateGradlePage();
		return page;
	}

	private void updateGradlePage() {
		String text = getText();
		if (text==null) text="";
		page = new GradlePage(text); //GradleParser.parse(text); /
		pageDirty = false;
	}
	
	/**
	 * @return The text of the editor's document, or null if unavailable.
	 */
	public String getText() {
		IDocument doc = getDocument();
		return doc==null? null : doc.get();
	}
	
	public IDocument getDocument() {
		IEditorInput input = getEditorInput();
		IDocumentProvider docProvider = getDocumentProvider();		
		return docProvider==null? null : docProvider.getDocument(input);
	}

	public static GradleEditor getEditor(IDocument doc) {
		return doc2editor.get(doc);
	}

	private static final Map<IDocument, GradleEditor> doc2editor = new HashMap<IDocument, GradleEditor>();
   
	@Override
	protected void doSetInput(IEditorInput input) throws CoreException {
		// Detach from old
		if (oldDoc!= null) {
			//oldDoc.removeDocumentListener(this);
			if (doc2editor.get(oldDoc) == this) doc2editor.remove(oldDoc);
		}
		// Set
		super.doSetInput(input);		
		// Attach as a listener to new doc
		IDocument doc = getDocument();
		oldDoc = doc;
		if (doc==null) return;		
		//doc.addDocumentListener(this);
		doc2editor.put(doc, this);
//		// Initialise code folding
//		haveRunFolding = false;
//		updateSectionFoldingAnnotations(null);
	}
	//}
}

