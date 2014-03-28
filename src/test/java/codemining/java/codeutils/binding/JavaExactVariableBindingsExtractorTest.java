package codemining.java.codeutils.binding;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import codemining.java.codeutils.EclipseASTExtractorTest;
import codemining.languagetools.NameBinding;

import com.google.common.collect.Sets;

public class JavaExactVariableBindingsExtractorTest {

	File classContent;
	File classContent2;

	public void checkAllBindings(final List<NameBinding> bindings) {
		final Set<Integer> indexes = Sets.newHashSet();
		for (final NameBinding binding : bindings) {
			checkBinding(binding);
			assertFalse("Indexes appear only once",
					indexes.removeAll(binding.nameIndexes));
			indexes.addAll(binding.nameIndexes);
		}
	}

	public void checkBinding(final NameBinding binding) {
		final String tokenName = binding.sourceCodeTokens
				.get(binding.nameIndexes.get(0));
		for (int i = 1; i < binding.nameIndexes.size(); i++) {
			assertEquals(tokenName,
					binding.sourceCodeTokens.get(binding.nameIndexes.get(i)));
		}
	}

	@Before
	public void setUp() throws IOException {
		classContent = new File(EclipseASTExtractorTest.class.getClassLoader()
				.getResource("SampleClass.txt").getFile());
		classContent2 = new File(EclipseASTExtractorTest.class.getClassLoader()
				.getResource("SampleClass2.txt").getFile());
	}

	@Test
	public void testClassBindings() throws IOException {
		final JavaExactVariableBindingsExtractor jbe = new JavaExactVariableBindingsExtractor();
		final List<NameBinding> classVariableBindings = jbe
				.getNameBindings(classContent);
		checkAllBindings(classVariableBindings);
		assertEquals(classVariableBindings.size(), 5);

		final List<NameBinding> classVariableBindings2 = jbe
				.getNameBindings(classContent2);

		assertEquals(classVariableBindings2.size(), 9);
	}
}
