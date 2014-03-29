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
import codemining.languagetools.TokenNameBinding;

import com.google.common.collect.Sets;

public class JavaExactVariableBindingsExtractorTest {

	public static void checkAllBindings(final List<TokenNameBinding> bindings) {
		final Set<Integer> indexes = Sets.newHashSet();
		for (final TokenNameBinding binding : bindings) {
			checkBinding(binding);
			assertFalse("Indexes appear only once",
					indexes.removeAll(binding.nameIndexes));
			indexes.addAll(binding.nameIndexes);
		}
	}

	public static void checkBinding(final TokenNameBinding binding) {
		final String tokenName = binding.sourceCodeTokens
				.get(binding.nameIndexes.iterator().next());
		for (final int idx : binding.nameIndexes) {
			assertEquals(tokenName, binding.sourceCodeTokens.get(idx));
		}
	}

	File classContent;

	File classContent2;

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
		final List<TokenNameBinding> classVariableBindings = jbe
				.getNameBindings(classContent);
		checkAllBindings(classVariableBindings);
		assertEquals(classVariableBindings.size(), 5);

		final List<TokenNameBinding> classVariableBindings2 = jbe
				.getNameBindings(classContent2);

		assertEquals(classVariableBindings2.size(), 9);
	}
}
