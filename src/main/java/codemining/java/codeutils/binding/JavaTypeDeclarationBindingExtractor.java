/**
 *
 */
package codemining.java.codeutils.binding;

import static com.google.common.base.Preconditions.checkArgument;

import java.util.Set;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import codemining.java.tokenizers.JavaTokenizer;
import codemining.languagetools.ITokenizer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

/**
 * Extract Java type name bindings.
 *
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 *
 */
public class JavaTypeDeclarationBindingExtractor extends
		AbstractJavaNameBindingsExtractor {

	private static class ClassnameFinder extends ASTVisitor {

		final Multimap<String, ASTNode> classNamePostions = HashMultimap
				.create();

		@Override
		public boolean visit(final TypeDeclaration node) {
			classNamePostions.put(node.getName().getIdentifier(),
					node.getName());
			return super.visit(node);
		}
	}

	public JavaTypeDeclarationBindingExtractor() {
		super(new JavaTokenizer());
	}

	public JavaTypeDeclarationBindingExtractor(final ITokenizer tokenizer) {
		super(tokenizer);
	}

	@Override
	protected Set<String> getFeatures(final Set<ASTNode> boundNodes) {
		checkArgument(boundNodes.size() == 1);
		final ASTNode decl = boundNodes.iterator().next().getParent();

		checkArgument(decl instanceof TypeDeclaration);
		final TypeDeclaration td = (TypeDeclaration) decl;
		final String currentTypeName = td.getName().getIdentifier();
		final Set<String> features = Sets.newHashSet();
		if (td.isInterface()) {
			features.add("isInterface");
		}

		for (final Object suptype : td.superInterfaceTypes()) {
			final Type supertype = (Type) suptype;
			for (final String namePart : JavaFeatureExtractor
					.getNameParts(supertype.toString())) {
				features.add("implementVoc:" + namePart);
			}
		}

		if (td.getSuperclassType() != null) {
			for (final String namePart : JavaFeatureExtractor.getNameParts(td
					.getSuperclassType().toString())) {
				features.add("implementVoc:" + namePart);
			}
		}

		for (final FieldDeclaration fd : td.getFields()) {
			for (final Object vdf : fd.fragments()) {
				final VariableDeclarationFragment frag = (VariableDeclarationFragment) vdf;
				for (final String namePart : JavaFeatureExtractor
						.getNameParts(frag.getName().toString())) {
					features.add("fieldVoc:" + namePart);
				}
			}
			if (!currentTypeName.equals(fd.getType().toString())) {
				features.add("fieldType:" + fd.getType().toString());
				for (final String namePart : JavaFeatureExtractor
						.getNameParts(fd.getType().toString())) {
					features.add("fieldVoc:" + namePart);
				}
			}
		}

		for (final MethodDeclaration md : td.getMethods()) {
			if (md.isConstructor()) {
				continue;
			}
			for (final String namePart : JavaFeatureExtractor.getNameParts(md
					.getName().getIdentifier())) {
				features.add("methodVoc:" + namePart);
			}
			for (final Object arg : md.parameters()) {
				final SingleVariableDeclaration svd = (SingleVariableDeclaration) arg;
				for (final String namePart : JavaFeatureExtractor
						.getNameParts(svd.getName().toString())) {
					features.add("methodVoc:" + namePart);
				}
				if (!svd.getType().toString().equals(currentTypeName)) {
					for (final String namePart : JavaFeatureExtractor
							.getNameParts(svd.getType().toString())) {
						features.add("methodVoc:" + namePart);
					}
				}
			}
		}

		return features;
	}

	@Override
	public Set<Set<ASTNode>> getNameBindings(final ASTNode node) {
		final ClassnameFinder finder = new ClassnameFinder();
		node.accept(finder);

		final Set<Set<ASTNode>> nameBindings = Sets.newHashSet();
		for (final String methodName : finder.classNamePostions.keySet()) {
			final Set<ASTNode> boundNodes = Sets.newIdentityHashSet();
			boundNodes.addAll(finder.classNamePostions.get(methodName));
			nameBindings.add(boundNodes);
		}
		return nameBindings;
	}

}
