package cor.utilities;

import it.uniroma1.lcl.babelfy.commons.BabelfyConstraints;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.DisambiguationConstraint;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.MCS;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.ScoredCandidates;
import it.uniroma1.lcl.babelfy.commons.BabelfyParameters.SemanticAnnotationResource;
import it.uniroma1.lcl.babelfy.commons.annotation.CharOffsetFragment;
import it.uniroma1.lcl.babelfy.commons.annotation.SemanticAnnotation;
import it.uniroma1.lcl.babelfy.core.Babelfy;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.babelnet.BabelSynsetID;
import it.uniroma1.lcl.babelnet.BabelSynsetIDRelation;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.util.Language;

import java.util.ArrayList;
import java.util.List;

import cor.domain.Mention;

public class BabelNetUtilities {

	private BabelNet bn;
	private static final double THRESHOLD = 0.2;

	
	public BabelNetUtilities(){
		bn = BabelNet.getInstance();
	}
	
	
	public String getSenseBySentence(String sentence, String word){
		
		int startIndex = sentence.indexOf(word);
		int endIndex = startIndex + word.length()-1;

		if(startIndex == -1){
			startIndex = sentence.replace("'", "").indexOf(word.replace("'", ""));
			endIndex = startIndex + word.length();
		}
		
		
		BabelfyConstraints constraints = new BabelfyConstraints();
		CharOffsetFragment fragment = new CharOffsetFragment(startIndex, endIndex);		
		constraints.addFragmentToDisambiguate(fragment);

		BabelfyParameters bp = new BabelfyParameters();
		bp.setAnnotationResource(SemanticAnnotationResource.BN);
		//bp.setMCS(MCS.ON_WITH_STOPWORDS);
		
		/*
		 * MSC OFF significa che non considera, in caso di basso score di Babelfy, Most Common Sense (il più comune),
		 * e quindi può non rispondere (attempted minore di 1)
		 * MSC ON: in caso di basso score (dettato dalla costante THRESHOLD) si usa il MCS e quindi il sistema risponde
		 * sempre
		 * Se diminuiamo la threshold rispondiamo a più domande perche basta uno score più basso
		 * 
		 * */
		bp.setMCS(MCS.OFF);
		bp.setScoredCandidates(ScoredCandidates.TOP);
		bp.setDisambiguationConstraint(DisambiguationConstraint.DISAMBIGUATE_ALL_RETURN_INPUT_FRAGMENTS);
		bp.setThreshold(THRESHOLD);
		
		Babelfy bfy = new Babelfy(bp);
		List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(sentence, Language.EN, constraints);
		//List<SemanticAnnotation> bfyAnnotations = bfy.babelfy(inputText, Language.EN);

		String sense = null;
		
		//bfyAnnotations is the result of Babelfy.babelfy() call
		for (SemanticAnnotation annotation : bfyAnnotations)
		{
		    
		    /* 
		    double temp = annotation.getScore();
		    if(temp > MaxScore){
		    	MaxScore = temp;
		    	sense = annotation.getBabelSynsetID();
		    }
		    */
		    
		    if(bp.getScoredCandidates() == ScoredCandidates.TOP){
		    	sense = annotation.getBabelSynsetID();
		    }
		}
		return sense;	
	}
	
	
	
	//TODO: problema con l'hypernym "human" e "sex_or_gender" di BabelNet
	public List<BabelSynset> getHypernyms(Mention mention, String sense){
		List<BabelSynset> hypernyms = new ArrayList<BabelSynset>();
		if(sense == null)
			return hypernyms;
		
		try{
			BabelSynset synset = bn.getSynset(new BabelSynsetID(sense));
			List<BabelSynsetIDRelation> relations = synset.getEdges(BabelPointer.HYPERNYM);
			for(BabelSynsetIDRelation r : relations){
				if(r.getLanguage().equals(Language.EN)){
					BabelSynset hypernymSynset = r.getBabelSynsetIDTarget().toBabelSynset();
					hypernyms.add(hypernymSynset);
					
					//Util.debug("hypernym for the mention " + mention +": " + hypernymSynset + " with ID: " + hypernymSynset.getId().getID());
					if(hypernymSynset.getId().getID().equals(Constant.humanSense)){
						if(mention != null){
							mention.setNerTag("PERSON");
							
							/*
							List<BabelSynsetIDRelation> relations2 = synset.getEdges();
							for(BabelSynsetIDRelation r2 : relations2){
								if(r2.getBabelSynsetIDTarget().getID().equals(Constant.maleID))
									mention.setGender("M");
								else
									mention.setGender("F");
							}
							*/
						}
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
			
		return hypernyms;
	}

	
	
}
