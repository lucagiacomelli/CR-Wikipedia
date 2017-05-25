package cor.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.stanford.nlp.util.CoreMap;

public class CustomSentence {
	
	private String hyperlinkedText;
	private String plainText;
	private List<Mention> mentions;
	private List<CoreMap> coreMaps;
	
	public CustomSentence(){}
	
	public CustomSentence(String hText){
		this.hyperlinkedText = hText;
		plainText = "";
		mentions = new ArrayList<Mention>();
	}
	
	public CustomSentence(String hText, String plainText, List<Mention> mentions){
		this.hyperlinkedText = hText;
		this.plainText = plainText;
		this.mentions = mentions;
	}

	public String getHyperlinkedText() {
		return hyperlinkedText;
	}

	public void setHyperlinkedText(String hyperlinkedText) {
		this.hyperlinkedText = hyperlinkedText;
	}

	public String getPlainText() {
		return plainText.replace(" '", "'");
	}

	public void setPlainText(String plainText) {
		this.plainText = plainText;
	}

	public List<Mention> getMentions() {
		return mentions;
	}

	public void setMentions(List<Mention> mentions) {
		this.mentions = mentions;
	}
	
	public List<CoreMap> getCoreMaps(){
		return coreMaps;
	}
	
	public void setCoreMaps(List<CoreMap> coreMaps){
		this.coreMaps = coreMaps;
	}
	
	@Override
	public String toString(){
		return "\n\nTEXT: " + this.plainText + "\nMENTIONS: \n" +printMentions();
	}
	
	
	private String printMentions(){
		String result = "";
		for(Mention m: mentions){
			result += m + "\n";
		}
		return result;
	}
	
	
	/*
	 * Given a mention, return the position of the mention in the plain text
	 * 
	 * */
	public int mentionInText(Mention mention){
		
		List<Integer> indexes = new ArrayList<Integer>();
		String value = mention.getValue();

		Matcher matcher = Pattern.compile(value).matcher(plainText);
		while(matcher.find()){
			indexes.add(matcher.start());
		}		
		
		int mentionPosition = 0;
		for(Mention me : mentions){
			
			if(value.equals(me.getValue())){
				if(mention.getId() > me.getId()){
					mentionPosition++;
				}	
			}	
		}
		if(mentionPosition == 0)
			return 0;

		if(mentionPosition >= indexes.size())
			return indexes.get(indexes.size()-1);
		
		return indexes.get(mentionPosition);
	}
	
	
	public Mention getMentionFromId(int id){
		for(Mention m: getMentions()){
			if(m.getId() == id)
				return m;
		}
		return null;		
	}
	
}
