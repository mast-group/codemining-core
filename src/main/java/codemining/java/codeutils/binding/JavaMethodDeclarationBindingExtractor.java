/**
 *
 */
package codemining.java.codeutils.binding;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

import codemining.java.codedata.metrics.CyclomaticCalculator;
import codemining.java.tokenizers.JavaTokenizer;
import codemining.languagetools.ITokenizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Extract bindings (and features) for method delarations.
 *
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 *
 */
public class JavaMethodDeclarationBindingExtractor

extends AbstractJavaNameBindingsExtractor {

	private static class MethodBindings extends ASTVisitor {
		/**
		 * A map from the method name to the position.
		 */
		Multimap<String, ASTNode> methodNamePostions = HashMultimap.create();

		@Override
		public boolean visit(final MethodDeclaration node) {
			if (node.isConstructor()) {
				return super.visit(node);
			}
			final String name = node.getName().toString();
			methodNamePostions.put(name, node.getName());
			return super.visit(node);
		}
	}

	public JavaMethodDeclarationBindingExtractor() {
		super(new JavaTokenizer());
	}

	public JavaMethodDeclarationBindingExtractor(final ITokenizer tokenizer) {
		super(tokenizer);
	}

	@Override
	protected Set<String> getFeatures(final Set<ASTNode> boundNodes) {
		checkArgument(boundNodes.size() == 1);
		final ASTNode method = boundNodes.iterator().next().getParent();
		final Set<String> features = Sets.newHashSet();

		checkArgument(method instanceof MethodDeclaration);
		final MethodDeclaration md = (MethodDeclaration) method;
		features.add("nParams:" + md.parameters().size());
		for (int i = 0; i < md.parameters().size(); i++) {
			final SingleVariableDeclaration varDecl = (SingleVariableDeclaration) md
					.parameters().get(i);
			features.add("param" + i + "Type:" + varDecl.getType().toString());
			features.add("param" + i + "Name:" + varDecl.getName().toString());
		}

		if (md.isVarargs()) {
			features.add("isVarArg");
		}

		for (final Object exception : md.thrownExceptionTypes()) {
			final SimpleType ex = (SimpleType) exception;
			features.add("thrownException:" + ex.toString());
		}

		features.add("returnType:" + md.getReturnType2());
		JavaFeatureExtractor.addModifierFeatures(features, md.modifiers());

		if (md.getBody() == null) {
			features.add("isInterfaceDeclaration");
		}

		JavaFeatureExtractor.addAstAncestryFeatures(features, method);
		JavaFeatureExtractor.getMethodTopicFeatures(md, features);
		JavaFeatureExtractor.addImplementorVocab(method, features);
		features.add("cyclomatic:"
				+ (int) (new CyclomaticCalculator().getMetricForASTNode(method)));
		return features;
	}

	@Override
	public Set<Set<ASTNode>> getNameBindings(final ASTNode node) {
		final MethodBindings mb = new MethodBindings();
		node.accept(mb);

		final Set<Set<ASTNode>> nameBindings = Sets.newHashSet();
		for (final Entry<String, ASTNode> entry : mb.methodNamePostions
				.entries()) {
			final Set<ASTNode> boundNodes = Sets.newIdentityHashSet();
			boundNodes.add(entry.getValue());
			nameBindings.add(boundNodes);
		}
		return nameBindings;
	}

}
