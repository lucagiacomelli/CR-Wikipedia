package cor.utilities;

import info.bliki.wiki.filter.PlainTextConverter;
import info.bliki.wiki.model.WikiModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import cor.domain.CustomSentence;
import cor.domain.Mention;
import cor.domain.WebPageParser;

public class Util {

	static final boolean DEBUG = false;

	
	/*
	 * This method provides to extract sentences from the Web Pages of Wikipedia
	 * Return a List of sentences
	 * 
	 */
	 public static List<CustomSentence> extractSentences(String pageTitle, String pageText){
     	List<CustomSentence> result = new ArrayList<CustomSentence>();
     	
		Pattern p = Pattern.compile("[|!*{} ]");
		Pattern p2 = Pattern.compile("[a-zA-Z0-9']");
    	try {
			
			boolean validSentences = false;
			String [] page_splitted = pageText.split("\\n");
	    	for(int i=0; i<page_splitted.length; i++){
	       		if(!validSentences &&
	    				page_splitted[i].length() >= 2 && 
	    				(page_splitted[i].contains("'''")) && !p.matcher(page_splitted[i].substring(0,1)).find()){
	    			validSentences = true;
	    		}
	    		
	    		if(validSentences){
	    			//if the first character of the line is alphanumeric
		    		if(page_splitted[i].length() >0 && !p.matcher(page_splitted[i].substring(0,1)).find() ){
		    			if( p2.matcher(page_splitted[i].substring(0,1)).find() )
		    				result.add(new CustomSentence(page_splitted[i] + "\n"));
		    		}
	    		}
	    	}
	     	
	    	//writer.close();
	    	
		} catch (Exception e) {
			Util.debug("ERROR, Exception found: " + e.getMessage());
		}
    	
     	return result;
     }
	 
	 
	 
	 /*
	  * Return the plain sentence that will be the input to CR system
	  */
	 public static String Wiki2Plain(String wikiSentence){
		String [] letters = "a|b|c|d|e|f|g|h|f|g|h|i".split("|");
		 String s = wikiSentence;
			WikiModel wikiModel = new WikiModel("http://www.mywiki.com/wiki/${image}", "http://www.mywiki.com/wiki/${title}");
	        String plainStr;
	        String final_sentence = "";
	        
			try {
				plainStr = wikiModel.render(new PlainTextConverter(), s);
				 final_sentence = plainStr.replace("[1]", "");
			        for(int k = 2; k<Constant.MAX_REF_BTW_BRACKETS; k++){
				        final_sentence = final_sentence.replace("["+k+"]", "");
			        }
			        
			        for(int k = 1; k<Constant.MAX_REF_BTW_BRACKETS; k++){
				        final_sentence = final_sentence.replace("[note "+k+"]", "");
			        }
			        
			        for(int k = 0; k<letters.length; k++){
				        final_sentence = final_sentence.replace("["+letters[k]+"]", "");
			        }
			        
				
			} catch (IOException e) {
				e.printStackTrace();
			}
	        
			return final_sentence;
	 }


	
	public static void setRecursiveReferent(CustomSentence sentence, Mention mention, Mention referent){
		
		/* the referent is already referentiated*/
		if(referent.getId() != referent.getRef()){
			return;
		}
		
		if(mention.getId() == mention.getRef()){
			referent.setRef(mention.getId());
			
			/* We have to change also the references*/
			for(Mention m : sentence.getMentions()){
				if(m.getRef() == referent.getId()){
					m.setRef(mention.getId());
				}
			}
			
			return;
		}
		
		for(Mention me: sentence.getMentions()){
			if(me.getId() == mention.getRef()){
				setRecursiveReferent(sentence, me, referent);
				break;
			}
		}
	}
	
	
	
	
	public static Mention getRecursiveReferent(CustomSentence sentence, Mention mention){		
		if(mention.getId() == mention.getRef()){
			return mention;
		}
		
		for(Mention m: sentence.getMentions()){
			if(m.getId() == mention.getRef())
				return  getRecursiveReferent(sentence, m);
		}
		
		return null;
	}
	
	
	
	public static boolean isReportingVerb(String lemma) {
		return Constant.REPORTING_VERBS.contains(lemma);
	}


