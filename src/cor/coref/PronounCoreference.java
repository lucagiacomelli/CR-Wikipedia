package cor.coref;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cor.domain.CustomSentence;
import cor.domain.Mention;
import cor.domain.WebPageParser;
import cor.utilities.Constant;
import cor.utilities.CosineSimilarity;
import cor.utilities.LeskGlossOverlaps;
import cor.utilities.StanfordUtilities;
import cor.utilities.Util;
import edu.stanford.nlp.util.CoreMap;

public class PronounCoreference {

	
	private Mention pronoun;
	private CustomSentence sentence;
	
	
	public PronounCoreference(Mention pronoun, CustomSentence sentence){
		this.pronoun = pronoun;
		this.sentence = sentence;
	}

	
	/*
	 * Return the candidates for the pronoun resolution.
	 * We consider only the mentions in the same sentence of the pronoun or in the previous sentence
	 * If a pronoun 
	 * 
	 * */
	public List<Mention> getCandidates(){
		List<Mention> result = new ArrayList<Mention>();
		
		//Util.debug("pronoun: " + pronoun + " in the sentence: " + pronoun.getCoreMap() + " with index: " + pronoun.getIndexCoreMap());
		
		String gender = pronoun.getGender();
		String number = pronoun.getNumber();
		int index = pronoun.getIndex();
		
		CoreMap coreMap = pronoun.getCoreMap();
		List<CoreMap> sentences = sentence.getCoreMaps();
		List<CoreMap> previousSentence = new ArrayList<CoreMap>();
		
		for(int i=0; i< sentences.size(); i++){
			if(sentences.get(i).equals(coreMap)){
				if(i==0)
					previousSentence.add(sentences.get(i));
				else{
					previousSentence.add(sentences.get(i-1));
					previousSentence.add(sentences.get(i));
				}
			}
		}
		
		
		
		if(pronoun.getIndexCoreMap() == 0 /*&& (
				Constant.masculinePronouns.contains(pronoun.getValue().toLowerCase()) || 
				Constant.femininePronouns.contains(pronoun.getValue().toLowerCase()))*/){
			
			//Util.debug("aggiunta anche menzione principale");
			result.add(sentence.getMentions().get(0));
		}
		
		for(Mention m: sentence.getMentions()){
			if(!m.getPosTag().equals(Constant.prp) && m.getPosTag().contains("N") && m.getId() < pronoun.getId() &&
					m.getIndex() < index && m.getGender().equals(gender) && m.getNumber().equals(number)){
				
				if(m.getCoreMap() != null && previousSentence.contains(m.getCoreMap())){
					
					boolean duplicate = false;
					for(Mention m2 : result){
						if(Util.getRecursiveReferent(sentence, m) == Util.getRecursiveReferent(sentence, m2))
							duplicate = true;
					}	
					
					if(!duplicate)
						result.add(m);
				}
			}	
		}
		

		if(result.size() == 0){
			result.add(sentence.getMentions().get(0));
		}
		return result;
	}
	

	public Mention solveReference(List<Mention> candidates){
		if(candidates.size() == 1){
			return candidates.get(0);
		}
		else{
			String sent = sentence.getPlainText();
			String sentenceOfPronoun = pronoun.getCoreMap().toString();
			int indexPronoun = pronoun.getIndex();

			double maxScore = 0.0;
			Mention result = null;
			//LeskGlossOverlaps overlap = new LeskGlossOverlaps();
			CosineSimilarity cs = new CosineSimilarity();
			
			for(Mention candidate : candidates){
				
				Pattern pattern = Pattern.compile("[ ,]");
				Matcher m = pattern.matcher(sent);
				
				String newSentence = "";
				
				int wordIndex = 0;
				while(m.find()){
					wordIndex++;
					if(wordIndex == indexPronoun){						
						String word = pronoun.getValue();
						newSentence = sentenceOfPronoun.replace(word, candidate.getValue());
					}					
				}
				
				newSentence.trim();
								
				String firstParagraph = candidate.getFirstParagraph();	
			
				if(firstParagraph != null){
					
					//double score = overlap.overlap(firstParagraph, newSentence);
					double score2 = cs.calculate(firstParagraph, newSentence);
					
					if(Util.getRecursiveReferent(sentence, candidate).getId()== Constant.initialIdMention)
						score2 += 0.25;
					
					//Util.debug("score of candidate "+ candidate.getValue()+":" + score);
					Util.debug("score2 of candidate "+ candidate.getValue()+":" + score2);

					if(score2 > maxScore){
						maxScore = score2;
						result = candidate;
					}	
				}
			}
			
			Util.debug("----------candidate chosen: " + result);
			return result;	
		}
	}
	
	
}
