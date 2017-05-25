package cor.md;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cor.domain.CustomSentence;
import cor.domain.Mention;
import cor.utilities.Constant;
import cor.utilities.StanfordUtilities;
import cor.utilities.Util;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations.TreeAnnotation;
import edu.stanford.nlp.trees.tregex.TregexMatcher;
import edu.stanford.nlp.trees.tregex.TregexPattern;
import edu.stanford.nlp.util.CoreMap;

public class MentionDetection {


	
	/*
	 * Returns all possible mentions in a sentence (NPs, pronouns, proper nouns and named entity mentions)
	 * Firstly, take into account the hyperlinks. Each hyperlink is a mention.
	 * Then, Consider the plain sentence for finding other mentions.
	 * The parse tree allows to find all the noun phrases of the sentence
	 * 
	 * "There are three basic mention types: proper, nominal, and pronominal"
	 * Aria Haghighi and Dan Klein, Simple Coreference Resolution with Rich Syntactic and Semantic Features. 2009
	 * 
	 * */
	public static List<Mention> extractMentions(CustomSentence customSent, String title, int idMention, boolean isDump) {
		List<Mention> result = new ArrayList<Mention>();
		
		String sentence = customSent.getHyperlinkedText();
		List<CoreMap> sentences = StanfordUtilities.getCoreMap(customSent.getPlainText());
		customSent.setCoreMaps(sentences);
		
		
		Pattern p = Pattern.compile("\\[\\[[a-zA-Z0-9-' ]*\\]\\]");
		Pattern p2 = Pattern.compile("\\[\\[[0-9]*\\]\\]");
		Pattern p3 = Pattern.compile("\'\'\'[a-zA-Z0-9-'éèòàù ]*\'\'\'");
	
		//Util.debug(sentence);
		
		if(title != null){
			Sentence se = new Sentence(title);			
			Mention m = new Mention(idMention,  idMention, title, "O" ,se.posTag(0));
			idMention++;
			if(!isDump)
				m.setHyperlinked(true);
			result.add(m);
		}

		
		
		/*
		 * add clusters related to the hyperlinks of the Wikipedia page
		 * 
		 */
		Matcher m2 = p3.matcher(sentence);
		if(m2.find()){
			String mention = sentence.substring(m2.start()+3, m2.end()-3);
			Mention ment = new Mention(idMention, Constant.initialIdMention, mention, "O", "NNP");
			idMention++;
			
			//Util.debug("estratta menzione " + mention + " dalla frase: " + customSent.getPlainText());
			for(CoreMap sentCoreMap: sentences) {
				String coreMap = sentCoreMap.toString();
				if(coreMap.contains(mention)){
					ment.setCoreMap(sentCoreMap);
					break;
				}	
			}
			
			
			result.add(ment);
		}
		
		
		Matcher m1 = p.matcher(sentence);
		while(m1.find()){			
			if(!p2.matcher(sentence.substring(m1.start(), m1.end())).find()){
				
				String mention = sentence.substring(m1.start()+2, m1.end()-2);
				Sentence se1 = new Sentence(mention);
				Mention ment = new Mention(idMention,  idMention, mention, "O",se1.posTag(0));
				idMention++;
				ment.setHyperlinked(true);				
				for(CoreMap sentCoreMap: sentences) {
					String coreMap = sentCoreMap.toString();
					if(coreMap.contains(mention)){
						ment.setCoreMap(sentCoreMap);
						
						//Util.debug("estratta menzione " + mention + " dalla frase: " + sentCoreMap);
						break;
					}
				}
				
				result.add(ment);
			}
		}
		
		
		
		/* This list allows to avoid repetition when consider other mentions*/
		List<Mention> hyperlinks = new ArrayList<Mention>();
		for(Mention me : result){
			hyperlinks.add(me);
		}
		
		
		int index = 0, finalIndex = 0;
		CoreMap currentCoreMap = null;
		Tree tree = null;
		
		for(CoreMap sentCoreMap: sentences) {
			
			if(currentCoreMap == null || sentCoreMap != currentCoreMap){
		    	tree = sentCoreMap.get(TreeAnnotation.class);
		    	currentCoreMap = sentCoreMap;
		    	
		    	if(currentCoreMap != null)
		    		finalIndex = index++;
			}
	    	
	    	try {
	    		for (Tree subtree: tree){
	    			int indexCoreMap = ((CoreLabel)subtree.label()).get(CoreAnnotations.BeginIndexAnnotation.class);
	    			index = finalIndex + indexCoreMap;
	    			
					if(subtree.label().value().equals(Constant.noun_phrase) || subtree.label().value().equals(Constant.noun_phrase_tmp) ){
	    				String s = "";
						if(subtree.getLeaves().size() < Constant.MAX_LEAVES_OF_NP){
							
			    			//Util.debug(subtree);
							
							/* we build the value for the mention*/
							for(Tree t : subtree.getLeaves()){
								s += t + " ";
							}
							s = s.trim();
			    			//Util.debug("partial noun phrase: " + s);

			    			
							int numberOfLeaves = subtree.getLeaves().size();
							String noun = s;
							String posTag = Constant.noun_phrase;
							
							boolean phraseWithPreposition = false;
							
							for(Tree t : subtree.getChildrenAsList()){								
								Tree tr = t.getLeaves().get(0);
								String nerTag = "O";
								
								
								if(t.label().value().contains(Constant.prp)){
									String str = tr+"";
									
									Mention m = new Mention(idMention, idMention, str, nerTag, Constant.prp, sentCoreMap, index);
									m.setIndexCoreMap(indexCoreMap);
									idMention++;
									result.add(m);
									phraseWithPreposition = true;
								}
								
								if(phraseWithPreposition &&
									(t.label().value().equals(Constant.nominal) || t.label().value().equals(Constant.nominal+"S"))){
									
									if(!hyperlinks.contains(tr+"")){
																				
										Mention m = new Mention(idMention,  idMention, tr+"", nerTag, t.label().value(), sentCoreMap, index);
										m.setIndexCoreMap(indexCoreMap);

										idMention++;
										result.add(m);
										
										if(!nerTag.equals("DATE"))
											hyperlinks.add(m);
									}
								}
								
								/* We extract the noun from the tree of the NP with more than 2 leaves*/
								if(numberOfLeaves > 2 &&
										t.label().value().equals(Constant.nominal) || t.label().value().equals(Constant.nominal+"S")){
									noun = tr+"";
									posTag =  t.label().value();
								}
								
							}
							
							if(!hyperlinks.contains(s) && !phraseWithPreposition){
								
								//remove mentions with only numbers
								if(Pattern.compile("[a-zA-Z]").matcher(s).find()){
									Mention mention;
									
									Sentence sen = new Sentence(s);
									posTag = sen.posTag(0);
									if(posTag.equals("NNP")){
										mention = new Mention(idMention,  idMention, s, "O", posTag, sentCoreMap, index);
										mention.setIndexCoreMap(indexCoreMap);

									}
									else{
										mention = new Mention(idMention,  idMention, s, "O", Constant.noun_phrase, sentCoreMap, index);
										mention.setIndexCoreMap(indexCoreMap);

										sen = new Sentence(mention.getValue());
										
										if(mention.getValue().equals(title))
											posTag = Constant.noun_phrase;
										else
											posTag = sen.posTag(0);
										mention.setPosTag(posTag);
									}
									
									
									
									if(!noun.equals(s)){
										mention.setValue(noun);
										sen = new Sentence(noun);
										posTag = sen.posTag(0);
										mention.setPosTag(posTag);
									}
										
									
									if(!mention.containsAND() && mention.hasWordsInValue() < 4){
										idMention++;
										result.add(mention);
									}
										
								}
							}
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				Util.debug("error during the generation of the parse tree of the sentence");
			}
	    }
		return result;
	}
	
	

	/** Check whether pleonastic 'it'. E.g., It is possible that ... */
	private static final TregexPattern[] pleonasticPatterns = getPleonasticPatterns();
	  
	public static boolean isPleonasticIt(Mention m, Tree t){
		if ( !m.getValue().equalsIgnoreCase("it")) return false;
		for (TregexPattern p : pleonasticPatterns) {
		      if (checkPleonastic(m, t, p)) {		    	  
		    	  return true;
		      }
		}
		return false;
	}
	 
	  
	private static boolean checkPleonastic(Mention m, Tree tree, TregexPattern tgrepPattern) {
	    try {
	      TregexMatcher matcher = tgrepPattern.matcher(tree);
	      while (matcher.find()) {
	        Tree np1 = matcher.getNode("m1");
	        if (((CoreLabel)np1.label()).get(CoreAnnotations.BeginIndexAnnotation.class) == m.getIndex()) {
	        	return true;
	        }
	      }
	      
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return false;
	}
	  
	
	public static boolean inStopList(Mention m) {
		String mentionSpan = m.getValue().toLowerCase(Locale.ENGLISH);
	    if (mentionSpan.equals("u.s.") || mentionSpan.equals("u.k.")
	        || mentionSpan.equals("u.s.s.r")) return true;
	    if (mentionSpan.equals("there") || mentionSpan.startsWith("etc.")
	        || mentionSpan.equals("ltd.")) return true;
	    if (mentionSpan.startsWith("'s ")) return true;
	    if (mentionSpan.endsWith("etc.")) return true;

	    return false;
	}
		
	  
	  
	  
	  /*
	   * Now we remove all the mentions according to 
	   * Heeyoung Lee, Angel Chang, Yves Peirsman, Nathanael Chambers, Mihai Surdeanu and Dan Jurafsky. 
	   * Deterministic Coreference Resolution Based on Entity-Centric, Precision-Ranked Rules. 2013
	   * 
	   * */
	  public static void removeMentions(CustomSentence sentence){
		List<Mention> result = new ArrayList<Mention>();
		PartitiveExpression pe = new PartitiveExpression();
				
		for(Mention mention : sentence.getMentions()){
			
			if(!mention.getNerTag().equals("O")){
				if(!Constant.entityTags.contains(mention.getNerTag())){
					continue;
				}
			}
			
			if(mention.getNerTag().equals("PERCENT") || mention.getNerTag().equals("CARDINAL")){
				continue;
			}
			
			CoreMap coreMap = mention.getCoreMap();
			if(coreMap != null){
				Tree tree = coreMap.get(TreeAnnotation.class);
				if(MentionDetection.isPleonasticIt(mention, tree)){
					continue;
		    	}
				if(pe.partitiveRule(mention, tree)){
					continue;
				}					
			}
				
			if(inStopList(mention))
				continue;
			
			
			result.add(mention);
    	}
		sentence.setMentions(result);
	}

	 


	private static TregexPattern[] getPleonasticPatterns() {
		    final String[] patterns = {
		            // cdm 2013: I spent a while on these patterns. I fixed a syntax error in five patterns ($.. split with space), so it now shouldn't exception in checkPleonastic. This gave 0.02% on CoNLL11 dev
		            // I tried some more precise patterns but they didn't help. Indeed, they tended to hurt vs. the higher recall patterns.

		            //"NP < (PRP=m1) $.. (VP < ((/^V.*/ < /^(?:is|was|become|became)/) $.. (VP < (VBN $.. /S|SBAR/))))", // overmatches
		            // "@NP < (PRP=m1 < it|IT|It) $.. (@VP < (/^V.*/ < /^(?i:is|was|be|becomes|become|became)$/ $.. (@VP < (VBN < expected|hoped $.. @SBAR))))",  // this one seems more accurate, but ...
		            "@NP < (PRP=m1 < it|IT|It) $.. (@VP < (/^V.*/ < /^(?i:is|was|be|becomes|become|became)$/ $.. (@VP < (VBN $.. @S|SBAR))))",  // in practice, go with this one (best results)

		            "NP < (PRP=m1) $.. (VP < ((/^V.*/ < /^(?:is|was|become|became)/) $.. (ADJP $.. (/S|SBAR/))))",
		            "NP < (PRP=m1) $.. (VP < ((/^V.*/ < /^(?:is|was|become|became)/) $.. (ADJP < (/S|SBAR/))))",
		            // "@NP < (PRP=m1 < it|IT|It) $.. (@VP < (/^V.*/ < /^(?i:is|was|be|becomes|become|became)$/ $.. (@ADJP < (/^(?:JJ|VB)/ < /^(?i:(?:hard|tough|easi)(?:er|est)?|(?:im|un)?(?:possible|interesting|worthwhile|likely|surprising|certain)|disappointing|pointless|easy|fine|okay)$/) [ < @S|SBAR | $.. (@S|SBAR !< (IN !< for|For|FOR|that|That|THAT)) ] )))", // does worse than above 2 on CoNLL11 dev

		            "NP < (PRP=m1) $.. (VP < ((/^V.*/ < /^(?:is|was|become|became)/) $.. (NP < /S|SBAR/)))",
		            "NP < (PRP=m1) $.. (VP < ((/^V.*/ < /^(?:is|was|become|became)/) $.. (NP $.. ADVP $.. /S|SBAR/)))",
		            // "@NP < (PRP=m1 < it|IT|It) $.. (@VP < (/^V.*/ < /^(?i:is|was|be|becomes|become|became)$/ $.. (@NP $.. @ADVP $.. @SBAR)))", // cleft examples, generalized to not need ADVP; but gave worse CoNLL12 dev numbers....

		            // these next 5 had buggy space in "$ ..", which I fixed
		            "NP < (PRP=m1) $.. (VP < (MD $.. (VP < ((/^V.*/ < /^(?:be|become)/) $.. (VP < (VBN $.. /S|SBAR/))))))",

		            "NP < (PRP=m1) $.. (VP < (MD $.. (VP < ((/^V.*/ < /^(?:be|become)/) $.. (ADJP $.. (/S|SBAR/))))))", // extraposed. OK 1/2 correct; need non-adverbial case
		            "NP < (PRP=m1) $.. (VP < (MD $.. (VP < ((/^V.*/ < /^(?:be|become)/) $.. (ADJP < (/S|SBAR/))))))", // OK: 3/3 good matches on dev; but 3/4 wrong on WSJ
		            // certain can be either but relatively likely pleonastic with it ... be
		            // "@NP < (PRP=m1 < it|IT|It) $.. (@VP < (MD $.. (@VP < ((/^V.*/ < /^(?:be|become)/) $.. (@ADJP < (/^JJ/ < /^(?i:(?:hard|tough|easi)(?:er|est)?|(?:im|un)?(?:possible|interesting|worthwhile|likely|surprising|certain)|disappointing|pointless|easy|fine|okay))$/) [ < @S|SBAR | $.. (@S|SBAR !< (IN !< for|For|FOR|that|That|THAT)) ] )))))", // GOOD REPLACEMENT ; 2nd clause is for extraposed ones

		            "NP < (PRP=m1) $.. (VP < (MD $.. (VP < ((/^V.*/ < /^(?:be|become)/) $.. (NP < /S|SBAR/)))))",
		            "NP < (PRP=m1) $.. (VP < (MD $.. (VP < ((/^V.*/ < /^(?:be|become)/) $.. (NP $.. ADVP $.. /S|SBAR/)))))",

		            "NP < (PRP=m1) $.. (VP < ((/^V.*/ < /^(?:seems|appears|means|follows)/) $.. /S|SBAR/))",

		            "NP < (PRP=m1) $.. (VP < ((/^V.*/ < /^(?:turns|turned)/) $.. PRT $.. /S|SBAR/))"
		    };

		    TregexPattern[] tgrepPatterns = new TregexPattern[patterns.length];
		    for (int i = 0; i < tgrepPatterns.length; i++) {
		    	tgrepPatterns[i] = TregexPattern.compile(patterns[i]);
		    }
		    return tgrepPatterns;
	  }

	
}
