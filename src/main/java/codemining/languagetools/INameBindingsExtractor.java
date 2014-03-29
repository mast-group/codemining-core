/**
 * 
 */
package codemining.languagetools;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * A NameBindings extractor from arbitrary code.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public interface INameBindingsExtractor {
	/**
	 * Get the name bindings for the given file.
	 * 
	 * @param f
	 * @return
	 * @throws IOException
	 */
	List<TokenNameBinding> getNameBindings(final File f) throws IOException;

	/**
	 * Get the name bindings given the code.
	 * 
	 * @param code
	 * @return
	 */
	List<TokenNameBinding> getNameBindings(final String code);
}
