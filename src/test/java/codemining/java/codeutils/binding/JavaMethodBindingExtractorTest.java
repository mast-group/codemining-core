/**
 * 
 */
package codemining.java.codeutils.binding;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.Test;

import codemining.java.codeutils.EclipseASTExtractorTest;
import codemining.languagetools.NameBinding;

public class JavaMethodBindingExtractorTest {

	File classContent;

	File classContent2;

	String methodContent;

	@Before
	public void setUp() throws IOException {
		classContent = new File(EclipseASTExtractorTest.class.getClassLoader()
				.getResource("SampleClass.txt").getFile());
		classContent2 = new File(EclipseASTExtractorTest.class.getClassLoader()
				.getResource("SampleClass2.txt").getFile());

		methodContent = FileUtils.readFileToString(new File(
				EclipseASTExtractorTest.class.getClassLoader()
						.getResource("SampleMethod.txt").getFile()));
	}

	@Test
	public void testClassLevelBindings() throws IOException {
		final JavaMethodBindingExtractor jame = new JavaMethodBindingExtractor();

		final List<NameBinding> classMethodBindings = jame
				.getNameBindings(classContent);

		JavaExactVariableBindingsExtractorTest
				.checkAllBindings(classMethodBindings);
		assertEquals(classMethodBindings.size(), 8);

		final List<NameBinding> classMethodBindings2 = jame
				.getNameBindings(classContent2);

		assertEquals(classMethodBindings2.size(), 7);
	}

}
