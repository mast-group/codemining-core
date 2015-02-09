/**
 *
 */
package codemining.java.codeutils.binding;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.IExtendedModifier;
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

	private class MethodBindings extends ASTVisitor {
		/**
		 * A map from the method name to the position.
		 */
		Multimap<String, ASTNode> methodNamePostions = HashMultimap.create();

		private boolean isOverride(final MethodDeclaration node) {
			final List modifiers = node.modifiers();
			for (final Object mod : modifiers) {
				final IExtendedModifier modifier = (IExtendedModifier) mod;
				if (modifier.isAnnotation()) {
					final Annotation annotation = (Annotation) modifier;
					if (annotation.getTypeName().toString().equals("Override")) {
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean visit(final MethodDeclaration node) {
			if (node.isConstructor()) {
				return super.visit(node);
			} else if (!includeOverrides && isOverride(node)) {
				return super.visit(node);
			}
			final String name = node.getName().toString();
			methodNamePostions.put(name, node.getName());
			return super.visit(node);
		}
	}

	private final boolean includeOverrides;

	public JavaMethodDeclarationBindingExtractor() {
		super(new JavaTokenizer());
		this.includeOverrides = true;
	}

	public JavaMethodDeclarationBindingExtractor(final boolean includeOverrides) {
		super(new JavaTokenizer());
		this.includeOverrides = includeOverrides;
	}

	public JavaMethodDeclarationBindingExtractor(final ITokenizer tokenizer) {
		super(tokenizer);
		this.includeOverrides = true;
	}

	public JavaMethodDeclarationBindingExtractor(final ITokenizer tokenizer,
			final boolean includeOverrides) {
		super(tokenizer);
		this.includeOverrides = includeOverrides;
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
			for (final String namepart : JavaFeatureExtractor
					.getNameParts(varDecl.getName().toString())) {
				features.add("paramName:" + namepart);
			}
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
		JavaFeatureExtractor.addMethodTopicFeatures(md, features);
		JavaFeatureExtractor.addImplementorVocab(method, features);
		JavaFeatureExtractor.addFields(method, features);
		JavaFeatureExtractor.addSiblingMethodNames(md, features);
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
