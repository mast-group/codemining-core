/**
 * 
 */
package codemining.java.codedata.experimental;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.Stack;
import java.util.TreeMap;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

import codemining.java.codeutils.JavaASTExtractor;
import codemining.java.tokenizers.JavaTokenizer;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

/**
 * @author Miltos Allamanis <m.allamanis@ed.ac.uk>
 * 
 */
public class MethodSimilarity extends ASTVisitor {

	private static final Logger LOGGER = Logger
			.getLogger(MethodSimilarity.class.getName());

	private static Collection<File> getAllFiles(final String baseDir) {
		File baseDirectory = new File(baseDir);
		return FileUtils.listFiles(baseDirectory, new RegexFileFilter(
				".*\\.java$"), DirectoryFileFilter.DIRECTORY);
	}

	public static void main(String args[]) throws IOException,
			InvalidInputException {
		final MethodSimilarity m = new MethodSimilarity();
		final JavaASTExtractor astExtractor = new JavaASTExtractor(true);

		for (final File f : getAllFiles(args[0])) {
			try {
				final CompilationUnit cu = astExtractor.getAST(f);
				m.setFile(f);
				cu.accept(m);
			} catch (Exception e) {
				LOGGER.warning(ExceptionUtils.getFullStackTrace(e));
			}
		}
		m.normalizeSims();
		m.pruneRareCounts(.001);
		System.out.println(m.scoreSimilarity(.1, 5));
	}

	public SortedMap<Integer, String> allTokens;

	public TreeMap<String, Integer> methodCount = Maps.newTreeMap();

	/**
	 * Stack of list of the positions of methods in the document.
	 */
	final Stack<List<Integer>> internalMethodInvocations = new Stack<List<Integer>>();

	final Stack<Integer> methodStartPos = new Stack<Integer>();
	final Map<String, Map<Long, Double>> methodSim = new TreeMap<String, Map<Long, Double>>();
	final BiMap<String, Long> alphabet = HashBiMap.create();
	long nextMethodId = 0;
	// Window size in characters
	public static final int WINDOW_SIZE = 150;

	JavaTokenizer tokenizer = new JavaTokenizer();

	public void countNeighboursOf(SortedMap<Integer, String> localTokens,
			int methodInvocationPos) {
		final String methodName = localTokens.get(methodInvocationPos);

		if (methodCount.containsKey(methodName)) {
			methodCount.put(methodName, 1 + methodCount.get(methodName));
		} else {
			methodCount.put(methodName, 1);
		}
		// Get or set method window
		final Map<Long, Double> methodNeighbours;
		if (methodSim.containsKey(methodName)) {
			methodNeighbours = methodSim.get(methodName);
		} else {
			methodNeighbours = Maps.newTreeMap();
			methodSim.put(methodName, methodNeighbours);
		}

		// add counts...
		for (final Entry<Integer, String> tokenPosEntry : localTokens
				.entrySet()) {
			final int tokenPos = tokenPosEntry.getKey();
			final String token = tokenPosEntry.getValue();

			// TODO (?) Split token in words?
			final long tokenId;
			if (alphabet.containsKey(token)) {
				tokenId = alphabet.get(token);
			} else {
				tokenId = nextMethodId;
				nextMethodId++;
				alphabet.put(token, tokenId);
			}

			final double distance = Math.abs(tokenPos - methodInvocationPos);
			double freqValue = (WINDOW_SIZE - distance) / (WINDOW_SIZE);

			if (methodNeighbours.containsKey(tokenId)) {
				freqValue += methodNeighbours.get(tokenId);
			}

			methodNeighbours.put(tokenId, freqValue);
		}
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		final List<Integer> methodInvocationsList = internalMethodInvocations
				.pop();
		methodStartPos.pop();

		// Get tokens, get context, save
		for (final int methodInvocation : methodInvocationsList) {
			final int startPos = methodInvocation - WINDOW_SIZE;
			final int endPos = methodInvocation + WINDOW_SIZE + 1;
			final SortedMap<Integer, String> relevantTokens = allTokens.subMap(
					startPos, endPos);

			countNeighboursOf(relevantTokens, methodInvocation);
		}

		super.endVisit(node);
	}

	/**
	 * Return the cos-similarity of two sparse vectors.
	 * 
	 * @param sparseVector1
	 * @param sparseVector2
	 * @return
	 */
	private double getCosSimilarity(final Map<Long, Double> sparseVector1,
			final Map<Long, Double> sparseVector2) {
		double similarity = 0;
		for (final Entry<Long, Double> freqEntry : sparseVector1.entrySet()) {
			final long dim = freqEntry.getKey();
			if (sparseVector2.containsKey(dim)) {
				similarity += freqEntry.getValue() * sparseVector2.get(dim);
			}
		}

		return similarity;
	}

	/**
	 * Normalize each similarity so that each element contains the frequency.
	 */
	public void normalizeSims() {
		for (Map<Long, Double> counts : methodSim.values()) {

			double sum = 0;
			for (final double fr : counts.values()) {
				sum += fr;
			}

			for (final Entry<Long, Double> objEntry : counts.entrySet()) {
				counts.put(objEntry.getKey(), objEntry.getValue() / sum);
			}
		}
	}

	public void pruneRareCounts(double threshold) {
		for (Map<Long, Double> counts : methodSim.values()) {
			ArrayList<Long> toBeRemoved = Lists.newArrayList();

			for (final Entry<Long, Double> objEntry : counts.entrySet()) {
				if (objEntry.getValue() < threshold) {
					toBeRemoved.add(objEntry.getKey());
				}
			}

			for (final long obj : toBeRemoved) {
				counts.remove(obj);
			}
		}
	}

	/**
	 * Return a table with the similarity scores of all methods above the given
	 * threshold.
	 * 
	 * @return
	 */
	public Table<String, String, Double> scoreSimilarity(double simThreshold,
			int seenThreshold) {
		final Table<String, String, Double> similarities = TreeBasedTable
				.create();

		final Set<String> toSee = Sets.newHashSet();
		toSee.addAll(methodSim.keySet());

		for (final Entry<String, Map<Long, Double>> method1Entry : methodSim
				.entrySet()) {
			final String method1 = method1Entry.getKey();
			toSee.remove(method1);
			if (methodCount.get(method1) < seenThreshold) {
				continue;
			}
			for (final String method2 : toSee) {
				if (methodCount.get(method2) < seenThreshold) {
					continue;
				}
				double sim = getCosSimilarity(method1Entry.getValue(),
						methodSim.get(method2));

				if (sim > simThreshold) {
					similarities.put(method1, method2, sim);
				}
			}
		}

		return similarities;
	}

	public void setFile(final File f) throws InvalidInputException, IOException {
		allTokens = tokenizer.tokenListWithPos(FileUtils.readFileToString(f)
				.toCharArray());
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		internalMethodInvocations.push(new ArrayList<Integer>());
		methodStartPos.push(node.getStartPosition());
		return super.visit(node);
	}

	@Override
	public boolean visit(MethodInvocation node) {
		if (internalMethodInvocations.size() > 0) {
			internalMethodInvocations.peek().add(node.getStartPosition());
		}
		return super.visit(node);
	}
}
