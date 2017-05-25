package cor.coref;
import it.uniroma1.lcl.babelnet.BabelSynset;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cor.domain.CustomSentence;
import cor.domain.Mention;
import cor.domain.WebPageParser;
import cor.md.MentionDetection;
import cor.utilities.BabelNetUtilities;
import cor.utilities.Constant;
import cor.utilities.LeskGlossOverlaps;
import cor.utilities.Util;
import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.util.CoreMap;


public class CorRes {

	
	public static List<CustomSentence> extractMentionsBySentences(List<CustomSentence> sentences, String title){
		
		List<CustomSentence> customSentences = new ArrayList<CustomSentence>();
		int index = 0;
        int idMention = Constant.initialIdMention;
		
		for(CustomSentence s : sentences){	
			if(index==Constant.SENTENCES_PER_PAGE) break;
			
			if(s.getHyperlinkedText().length() > Constant.LIMIT_LENGTH_SENTENCE){
				Util.debug("Sentence too long!");
			}
			else{
				
				//Util.debug("hyperlinked text: " + s.getHyperlinkedText());
				String plainStr = Util.Wiki2Plain(s.getHyperlinkedText());
				
				
				if(index == 0 && plainStr.indexOf("(") != -1){
					plainStr = plainStr.substring(0, plainStr.indexOf(" (")) + 
							 plainStr.substring(plainStr.indexOf(") ")+1, plainStr.length()-1);
				}
				//Util.debug(plainStr);
				
				s.setPlainText(plainStr);
				
				if(index >0)
					title = null;
				
				List<Mention> mentions = MentionDetection.extractMentions(s, title, idMention,false);
				s.setMentions(mentions);
				
				idMention += mentions.size();
				customSentences.add(s);
			}
			
			index++;
			
		}
		return customSentences;
	}
	
	
	public static void removeMentions(List<CustomSentence> customSentences){
		for(CustomSentence sentence : customSentences){		
			MentionDetection.removeMentions(sentence);
		}
	}
	
	
	public static void run(CustomSentence customSentence){
		
		Set<Mention> pronouns = new HashSet<Mention>();
		//speakerIdentification(customSentence);

		for(Mention mention : customSentence.getMentions()){
			for(Mention referent : customSentence.getMentions()){
				if(mention.getId() < referent.getId()){
					
					if(referent.getPosTag().equals(Constant.prp)){
						pronouns.add(referent);
					}
					
					else if(exactMatch(mention, referent)){
						Util.setRecursiveReferent(customSentence, mention, referent);
					}
				
					else if(partialMatch(mention, referent)){
						Util.setRecursiveReferent(customSentence, mention, referent);
					}
					
					else if(sameSense(mention, referent)){
						Util.setRecursiveReferent(customSentence, mention, referent);
					}
					
					//else if(glossOverlap(mention, referent)){
					//	Util.setRecursiveReferent(sentence, mention, referent);
					//}
				}
				
				
			}
		}

		
		predicateNominative(customSentence);
		
		for(Mention referent : pronouns){
			PronounCoreference pc = new PronounCoreference(referent, customSentence);
			List<Mention> candidates = pc.getCandidates();
			
			Util.debug("candidates of the pronoun " + referent + ": " + candidates);
			
			Mention mention = pc.solveReference(candidates);
			if(mention != null)
				Util.setRecursiveReferent(customSentence, mention, referent);
		}

		for(Mention referent : pronouns){
			if(referent.getId() == referent.getRef()){
				for(Mention pr : pronouns){
					if(pr.getValue().equalsIgnoreCase(referent.getValue()) && pr.getRef() != pr.getId()){
						Util.setRecursiveReferent(customSentence,  Util.getRecursiveReferent(customSentence, pr), referent);
						break;
					}
				}
			}
		}
		
		
		/*Relative pronouns TODO*/
		
	}
	
	
	


	private static boolean glossOverlap(Mention mention, Mention referent) {
		String gloss1 = mention.getFirstParagraph();
		String gloss2 = referent.getFirstParagraph();
		
		if(gloss1 != null && gloss2 != null){
			LeskGlossOverlaps lgo = new LeskGlossOverlaps();
			double scoreOverlap = lgo.overlap(gloss1, gloss2);
			
			if(scoreOverlap > Constant.THRESHOLD_OVERLAP){
				return true;
			}
		}
		
		
		return false;
	}


