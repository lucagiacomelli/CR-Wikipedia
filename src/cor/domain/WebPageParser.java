package cor.domain;

import info.bliki.html.HTML2WikiConverter;
import info.bliki.html.wikipedia.ToWikipedia;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cor.utilities.Constant;
import cor.utilities.Util;


public class WebPageParser {

	private String pageTitle;
	
	public WebPageParser(String title){
		pageTitle = title;
	}
	
	/*
	 * Returns a the wiki text of the wikipedia page.
	 * 
	 * */
	public String parse(){
		String result = null;
		try {
			BufferedReader in = connect(Constant.default_url + pageTitle);
		    
		    String inputLine;
		    
		    String page= "";
		    if(in != null){
		    	while ((inputLine = in.readLine()) != null){
			    	page+=inputLine+"\n";
			    }
			    
			    HTML2WikiConverter conv = new HTML2WikiConverter();
		        conv.setInputHTML(page);
		        result = conv.toWiki(new ToWikipedia(true, true, true));
		        
		    	
		        /*
		        BufferedWriter writer;
		        writer = new BufferedWriter(new FileWriter(Constant.path + pageTitle));
    			writer.write(result);
		    	writer.close();
		    	*/
			    in.close();
		    }
		    
		} 
		catch (Exception e) {   
		    //e.printStackTrace();
		}		
		return result;
	}
	
	
	public String getPlainFirstParagraph(String pageText){
		List<CustomSentence> sentences = extractSentences(pageText);
		String plainStr = null;

		if(sentences.size() > 0){
			CustomSentence s = sentences.get(0);
			if(s != null)
				plainStr  = Util.Wiki2Plain(s.getHyperlinkedText());
		}
		
		return plainStr;
	}
	
	
	

	/*
	 * Extraction of the sentences.
	 * Input: wiki text of the page
	 * Output: list of sentences
	 * 
	 * */
	public List<CustomSentence> extractSentences(String pageText){
		List<CustomSentence> sentences = new ArrayList<CustomSentence>();
		
		sentences = Util.extractSentences(pageTitle, pageText);
		return sentences;
	}
	
	
	
	/*
	 * Return all the categories related to the Wikipedia page
	 * Input: wiki text of the page
	 * Output: list of categories
	 * 
	 * Difference between dump file and wikipedia page
	 * 
	 */
	public List<String> categories(String pageText){		
		List<String> result = new ArrayList<String>();
		
		Pattern p = Pattern.compile("\\[\\[Categories\\]\\]");
		int end = pageText.indexOf("Hidden categories");
		Pattern p2 = Pattern.compile("== Navigation menu ==");
		
		Matcher m = p.matcher(pageText);
		Matcher m2 = p2.matcher(pageText);
		if(m.find() && m2.find()){
			
			try{
				if(end == -1)
					end = m2.start();
				
				String subText = pageText.substring(m.start(),end);			
				Pattern p3 = Pattern.compile("\\[\\[[a-zA-Z0-9'\\(\\)\\- ]*\\]\\]");
				Matcher m3 = p3.matcher(subText);
				
				while(m3.find()){
					String cat = subText.substring(m3.start()+2, m3.end()-2);
					if(!cat.equals("Categories"))
						result.add(cat);
				}
				
			}
			catch(Exception e){
				Util.debug("ERROR in categories of " + pageTitle);
			}
			
			
		}
		return result;
	}
	
	
	
	/*
	 * Auxiliary method for the connection
	 * 
	 * */
	private BufferedReader connect(String url){
		//Util.debug("....Connection to the url: "+ url + "...");
		URL myURL;
		URLConnection connection = null;
		BufferedReader result = null;
		
		try {
		    myURL = new URL(Constant.default_url + pageTitle);
		    connection = myURL.openConnection();
		    connection.connect();
		    
		    if(connection != null){
				result = new BufferedReader(new InputStreamReader(
		                connection.getInputStream()));	
			}
		} 
		catch (Exception e) {
			//Util.debug("ERROR in the connection: The page" + url + " may not exist!");
		}	
		return result;
	}	
}

