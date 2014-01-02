/**
 * 
 */
package codemining.cpp.codeutils;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.NullCodeReaderFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * A C AST Extractor.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class CppASTExtractor implements ICdtAstExtractor {

	/* (non-Javadoc)
	 * @see codemining.cpp.codeutils.ICDTASTExtractor#getAST(char[])
	 */
	@Override
	public IASTTranslationUnit getAST(final char[] code) throws CoreException {
		final ScannerInfo scInfo = new ScannerInfo();
		final IASTTranslationUnit ast = GPPLanguage.getDefault()
				.getASTTranslationUnit(new CodeReader(code), scInfo,
						NullCodeReaderFactory.getInstance(), null,
						new IParserLogService() {

							@Override
							public boolean isTracing() {
								return false;
							}

							@Override
							public void traceLog(String arg0) {
							}

						});
		return ast;

	}

}