	/*
	 * Start to refer the mentions when we have non-conversational or conversational text
	 * - ⟨I⟩s ('I', 'my', 'mine', 'me') assigned to the same speaker are coreferent.
	 * - ⟨We⟩s ('We', 'our', 'ours', 'us') assigned to the same speaker are coreferent.
	 * - ⟨You⟩s with the same speaker are coreferent.
	 * 
	 * */
	public static void speakerIdentification(CustomSentence sentence) {
		
		String text = sentence.getPlainText();
		
		int startQuote =0;
		int endQuote = 0;
		int numberOfQuotationMarks = 0;
		
		Matcher matcher = Pattern.compile("\"").matcher(text);
		while(matcher.find()){
			if(numberOfQuotationMarks%2 == 0)
				startQuote = matcher.end();
			
			if(numberOfQuotationMarks%2 == 1)
				endQuote = matcher.start();
			
			if(startQuote != 0 && endQuote != 0){
				//Util.debug(startQuote + " "+ endQuote);
								
				Mention[] firstMentions = new Mention[3];
				for(Mention mention : sentence.getMentions()){
					
					if(sentence.mentionInText(mention) <= endQuote && sentence.mentionInText(mention) >= startQuote){
						
						if(Constant.Is.contains(mention.getValue())){
							if(firstMentions[0] == null)
								firstMentions[0] = mention;
							else
								mention.setRef(firstMentions[0].getId());
						}
						
						if(Constant.WEs.contains(mention.getValue())){
							if(firstMentions[1] == null)
								firstMentions[1] = mention;
							else
								mention.setRef(firstMentions[1].getId());
						}
						
						if(Constant.YOUs.contains(mention.getValue())){
							if(firstMentions[2] == null)
								firstMentions[2] = mention;
							else
								mention.setRef(firstMentions[2].getId());
							
						}
					}
				}
				
				startQuote = 0;
				endQuote = 0;
			}
					
			numberOfQuotationMarks++;
		}
	}
	
	
	
	private static boolean exactMatch(Mention mention, Mention referent) {
		if(mention.getEntireValue().equalsIgnoreCase(referent.getEntireValue()) && mention.getId() < referent.getId()){
			//Util.debug("exact match found: " + referent + " ---> "+ mention);
			return true;
		}
		return false;
	}



	private static boolean partialMatch(Mention mention, Mention referent) {
		if((mention.getNerTag().equals("PERSON")|| referent.getPosTag().equals("NNP")) && 
				(referent.getNerTag().equals("PERSON") || referent.getPosTag().equals("NNP"))){
			
			String[] names = mention.getEntireValue().split(" ");
			for(String name : names){
				if(name.equals(referent.getValue()))
					return true;
			}
						
		}
		return false;
	}
	
	
	/*
	 * Two mentions are coreferent if:
	 * 1. they have the same sense
	 * 2. the hypernyms of the first have the same sense of the second mention
	 * 
	 * */
	private static boolean sameSense(Mention mention, Mention referent) {

		List<BabelSynset> all_hypernyms = mention.getHypernyms();
		if(mention.getBabelSense() != null && mention.getBabelSense().equals(referent.getBabelSense())){
			
			//Util.debug("same sense found: " + referent + " ---> "+ mention + " because " +
			//referent.getBabelSense() + " = " + mention.getBabelSense());

			return true;
		}
		else{
			if(referent != mention){				
				for(BabelSynset hypernym : all_hypernyms){					
					if(hypernym.getId().getID().equals(referent.getBabelSense())){
						
						//Util.debug("same sense found: " + referent + " ---> "+ mention + " because " +
								//hypernym.getId().getID() + " = " + referent.getBabelSense());
						
						return true;
					}
				}
			}		
		}
		
		return false;
	}




	
	
	/*
	 * This method allows to:
	 * 1. search for copulae in a predicateNominative construct
	 * 2. search for reporting verbs like "say", "tell", ...
	 * 
	 * */
	public static void predicateNominative(CustomSentence sentence){
		Sentence sent = new Sentence(sentence.getPlainText());
		
		SemanticGraph graph = sent.dependencyGraph();
		//Util.debug(graph);
		
		for(IndexedWord descendant : graph.descendants(graph.getFirstRoot())){
						
			for(IndexedWord child : graph.getChildren(descendant)){
				IndexedWord subject = null;

				if(graph.reln(descendant, child).toString().contains("cop")){
					
					for(IndexedWord child2 : graph.getSiblings(child)){
						if(graph.reln(descendant, child2).toString().contains("subj")){							
							InsertReference(descendant, child2, sentence);
							subject = child2;
							//Util.debug("subject: " + subject);
						}
					}
					
					if(subject != null){
						for(IndexedWord child2 : graph.getSiblings(child)){
							if(graph.reln(descendant, child2).toString().equals("conj:and")){
								//Util.debug("child2: " + child2);
								//Util.debug("subject: " + subject);
								InsertReference(child2, subject, sentence);
							}
						}
					}
					
					break;
				}
			}
		}
	}
	
	
	
