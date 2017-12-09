package com.tottokug;

import java.util.ArrayList;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class Trimmer {
	static Logger logger = LoggerFactory.getLogger(Trimmer.class);

	/**
	 * 自分自身と子供達の中で一番強いノードを返す。
	 * 
	 * @param node
	 * @param myScore
	 * @return
	 */
	private static Node trim(Node node, double myScore) {
		double maxScore = myScore;
		Node strongNode = node;

		NodeList children = node.getChildNodes();
		for (int index = 0; index < children.getLength(); index++) {
			Node c = children.item(index);
			if (getTextLength(c) == 0 || c.getNodeName().equals("SCRIPT") || c.getNodeName().equals("STYLE")
					|| c.getNodeName().equals("#text") || c.getNodeName().equals("#comment")) {
				continue;
			}
			double childScore = getScore(c);
			Node strongChild = trim(c, childScore);
			double strongScore = getScore(strongChild);
			if (maxScore < strongScore) {
				maxScore = strongScore;
				strongNode = strongChild;
			}
			logger.debug("======================================================================");
			logger.debug(String.format("TAG: %8s | SCORE: %6f", c.getNodeName(), childScore));
			// logger.debug("CONTENTS=\"" + c.getTextContent().replaceAll("([\n|\\s])+",
			// "$1") + "\"");
			logger.debug("=================================================");
		}
		// logger.debug("maxScore = " + myScore);
		return strongNode;
	}

	private static Node cleaningNode(Node node) {
		NodeList nl = node.getChildNodes();
		for (int index = 0; index < nl.getLength(); index++) {
			Node child = nl.item(index);
			if (child.getNodeName().equals("SCRIPT") || child.getNodeName().equals("STYLE")
					|| child.getNodeName().equals("#comment") || child.getNodeName().equals("IFRAME")) {
				node.removeChild(child);
				index--;
			} else {
				cleaningNode(child);
			}
		}
		return node;
	}

	public static Node trim(Node node) {
		return trim(cleaningNode(node), 0);
	}

	private static long getDepth(Node node) {
		long depth = 0;
		Node p = node;
		while ((p = p.getParentNode()) != null) {
			depth++;
		}
		return depth;
	}

	private static double getScore(Node node) {
		long text = getTextLength(node);
		long child = getChildrens(node);
		long depth = getDepth(node);
		if (child == 0) {
			child = 10000;
		}
		logger.debug(String.format("TEXTLENGTH: %10d | CHILD: %6d | DEPTH: %6d", text, child, depth));
		double score = Math.round(Math.sqrt((text * Math.sqrt(text)) * depth / Math.sqrt(child * 2)));
		if (node.getNodeName().equals("SECTION")) {
			score *= 1.5;
		}
		return score;
	}

	public static long getTextLength(Node node) {
		long textLength = 0L;
		ArrayList<String> ignoreTag = new ArrayList<>();
		if (node.getNodeName().equals("SCRIPT") || node.getNodeName().equals("STYLE")
				|| node.getNodeName().equals("#comment")) {
			return 0L;
		}
		ignoreTag.addAll(Arrays.asList(new String[] { "A" }));
		if (node.getNodeName().equals("#text")) {
			textLength += node.getTextContent().replaceAll("[\\s|\\t|\\n|\\r]", "").length();
		} else {
			NodeList nl = node.getChildNodes();
			for (int index = 0; index < nl.getLength(); index++) {
				Node child = nl.item(index);
				if (!ignoreTag.contains(child.getNodeName())) {
					textLength += getTextLength(child);
				} else {
					textLength += getTextLength(child) / 3;
				}
			}
		}
		return textLength;
	}

	public static long getChildrens(Node node) {
		long count = 0;
		NodeList nl = node.getChildNodes();
		ArrayList<String> ignore = new ArrayList<String>(
				Arrays.asList(new String[] { "SCRIPT", "STYLE", "#comment", "IMG", "SPAN", }));
		for (int i = 0; i < nl.getLength(); i++) {
			if (!ignore.contains(node.getNodeName())) {
				count += getChildrens(nl.item(i));
				count++;
			}
		}
		return count;
	}
}