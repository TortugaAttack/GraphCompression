package org.dice_group.grp.index.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import grph.Grph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.rdf.model.Statement;
import org.dice_group.grp.grammar.Grammar;
import org.dice_group.grp.grammar.GrammarHelper;
import org.dice_group.grp.grammar.digram.Digram;
import org.dice_group.grp.grammar.digram.DigramOccurence;
import org.dice_group.grp.index.Indexer;
import org.rdfhdt.hdt.dictionary.DictionaryFactory;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.dictionary.TempDictionary;
import org.rdfhdt.hdt.dictionary.impl.PSFCTempDictionary;
import org.rdfhdt.hdt.enums.TripleComponentRole;
import org.rdfhdt.hdt.hdt.HDTVocabulary;
import org.rdfhdt.hdt.listener.ProgressListener;
import org.rdfhdt.hdt.listener.ProgressOut;
import org.rdfhdt.hdt.options.HDTSpecification;
import org.rdfhdt.hdt.rdf.parsers.JenaNodeFormatter;
import org.rdfhdt.hdtjena.NodeDictionary;

/**
 * Uses uris for indexing such that JENA can still use them
 * 
 * @author minimal
 *
 */
public class URIBasedIndexer  {
/*
	public static final String SUBJECT_PREFIX = ":s";
	// s and o have to be same
	public static final String OBJECT_PREFIX = ":s";
	public static final String PROPERTY_PREFIX = ":p";

	private TempDictionary tmpDict;
	private NodeDictionary nodeDict;
	private DictionaryPrivate dict;

	public URIBasedIndexer(TempDictionary tmpDict) {
		this.tmpDict = tmpDict;

	}

	public DictionaryPrivate getDict() {
		return dict;
	}

	/*
	 * 
	 */
/*
	@Override
	public Grph indexGraph(Grph graph) {
		Model indexedGraph = ModelFactory.createDefaultModel();
		List<Statement> stmts = graph.listStatements().toList();

		for (Statement stmt : stmts) {
			String s = SUBJECT_PREFIX
					+ dict.stringToId(JenaNodeFormatter.format(stmt.getSubject()), TripleComponentRole.SUBJECT);
			String o = OBJECT_PREFIX
					+ dict.stringToId(JenaNodeFormatter.format(stmt.getObject()), TripleComponentRole.SUBJECT);
			String p = stmt.getPredicate().toString();

			p = PROPERTY_PREFIX
					+ dict.stringToId(JenaNodeFormatter.format(stmt.getPredicate()), TripleComponentRole.PREDICATE);

			indexedGraph.add(ResourceFactory.createResource(s), ResourceFactory.createProperty(p),
					ResourceFactory.createResource(o));
			graph.remove(stmt);
		}
		this.nodeDict = new NodeDictionary(dict);
		return indexedGraph;
	}

	private void tmpIndexGraph(Grph graph) {
		List<Statement> stmts = graph.listStatements().toList();

		for (Statement stmt : stmts) {
			tmpDict.insert(JenaNodeFormatter.format(stmt.getSubject()), TripleComponentRole.SUBJECT);
			tmpDict.insert(JenaNodeFormatter.format(stmt.getObject()), TripleComponentRole.SUBJECT);
			tmpDict.insert(JenaNodeFormatter.format(stmt.getPredicate()), TripleComponentRole.PREDICATE);
			
		}
	}

	@Override
	public Node getNodeFromID(int id, TripleComponentRole role) {
		return this.nodeDict.getNode(id, role);

	}

	@Override
	public Node getNodeFromID(String s, TripleComponentRole role) {

		int hdtID = Integer.valueOf(s.substring(2));
		return getNodeFromID(hdtID, role);
	}

	@Override
	public Grammar indexGrammar(Grammar grammar) {
		// 1. tmpIndex everything
		tmpIndexGraph(grammar.getStart());
		for (Integer key : grammar.getRules().keySet()) {
			Digram d = grammar.getRules().get(key);
			tmpIndexDigrams(d, grammar.getReplaced().get(d));
		}
		// 2. reorganize
		HDTSpecification spec = new HDTSpecification();
		spec.set("dictionary.type", HDTVocabulary.DICTIONARY_TYPE_FOUR_PSFC_SECTION);
		dict = DictionaryFactory.createDictionary(spec);
		ProgressListener listener = new ProgressOut();
		tmpDict.reorganize();
		dict.load(new PSFCTempDictionary(tmpDict), listener);
		// 3. replace
		grammar.setStart(indexGraph(grammar.getStart()));
		Map<Digram, List<DigramOccurence>> realMap = new HashMap<Digram, List<DigramOccurence>>();
		for (Integer key : grammar.getRules().keySet()) {
			Digram digram = grammar.getRules().get(key);

			List<DigramOccurence> tmp = grammar.getReplaced().get(digram);
			digram = this.indexDigram(digram);
			tmp = indexDigramOcc(digram, tmp);
			realMap.put(digram, tmp);
			// TODO index occ too
			// overwrite old graph with indexed graph
			grammar.getRules().put(key, digram);
		}
		grammar.setReplaced(realMap);
		return grammar;
	}

	private List<DigramOccurence> indexDigramOcc(Digram digram, List<DigramOccurence> tmp) {
		List<DigramOccurence> ret = new ArrayList<DigramOccurence>();
		for (DigramOccurence occ : tmp) {
			List<RDFNode> ext = new ArrayList<RDFNode>();
			List<RDFNode> internals = new ArrayList<RDFNode>();
			for (RDFNode extNode : occ.getExternals()) {
				String s = SUBJECT_PREFIX
						+ dict.stringToId(JenaNodeFormatter.format(extNode), TripleComponentRole.SUBJECT);
				ext.add(ResourceFactory.createResource(s));
			}
			for (RDFNode intNode : occ.getInternals()) {
				String s = OBJECT_PREFIX
						+ dict.stringToId(JenaNodeFormatter.format(intNode), TripleComponentRole.OBJECT);
				internals.add(ResourceFactory.createResource(s));
			}
			ret.add(digram.createOccurence(ext, internals));
		}
		return ret;
	}

	private void tmpIndexDigrams(Digram digram, List<DigramOccurence> occs) {
		tmpDict.insert(JenaNodeFormatter.format(digram.getEdgeLabel1()), TripleComponentRole.PREDICATE);
		tmpDict.insert(JenaNodeFormatter.format(digram.getEdgeLabel2()), TripleComponentRole.PREDICATE);
		// internals (use OBJECT)
		for (DigramOccurence occ : occs) {
			for (RDFNode n : occ.getInternals()) {
				tmpDict.insert(JenaNodeFormatter.format(n), TripleComponentRole.OBJECT);
			}
		}
	}

	private Digram indexDigram(Digram digram) {
		String el1 = PROPERTY_PREFIX
				+ dict.stringToId(JenaNodeFormatter.format(digram.getEdgeLabel1()), TripleComponentRole.PREDICATE);
		String el2 = PROPERTY_PREFIX
				+ dict.stringToId(JenaNodeFormatter.format(digram.getEdgeLabel2()), TripleComponentRole.PREDICATE);
		digram.setEdgeLabel1(ResourceFactory.createResource(el1));
		digram.setEdgeLabel2(ResourceFactory.createResource(el2));
		return digram;
	}

	*/

}