	/*
	 * We can have proper nouns, nouns or pronouns
	 * 
	 * */
	private static void InsertReference(IndexedWord complement,
			IndexedWord subj, CustomSentence sentence) {
		
		Sentence s = new Sentence(subj.originalText());
		if(s.posTag(0).equals("PRP")){
			for(Mention m : sentence.getMentions()){
				
				if(m.getValue().equals(subj.originalText())){
					
					for(Mention m2 : sentence.getMentions()){
						if(m2.getValue().contains(complement.originalText())){
							Util.setRecursiveReferent(sentence, m, m2);
						}
					}
					break;
				}
			}
		}
		
		else{
			for(Mention m : sentence.getMentions()){
				if(m.getValue().contains(subj.originalText())){
					for(Mention m2 : sentence.getMentions()){
						if(m2.getValue().contains(complement.originalText())){
							Util.setRecursiveReferent(sentence, m, m2);
						}
					}
					break;
				}
			}
		}	
	}




	
	/*
	 * Search the categories of each mention --> improve
	 * 
	 * */
	public static void compareWikiCategories(List<CustomSentence> customSentences) {
		for(CustomSentence sentence : customSentences){
			
			for(Mention mention : sentence.getMentions()){
				
				List<String> categoriesOfMention = mention.getWikiCategories();
				for(String cat : categoriesOfMention){
					for(Mention m : sentence.getMentions()){
						
						if(m != mention && 
								(!m.getNerTag().equals("DATE") || mention.getNerTag().equals("DATE")) &&
								(m.getPosTag().equals("NP") || m.getPosTag().equals("NN")) &&
								cat.toLowerCase().contains(m.getValue().toLowerCase()) ){
							
							Util.debug("category: " + cat + " contains mention " + m.getValue());
							Util.setRecursiveReferent(sentence, mention, m);
						}	
					}
				}
			}
		}	
	}
	
	
	
	/*
	 * In the initialization phase we set
	 * 1. Sense of the mention from Babelfy
	 * 2. The list of all hypernyms of BabelNet related to the mention
	 * 3. The list of categories from Wikipedia
	 * 4. The first paragraph of Wikipedia
	 * 5. The gloss of the mention from BabelNet
	 * 
	 * */
	public static CustomSentence Init(List<CustomSentence> customSentences){
		CustomSentence totalArticle = new CustomSentence();

		BabelNetUtilities bn = new BabelNetUtilities();
		
		List<String> senses = new ArrayList<String>();
		List<Mention> allMentions = new ArrayList<Mention>();
		List<CoreMap> allCoreMaps = new ArrayList<CoreMap>();
		String allSentences = "";
		
		
		for(CustomSentence sentence : customSentences){
			allSentences += sentence.getPlainText().trim() + "\n";

			for(Mention mention : sentence.getMentions()){
				allMentions.add(mention);
			}
			for(CoreMap coreMap: sentence.getCoreMaps()){
				allCoreMaps.add(coreMap);
			}
		}
		
		totalArticle.setPlainText(allSentences);
		totalArticle.setMentions(allMentions);
		totalArticle.setCoreMaps(allCoreMaps);
		
		
		
		
		int index = 0;
		for(Mention mention : allMentions){
			
			List<String> categoriesOfMention = new ArrayList<String>();
			List<BabelSynset> first_hypernyms = new ArrayList<BabelSynset>();
			String sense = null;
			
			if(mention.getHyperlinked()){
				sense = bn.getSenseBySentence(allSentences, mention.getEntireValue());
			}
			else{
				sense = bn.getSenseBySentence(allSentences, mention.getValue());
			}
			mention.setBabelSense(sense);
			
			if(!senses.contains(sense)){
				senses.add(sense);
				
				first_hypernyms = bn.getHypernyms(mention, mention.getBabelSense());
				List<BabelSynset> all_hypernyms = new ArrayList<BabelSynset>();
				all_hypernyms.addAll(first_hypernyms);
				//for the first mention we expand other hypernyms
				if(index == 0){
					for(BabelSynset hypernym : first_hypernyms){
						List<BabelSynset> hypernyms2 = bn.getHypernyms(allMentions.get(0), hypernym.getId().getID());
						for(BabelSynset hypernym2 : hypernyms2){
							all_hypernyms.add(hypernym2);
						}
					}
				}
				
				mention.setHypernyms(all_hypernyms);
				
			}
			
			
			if(mention.getGender().equals("N") && mention.getNerTag().equals("PERSON") && mention.getNumber().equals("S")){
				mention.setGender(Util.extractGenderForProperNouns(mention));
			}
			else if(mention.getGender().equals("N") && mention.getPosTag().equals("NNP") && mention.getBabelSense() != null){
				mention.setGender(Util.extractGenderForProperNouns(mention));
			}
			
			
			if(mention.getPosTag().equals("NP") || mention.getPosTag().equals("NN") || mention.getPosTag().equals("NNS") || mention.getPosTag().equals("NNP")){	
			
				WebPageParser wp = new WebPageParser(mention.getValue());
				String pageText = wp.parse();
				if(pageText != null){
					String paragraph = wp.getPlainFirstParagraph(pageText);						
					mention.setFirstParagraph(paragraph);
					categoriesOfMention = wp.categories(pageText);
					mention.setWikiCategories(categoriesOfMention);
				}
			}
			
			index++;
		}
	
	
			
		Util.debug("Initialization done...");
		return totalArticle;
		
	}
}
