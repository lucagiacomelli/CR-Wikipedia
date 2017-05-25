package cor.domain;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.WikiXMLParser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cor.coref.CorRes;
import cor.md.MentionDetection;
import cor.utilities.Constant;
import cor.utilities.OutputFiles;
import cor.utilities.Util;


/*
 * Exract the articles from the Dump File
 * */
public class DumpParser {


	/*
	 * Generate the CoNLL file for evaluation
	 * 
	 * */
	public static void extractArticlesFromDocuments(final File folder){	
		try {
			
		     int numberOfFiles = 0;

			for (final File fileEntry : folder.listFiles()) {
				numberOfFiles++;
				if(numberOfFiles == 31)
					break;
				
				 List<CustomSentence> customSentences = new ArrayList<CustomSentence>();
			     int idMention = Constant.initialIdMention;
			     

		        if (fileEntry.isFile()) {
		        	String title = fileEntry.getName();
		            System.out.println("\n" + title);
		            
		            BufferedReader reader = new BufferedReader(new FileReader(fileEntry));
		            
		            int numberOfLine = 0;
		            String line = reader.readLine();
		            
		            while(line != null && numberOfLine < Constant.SENTENCES_PER_PAGE){

		            	CustomSentence cs = new CustomSentence(line);
		            	cs.setPlainText(line);
		            	
			            if(idMention != Constant.initialIdMention)
			            	title = null;
			            
			            List<Mention> mentions = MentionDetection.extractMentions(cs, title, idMention, true);
						cs.setMentions(mentions);
						idMention += mentions.size();
						
						customSentences.add(cs);
		            	line = reader.readLine();
		            	
		            	numberOfLine++;
		            }
		            
		            
					CorRes.removeMentions(customSentences);

					CustomSentence totalArticle = CorRes.Init(customSentences);
		            //Util.debug(customSentences);
		            		            
		    		//CorRes.compareWikiCategories(sentence_mentions);
		    		CorRes.run(totalArticle);
		    		
		    		//Util.removeLastMentions(totalArticle);
		    		
		    		Util.debug(totalArticle);

		    		//File f = OutputFiles.generateClusterFile("ClusterFile"+fileEntry.getName()+".txt", totalArticle);
		    		File f3 = OutputFiles.generateCoNLLFile(fileEntry.getName(), "CoNLL"+fileEntry.getName()+".txt", totalArticle);		
		    		//File f2 = OutputFiles.generateCoreferenceFile(fileEntry.getName(),"HyperlinkedFile"+title+".html", totalArticle);
		    		//OutputFiles.executeOutputFile(f2);
		            
		            reader.close();
		        } 
		        
		    }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
	}
	
	
}
