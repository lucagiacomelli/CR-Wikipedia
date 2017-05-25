package cor.main;

import java.io.File;
import java.util.List;

import cor.coref.CorRes;
import cor.domain.CustomSentence;
import cor.domain.DumpParser;
import cor.domain.WebPageParser;
import cor.gui.ChoiceWindow;
import cor.utilities.Constant;
import cor.utilities.CosineSimilarity;
import cor.utilities.LeskGlossOverlaps;
import cor.utilities.OutputFiles;
import cor.utilities.Util;

public class Main {

	
	/*
	 * We have two options: 
	 * 1. create a CR system starting from the dump documents
	 * 2. create a CR system dynamically based on the word searched by the user
	 * 
	 * */
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		double start = System.currentTimeMillis();

		ChoiceWindow window = new ChoiceWindow();
		
		double end = System.currentTimeMillis();
		Util.debug("time spent: " + (end-start));
	}

	
	public static void EvaluateFromDump(){
		/* For evaluation we take into account the 30 articles of WikiCoref*/
		DumpParser.extractArticlesFromDocuments(new File(Constant.pathInputFiles));
		OutputFiles.generateResponseFile(new File(Constant.pathCoNLLOutputFiles));
	}
	
	
	
	public static void CoreferenceNewWikiPage(String t){
		
		String[] pageTitles = {"Towel Day", "Martha Steart", "Roger Federer"};
		//String title = pageTitles[0];
		String title = t;
		WebPageParser webPageParser = new WebPageParser(title);
		
		
		String pageText = webPageParser.parse();
		List<CustomSentence> sent = webPageParser.extractSentences(pageText);
		
		List<CustomSentence> sentence_mentions = CorRes.extractMentionsBySentences(sent, title.replace("_", " ").replace("'", ""));
		CorRes.removeMentions(sentence_mentions);
		
		CustomSentence totalArticle = CorRes.Init(sentence_mentions);
		
		//CorRes.compareWikiCategories(sentence_mentions);
		CorRes.run(totalArticle);
		
		//Util.removeLastMentions(totalArticle);
		
		Util.debug(totalArticle);

		File f = OutputFiles.generateClusterFile("ClusterFile"+title+".txt", totalArticle);
		File f3 = OutputFiles.generateCoNLLFile(title, "CoNLL"+title+".txt", totalArticle);		
		File f2 = OutputFiles.generateCoreferenceFile(title,"HyperlinkedFile"+title+".html", totalArticle);
		//OutputFiles.executeOutputFile(f2);
	}
	
	

}
