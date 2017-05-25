package cor.md;


import java.util.HashSet;
import java.util.Set;

import cor.domain.Mention;
import edu.stanford.nlp.trees.Tree;



public class PartitiveExpression {
	

	private Set<String> partitiveConstructionHeads; //words that can be the syntactic heads of partitive constructions (e.g., ONE of the most prolific quarterbacks of all time)
		
	public PartitiveExpression(){	

		String[] tokens;		
		partitiveConstructionHeads = new HashSet<String>();

		tokens = "part|all|none|much|most|some|few|little|total|millions|hundreds|thoudands|billions|most|one|many|any|either|%|percent|portion|half|third|quarter|fraction|quarter|best|worst|member|bulk|majority|minority".split("\\|");
		for(int i=0; i<tokens.length; i++){
			partitiveConstructionHeads.add(tokens[i]);
		}
		
	}

	
	public boolean partitiveRule(Mention m, Tree t) {
		for(String heads : partitiveConstructionHeads){
			if(m.getValue().contains(heads + " of"))
				return true;
		}
		
		return false;
	  }


	
	
}