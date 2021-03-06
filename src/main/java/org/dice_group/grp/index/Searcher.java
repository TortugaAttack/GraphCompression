package org.dice_group.grp.index;

import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.dice_group.grp.grammar.Grammar;
import org.rdfhdt.hdt.dictionary.DictionaryPrivate;
import org.rdfhdt.hdt.enums.TripleComponentRole;

public interface Searcher {

	public Model deindexGraph(Model graph);

	public Node getNodeFromID(int id, TripleComponentRole role);

	public Node getNodeFromID(String s, TripleComponentRole role);

	public Grammar deindexGrammar(Grammar grammar);

	public void setDict(DictionaryPrivate dict);
	
}
