/**
 * 
 */
package codemining.cpp.codeutils;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.parser.CodeReader;
import org.eclipse.cdt.core.parser.IParserLogService;
import org.eclipse.cdt.core.parser.ScannerInfo;
import org.eclipse.cdt.internal.core.dom.NullCodeReaderFactory;
import org.eclipse.core.runtime.CoreException;

/**
 * A C AST extractor.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class CAstExtractor implements ICdtAstExtractor {

	/*
	 * (non-Javadoc)
	 * 
	 * @see codemining.cpp.codeutils.ICDTASTExtractor#getAST(char[])
	 */
	@Override
	public IASTTranslationUnit getAST(char[] code) throws CoreException {
		final ScannerInfo scInfo = new ScannerInfo();
		final IASTTranslationUnit ast = GCCLanguage.getDefault()
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
