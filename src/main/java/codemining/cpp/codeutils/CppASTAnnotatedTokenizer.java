/**
 * 
 */
package codemining.cpp.codeutils;

import codemining.languagetools.ITokenizer;

/**
 * A C++ AST Annotated Tokenizer
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class CppASTAnnotatedTokenizer extends AbstractCdtASTAnnotatedTokenizer {

	private static final long serialVersionUID = -8016456170070671980L;

	/**
	 * 
	 */
	public CppASTAnnotatedTokenizer() {
		super(CppASTExtractor.class);
	}

	/**
	 * @param base
	 */
	public CppASTAnnotatedTokenizer(ITokenizer base) {
		super(base, CppASTExtractor.class);
	}

}
