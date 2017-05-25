package cor.domain;

import info.bliki.wiki.dump.IArticleFilter;
import info.bliki.wiki.dump.Siteinfo;
import info.bliki.wiki.dump.WikiArticle;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cor.coref.CorRes;
import cor.utilities.OutputFiles;
import cor.utilities.Util;


/*
 * From a dump File we can extract all the articles.
 * We could also match a title of a particular page in input and extract that particular page
 * 
 * */
public class ArticleFilter  implements IArticleFilter{

	static int index = 0;
	
	
	@SuppressWarnings("unused")
	public void process(WikiArticle page, Siteinfo siteinfo) throws IOException {
		//Util.debug(page.getId());
		
		if(page.isMain() && page.getTitle().equals("Anatole France")){
			
			//Util.debug("page text: " + page.getText());
			
			
			
			List<CustomSentence> sent = Util.extractSentences(page.getTitle(),page.getText());
			List<CustomSentence> sentence_mentions = CorRes.extractMentionsBySentences(sent, page.getTitle());
			CorRes.removeMentions(sentence_mentions);

			
			/*
			CustomSentence totalArticle = CorRes.Init(sentence_mentions);
			CorRes.run(totalArticle);
						
			Util.debug(totalArticle);

			File f = OutputFiles.generateClusterFile("ClusterFile"+page.getTitle()+".txt", totalArticle);
			File f3 = OutputFiles.generateCoNLLFile(page.getTitle(), "CoNLL"+page.getTitle()+".txt", totalArticle);		
			//File f2 = OutputFiles.generateCoreferenceFile("HyperlinkedFile"+title+".html", sentence_mentions);
			//OutputFiles.executeOutputFile(f2);
			 * 
			 */

        }
        
		index++;
	}
	
	
	public List<String> categoriesFromDump(String pageText){		
		List<String> result = new ArrayList<String>();
		
		Pattern p = Pattern.compile("\\[\\[Category:[a-zA-Z0-9'\\(\\)\\- ]*\\]\\]");
		
		Matcher m = p.matcher(pageText);
		while(m.find()){
			String cat = pageText.substring(m.start()+11, m.end()-2);	
			result.add(cat);
		}
		return result;
	}
	
	

}