	public static boolean isCopulativeVerb(String lemma) {
		return Constant.COPULATIVE_VERBS.contains(lemma);
	}
	 
	

	public static String extractGenderForProperNouns(Mention mention){
		String gender = "N";
		
		String mentionValue = mention.getValue();
		char initial = mentionValue.toLowerCase().charAt(0);
		
		if(initial >= 'a' && initial <= 'b'){
			 gender = searchInFile("genderABJT", mentionValue);
			 if(initial == 'b' && gender.equals("N"))
				 gender = searchInFile("genderBJTFOR", mentionValue);
		}
		else if(initial >= 'c' && initial <= 'f'){
			gender = searchInFile("genderBJTFOR", mentionValue);
			 if(initial == 'f' && gender.equals("N"))
				 gender = searchInFile("genderFORLEN", mentionValue);
		}
		else if(initial >= 'g' && initial <= 'l'){
			gender = searchInFile("genderFORLEN", mentionValue);
			 if(initial == 'l' && gender.equals("N"))
				 gender = searchInFile("genderLENRC", mentionValue);
		}
		else if(initial >= 'm' && initial <= 'r'){
			gender = searchInFile("genderLENRC", mentionValue);
			 if(initial == 'r' && gender.equals("N"))
				 gender = searchInFile("genderRCWAS", mentionValue);
		}
		else if(initial >= 'x' && initial <= 'z'){
			gender = searchInFile("genderWASZUR", mentionValue);
		}
		
		
		return gender;
		
	}
	
	
	
	
	private static String searchInFile(String pathFile, String valueOfMention) {
		
		String gender = "N";
		String[] names = valueOfMention.split(" ");
		
		String name = valueOfMention.toLowerCase();
		
		//if(names.length >1)
			//name = names[0].toLowerCase();
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(pathFile));
			String line = br.readLine();
			while(line != null){
				if(line.split("\t")[0].equals(name)){
					
					br.close();
					return gender(line.split("\t")[1]);
				}
				
				line = br.readLine();
			}
			
			
			br.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return gender;
	}



	

	private static String gender(String line) {		
		String [] result = {"M", "F", "N"};
		String [] genders = line.split(" ");
		int[] counters = new int[3];
		for(int i=0; i<counters.length; i++){
			counters[i] = Integer.parseInt(genders[i]);
		}
		
		int index = 0;
		int gender = counters[index];

		for(int i=1; i<counters.length; i++){
			if(counters[i] > gender){
				index = i;
				gender = counters[i];
			}
				
		}
		return result[index];
	}




	public static void removeLastMentions(CustomSentence sentence) {
		List<Mention> result = new ArrayList<Mention>();
		for(Mention mention : sentence.getMentions()){
			if(mention.getId() == mention.getRef()){
				WebPageParser wp = new WebPageParser(mention.getValue());
				String pageText = wp.parse();
				
				if(pageText == null){
					continue;
				}
			}
			
			result.add(mention);
    	}
		sentence.setMentions(result);
	}

	
	
	
	
	public static void verificaFiles(File f1, File f2){
		BufferedReader reader;
		try {
			reader = new BufferedReader(new FileReader(f1));
			BufferedReader reader2 = new BufferedReader(new FileReader(f2));

	        String line2 = reader2.readLine();
	        String line = reader.readLine();
	        line = reader.readLine();

	        int count= 1;
	        while(line2 != null){
	        	if(count >= 53251 && count <= 61924){
	        		
	        		if(line.startsWith("NaN")){
	        			Util.debug("line1 "+ line);
	        			//Util.debug("line1 "+ line.substring(0, 8).replace(" ", "\t") +" line2: "+ line2.substring(0, 8));
	        			if(!line.substring(0, 8).replace(" ", "\t").equals(line2.substring(0, 8))){
		        			Util.debug("line "+ count +" different");	
		        		}
	        		}
	        			
	        		line = reader.readLine();
	        		
	        	}
	        	count++;
	        	line2 = reader2.readLine();
	        }
	        reader.close();
	        reader2.close();
		
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}

	public static void debug(Object s){
		if(DEBUG){
			System.out.println(s);
		}
		
	}


	 
}
