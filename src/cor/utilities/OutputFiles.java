package cor.utilities;

import java.awt.Desktop;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cor.domain.CustomSentence;
import cor.domain.Mention;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.simple.Sentence;

public class OutputFiles {

	

	public static void executeOutputFile(File f){
		try {
			Desktop.getDesktop().open(f);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static File generateClusterFile(String nameFile, CustomSentence article){
		Util.debug("generating cluster file...");
		File f = new File(Constant.pathClusterOutputFiles + nameFile);
	
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			writer.write(article.getPlainText()+"\n");
			
			for(Mention mention : article.getMentions()){
				if(mention.getId() == mention.getRef()){
					writer.write(mention.getEntireValue()+ " " + mention.getIndex() + "\t\t");
					
					for(Mention referent : article.getMentions()){
						if(referent != mention && Util.getRecursiveReferent(article, referent) == mention){
							writer.write(referent.getEntireValue() + " " + referent.getIndex() + "\t");
						}
					}
					writer.write("\n");
				}
			}
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return f;
	}

	
	public static File generateCoreferenceFile(String title, String nameFile, CustomSentence article){
		Util.debug("generating hyperlinked file...");
		
		File f = new File(Constant.pathLinkedOutputFiles + nameFile);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			
			String textFile = "";
			
			String sentence = "";
			List<String> sentences = new ArrayList<String>();
			String [] words = null;
			int count = 0;
			
			for(Mention mention : article.getMentions()){
				
				if(mention.getCoreMap() != null && !mention.getCoreMap().toString().equals(sentence)
						&& !sentences.contains(mention.getCoreMap().toString())){					
					sentence = mention.getCoreMap().toString();
					Sentence sen = new Sentence(sentence);
					List<String> wordsOfSentence = sen.words();
					List<String> wordsSentence = new ArrayList<String>();
					for(int i=0; i<wordsOfSentence.size(); i++){
						wordsSentence.add(wordsOfSentence.get(i));
					}
					
					//Util.debug("words of the sentence: " + wordsSentence);
				      
					String sentence1 = sentence;
					sentences.add(sentence);
					
					words = new String[wordsSentence.size()];
					for(int i=0; i<wordsSentence.size(); i++){
						words[i] = wordsSentence.get(i);
					}
					
					for(int i=0; i<words.length; i++){
						//String line =  "NaN\t0\t" + (i+1) + "\t"+ words[i] +"@";
						String line =  words[i]+"@";

						//Util.debug("words["+i+"]: " + words[i]);
						for(Mention m : article.getMentions()){
							if(m.getCoreMap() != null && m.getCoreMap().toString().equals(sentence1)){
								if(i == m.getIndexCoreMap() && !m.getHyperlinked()){									
									if(isReferencedMention(m, article)){
										line = line.replace(words[i], "<a href=\""+Constant.default_url+ m.getValue()+"\">" + m.getValue()+"</a>");
										i = i+m.hasTotalWords()-1;
									}
									else if (m.getId() != m.getRef()){											
										line = line.replace(words[i], "<a href=\""+Constant.default_url+ Util.getRecursiveReferent(article, m).getValue()+"\">" +  m.getValue()+"</a>");
										i = i+m.hasTotalWords()-1;
									}
									
									break;
								}
								
								else if(m.getHyperlinked() && words[i].equals(m.getValue())){
									if(isReferencedMention(m, article))
										line = line.replace(words[i], "<a href=\""+Constant.default_url+ m.getValue()+"\">" +  m.getValue()+"</a>");
									else if (m.getId() != m.getRef())
										line = line.replace(words[i], "<a href=\""+Constant.default_url+ Util.getRecursiveReferent(article, m).getValue()+"\">" +  m.getValue()+"</a>");
								}
							}
						}
						textFile += line.replace("@"," ");
					}
					textFile += "\n";
				}
			}
			writer.write(textFile);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();	
		}
		
		return f;
		
	}
	
	
	
	public static File generateCoNLLFile(String title, String nameFile, CustomSentence article){
		Util.debug("generating CoNLL file...");
		
		File f = new File(Constant.pathCoNLLOutputFiles + nameFile);
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
			
			String textFile = "";
			textFile += "#begin document " + title +"\n";
			
			String sentence = "";
			List<String> sentences = new ArrayList<String>();
			String [] words = null;
			int count = 0;
			
			for(Mention mention : article.getMentions()){
				
				if(mention.getCoreMap() != null && !mention.getCoreMap().toString().equals(sentence)
						&& !sentences.contains(mention.getCoreMap().toString())){					
					
					sentence = mention.getCoreMap().toString();

					/* exceptions */
					String phrase = "and the invading army of the Ottoman Empire under the leadership of Sultan Murad I.";
					Sentence sen = new Sentence(sentence);
					
					List<String> wordsOfSentence = sen.words();
					List<String> wordsSentence = new ArrayList<String>();

					for(int i=0; i<wordsOfSentence.size(); i++){
						wordsSentence.add(wordsOfSentence.get(i));
					}
					
					if(sentence.contains(phrase))
						wordsSentence.add(".");
					
					//Util.debug("words of the sentence: " + wordsSentence);
				      
					String sentence1 = sentence;
					sentences.add(sentence);
					
					words = new String[wordsSentence.size()];
					for(int i=0; i<wordsSentence.size(); i++){
						words[i] = wordsSentence.get(i);
					}
					
					
					for(int i=0; i<words.length; i++){
						String line =  "NaN\t0\t" + (i+1) + "\t"+ words[i] +"@";
						//Util.debug("words["+i+"]: " + words[i]);
						
						if(count == 0){
							count++;
							line = line.replace("@", "\t("+ article.getMentions().get(0).getId() +")\n");
						}
						else{
							for(Mention m : article.getMentions()){
								
								if(m.getCoreMap() != null && m.getCoreMap().toString().equals(sentence1)){
									if(i == m.getIndexCoreMap() && !m.getHyperlinked()){
										//Util.debug("trovata mention " + m.getEntireValue() +" con indice " + m.getIndexCoreMap() + " con numero di parole: " + m.hasTotalWords());
										
										if(isReferencedMention(m, article)){
											
											if(m.hasTotalWords() == 1){
												line = line.replace("@", "\t("+ m.getId() +")\n");
											}
											else{
												line = line.replace("@", "\t("+ m.getId() +"\n");
												textFile += line;
												
												for(int j=i+1; j<i+m.hasTotalWords() && j<words.length; j++){
													if(j == i+m.hasTotalWords()-1){
														line =  "NaN\t0\t" + (j+1) + "\t"+ words[j] +"\t"+ m.getId() +")\n";
													}
													else{
														line =  "NaN\t0\t" + (j+1) + "\t"+ words[j] +"\t-\n";
														textFile += line;
													}
												}
												i = i+m.hasTotalWords()-1;
											}
										}
										
										else if (m.getId() != m.getRef()){											
											if(m.hasTotalWords() == 1){
												line = line.replace("@", "\t("+Util.getRecursiveReferent(article, m).getId() +")\n");
											}
											else{
												line = line.replace("@", "\t("+ Util.getRecursiveReferent(article, m).getId() +"\n");
												textFile += line;
												
												for(int j=i+1; j<i+m.hasTotalWords() && j<words.length ; j++){
													
													if(j == i+m.hasTotalWords()-1){
														line =  "NaN\t0\t" + (j+1) + "\t"+ words[j] +"\t"+ Util.getRecursiveReferent(article, m).getId() +")\n";
													}
													else{
														line =  "NaN\t0\t" + (j+1) + "\t"+ words[j] +"\t-\n";
														textFile += line;
													}
												}
												i = i+m.hasTotalWords()-1;
											}
										}
										
										break;
									}
									
									else if(m.getHyperlinked() && words[i].equals(m.getValue())){
										if(isReferencedMention(m, article)){
											line = line.replace("@", "\t("+ m.getId() +")\n");
										}
										else if (m.getId() != m.getRef()){
											line = line.replace("@", "\t("+ Util.getRecursiveReferent(article, m).getId() +")\n");
										}
									}
								}
							}	
						}
						
						textFile += line.replace("@", "\t-\n");
					}
					
					
					textFile += "\n";
				}
			}

			textFile += "#end document\n";
			writer.write(textFile);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();	
		}
		
		return f;
	}
	
	
	
	public static File generateResponseFile(final File folder){
		File result = new File(Constant.pathCoNLLOutputFiles + "response_file");
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(result));
			
			for (final File f : folder.listFiles()) {
				Util.debug(f.getName());
				
				if(f.getName().startsWith("CoNLL")){
					BufferedReader reader = new BufferedReader(new FileReader(f));
		            String line = reader.readLine();
		            
		            while(line != null){
		            	writer.write(line+"\n");
		            	
		            	line = reader.readLine();
		            }
		            reader.close();
				}
			}
			
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		} 

		return result;
	}
	
	
	
	private static boolean isReferencedMention(Mention mention, CustomSentence article){
		for(Mention referent : article.getMentions()){
			if(referent != mention && Util.getRecursiveReferent(article, referent) == mention){
				return true;
			}
		}
		
		return false;
		
	}
	
}
