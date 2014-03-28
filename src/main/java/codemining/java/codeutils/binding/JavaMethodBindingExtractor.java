/**
 * 
 */
package codemining.java.codeutils.binding;

import java.util.List;
import java.util.SortedMap;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import codemining.java.codeutils.JavaASTExtractor;
import codemining.java.codeutils.JavaTokenizer;
import codemining.languagetools.NameBinding;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;

/**
 * Extract Java method bindings by using similar named method calls and
 * definitions.
 * 
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class JavaMethodBindingExtractor extends
		AbstractJavaNameBindingsExtractor {

	private static class MethodBindings extends ASTVisitor {
		/**
		 * A map from the method name to the position.
		 */
		Multimap<String, Integer> methodNamePostions = HashMultimap.create();

		final SortedMap<Integer, Integer> positionToIndex;

		public MethodBindings(final SortedMap<Integer, Integer> positionToIndex) {
			this.positionToIndex = positionToIndex;
		}

		@Override
		public boolean visit(final MethodDeclaration node) {
			if (node.isConstructor()) {
				return super.visit(node);
			}
			final String name = node.getName().toString();
			methodNamePostions.put(name,
					positionToIndex.get(node.getName().getStartPosition()));
			return super.visit(node);
		}

		@Override
		public boolean visit(final MethodInvocation node) {
			final String name = node.getName().toString();
			methodNamePostions.put(name,
					positionToIndex.get(node.getName().getStartPosition()));
			return super.visit(node);
		}
	}

	@Override
	protected JavaASTExtractor createExtractor() {
		return new JavaASTExtractor(false);
	}

	@Override
	public List<NameBinding> getNameBindings(final ASTNode node,
			final String sourceCode) {
		final JavaTokenizer tokenizer = new JavaTokenizer();
		final SortedMap<Integer, String> tokenPositions = tokenizer
				.tokenListWithPos(sourceCode.toCharArray());
		final SortedMap<Integer, Integer> positionToIndex = getTokenIndexForPostion(tokenPositions);
		final List<String> codeTokens = Lists.newArrayList(tokenPositions
				.values());

		final MethodBindings mb = new MethodBindings(positionToIndex);
		node.accept(mb);

		final List<NameBinding> bindings = Lists.newArrayList();
		for (final String methodName : mb.methodNamePostions.keySet()) {
			final NameBinding binding = new NameBinding(
					Lists.newArrayList(mb.methodNamePostions.get(methodName)),
					codeTokens);
			bindings.add(binding);
		}

		return bindings;
	}

}
