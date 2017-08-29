package org.eclipse.buildship.ui.editor;

import java.util.LinkedList;
import java.util.List;

/** 
 * Gradle file structure
 * 
 * parsing only first level {} block, i.e. it is made not to be perfect, but minimal enough to give Outline
 * 
 *  Does not depend on Groovy, Greclipse or Gradle Tooling API
 * */
//specially made package private
class GradlePage {
	
	static class Element{
		public static final Element EMPTY = new Element("");
		
		String name;
		int line;
		int position;
		
		Element() {			
		}
		Element(String string) {
			name = string;
		}		
	}
	
	static class Node extends Element{
		//the same as Element plus:
		List<Node> children = new LinkedList<Node>();

		Node(String string) {
			super(string);
		}
		Node(Element el) {
			name = el.name;
			line = el.line;
			position = el.position;
		}
		
		@Override
		public String toString() {
			return name+" (L"+line+','+position+")";
		}
	}
	
	String text = "";
	int curLine = 1;
	int curPosition = 1;
	int curIndex = 0;
	List<Element> elements = new LinkedList<Element>();
	List<Node> nodes = new LinkedList<Node>();

	GradlePage(String text) {
		this.text=text;
		parse();
	}

	void parse() {
		Element lexem = nextLexem();	
		while ( lexem != Element.EMPTY ){
			if (lexem.name.equals("{")){
				createNode(lexem);
				parseBlock();
			}
			lexem = nextLexem();
		}
	}//parse
	
	private void createNode(Element el) {
		// get previous
		int index = elements.indexOf(el);
		if (index>0){ //get previous
			index--;
			el = elements.get(index);			
		}
		nodes.add(new Node(el));
		
	}//createNode


	private void parseBlock() {
		Element prev, lexem;
		lexem = nextLexem();
		while ( !lexem.name.equals("}") && lexem != Element.EMPTY ){
			if (lexem.name.equals("{")) {
				createNode(lexem);
				parseBlock();
			}
			prev = lexem;
			lexem = nextLexem();
		}
	}


	private Element nextLexem() {
		Element el = nextLexemWithoutLog();
		elements.add(el);
		log(el.name);
		//log(el.toString());
		return el; 
	}


	public static final String LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
	public static final String LETTERS_ALL_CASES = LETTERS + LETTERS.toLowerCase();
	public static final String LETTERS_ALL_CASES_AND_UNDERLINE = LETTERS_ALL_CASES + '_';
	public static final String DIGITS = "0123456789";
	public static final String NUMBER_PARTS = DIGITS + ".eE";
	
	/** get lexical element literal, number or other like (){}/*-+
	 */
	//TODO comments and strings
	private Element nextLexemWithoutLog() {
		if (curIndex >= text.length()) {
			return Element.EMPTY;
		}
		char c;
		// skip white spaces
		loop1: while (curIndex < text.length()) {
			c = text.charAt(curIndex);
			switch (c) {
				case '\n': {
					curLine++;
					curPosition = 1;
					break;
				}
				//TODO look at Character.isWhitespace(c)
				case '\r':
				case '\t':
				case ' ':
					break;
				default:
					break loop1;
			}
			curIndex++;
		}
		
		if (curIndex >= text.length()) {// when file ends with spaces or new line
			return Element.EMPTY;
		}
		
		c = text.charAt(curIndex);
		if (LETTERS_ALL_CASES.indexOf(c, 0) >= 0){
//			Element el = new Element();
//			el.line = curLine;
//			el.position = curPosition;
//			el.name = ""+c;
//			
//			curIndex++;
//			while (curIndex < text.length()) {
//				c = text.charAt(curIndex);
//				if (LETTERS_ALL_CASES_AND_UNDERLINE.indexOf(c, 0) < 0){
//					return el;
//				}
//				el.name += c;
//				curIndex++;
//			}
//			return el; //when file ends with literal
			return readElementTail(c, LETTERS_ALL_CASES_AND_UNDERLINE);
		}
		//
		else if (DIGITS.indexOf(c, 0) >= 0){
			return readElementTail(c, NUMBER_PARTS);
		}
		
//		Element el = new Element();
//		el.line = curLine;
//		el.position = curPosition;
//		el.name = ""+c;
//		curIndex++;
//		
//		return el;
		
		return readElementTail(c, "");
	}//nextLexem


	private Element readElementTail(char c, final String CSET) {
		Element el = new Element();
		el.line = curLine;
		el.position = curPosition;
		el.name = ""+c;		
		curIndex++;
		
		while (curIndex < text.length()) {
			c = text.charAt(curIndex);
			if (CSET.indexOf(c, 0) < 0){
				return el;
			}
			el.name += c;
			curIndex++;
		}
		return el; //when file ends with literal			
	}
	
	static void log(String s){
		System.out.println(s);
	}
	
}
