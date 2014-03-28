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

public class JavaTypeBindingExtractorTest {

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
		final JavaTypeBindingExtractor jame = new JavaTypeBindingExtractor();

		final List<NameBinding> classTypeindings = jame
				.getNameBindings(classContent);

		JavaExactVariableBindingsExtractorTest
				.checkAllBindings(classTypeindings);
		assertEquals(classTypeindings.size(), 7);

		final List<NameBinding> classTypeBindings2 = jame
				.getNameBindings(classContent2);
		JavaExactVariableBindingsExtractorTest
				.checkAllBindings(classTypeBindings2);

		assertEquals(classTypeBindings2.size(), 11);
	}

}
