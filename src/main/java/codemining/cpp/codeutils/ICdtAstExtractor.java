package codemining.cpp.codeutils;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;

/**
 * Inteface for all classes that are able to retrieve a CDT-compatible AST.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public interface ICdtAstExtractor {

	/**
	 * Return an AST for the following CDT-compatible code;
	 * 
	 * @param code
	 * @return
	 * @throws CoreException
	 */
	public abstract IASTTranslationUnit getAST(char[] code)
			throws CoreException;

}