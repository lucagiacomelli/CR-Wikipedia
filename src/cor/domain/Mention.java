package cor.domain;

import it.uniroma1.lcl.babelnet.BabelSynset;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cor.utilities.Constant;
import edu.stanford.nlp.util.CoreMap;


public class Mention {
	
	private int id;
	private int ref;
	private int index;
	private int indexCoreMap;
	private String entireValue;
	private String value;
	private String nerTag;
	private String posTag;
	private CoreMap coreMap;
	private boolean hyperlinked;
	private String gender;
	private String number;
	private String babelSense;
	private String firstParagraph;
	private String glossBabelNet;
	private List<String> wikiCategories;
	private List<BabelSynset> hypernyms;
	

	public Mention(int id){
		this.id = id;
	}
	
	public Mention(int id, int ref, String value, String nerTag, String posTag){
		this.id = id;
		this.ref = ref;
		this.entireValue = value;
		this.nerTag = nerTag;
		this.posTag = posTag;
		this.hyperlinked = false;
		this.value = extractValue(value);
		this.gender = extractGender();
		this.number = extractNumber();
		wikiCategories = new ArrayList<String>();
		hypernyms = new ArrayList<BabelSynset>();
	}
	
	
	public Mention(int id, int ref, String value, String nerTag, String posTag, CoreMap coreMap, int index){
		this.id = id;
		this.ref = ref;
		this.entireValue = value;
		this.nerTag = nerTag;
		this.posTag = posTag;
		this.coreMap = coreMap;
		this.index = index;
		this.hyperlinked = false;
		this.value = extractValue(value);
		this.gender = extractGender();
		this.number = extractNumber();
		wikiCategories = new ArrayList<String>();
		hypernyms = new ArrayList<BabelSynset>();
	}
	
	
	
	
	private String extractValue(String value) {
		String result = value;
		
		
		if(has2Words() && value.contains("'")){
			return value.split(" ")[0];
		}
		if(has3Words() && value.contains("'")){
			if(!value.split(" ")[2].contains("'"))
				return value.split(" ")[2];
			else
				return value.split(" ")[0]+ " " +value.split(" ")[1]+"'"+value.split(" ")[2].replace("'", "");
		}
		if(has2Words() && !posTag.equals("NNP") && !nerTag.equals("PERSON")){
			Matcher m = Pattern.compile("[0-9]").matcher(value.split(" ")[1]);
			if(m.find())
				return value.split(" ")[0];
			return value.split(" ")[1];
		}

		return result;
	}

	
	/*
	 * we assign gender attributes from static lexicons from Bergsma and Lin (2006), and Ji and Lin (2009).
	 * 
	 * */
	public String extractGender() {
		
		if(posTag.equals(Constant.prp)){
			if(Constant.masculinePronouns.contains(value.toLowerCase()))
				return "M";
			
			if(Constant.femininePronouns.contains(value.toLowerCase()))
				return "F";
		}
				
		return "N";
	}

	
	private String extractNumber() {
		if(posTag.equals(Constant.prp)){
			if(Constant.pluralPronouns.contains(value.toLowerCase()))
				return "P";
		}
		
		if(posTag.equals(Constant.nominal+"S"))
			return "P";
		if(!nerTag.equals("O") && !nerTag.equals("ORGANIZATION"))
			return "S";
		
		return "S";
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public int getRef() {
		return ref;
	}


	public void setRef(int ref) {
		this.ref = ref;
	}
	
	public int getIndex() {
		return index;
	}


	public void setIndex(int index) {
		this.index = ref;
	}
	
	public int getIndexCoreMap(){
		return indexCoreMap;
	}
	
	public void setIndexCoreMap(int indexCoreMap){
		this.indexCoreMap = indexCoreMap;
	}
	
	public String getEntireValue(){
		return entireValue.replace(" '", "'");
	}

	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	public String getNerTag() {
		return nerTag;
	}
	
	public String getPosTag(){
		return posTag;
	}
	
	public void setPosTag(String posTag){
		this.posTag = posTag;
		this.number = extractNumber();
	}

	public void setNerTag(String nerTag) {
		this.nerTag = nerTag;
	}
	
	public CoreMap getCoreMap() {
		return coreMap;
	}
	
	public void setCoreMap(CoreMap coreMap){
		this.coreMap = coreMap;
	}
	
	public boolean getHyperlinked(){
		return hyperlinked;
	}
	
	public void setHyperlinked(boolean hyperlinked){
		this.hyperlinked = hyperlinked;
	}
	
	public String getBabelSense(){
		return babelSense;
	}
	
	public void setBabelSense(String babelSense){
		this.babelSense = babelSense;
	}
	
	public String getGender(){
		return gender;
	}
	
	public void setGender(String gender){
		this.gender = gender;
	}
	
	public String getNumber(){
		return number;
	}
	
	public void setNumber(String number){
		this.number = number;
	}
	
	public String getFirstParagraph() {
		return firstParagraph;
	}

	public void setFirstParagraph(String firstParagraph) {
		this.firstParagraph = firstParagraph;
	}

	public String getGlossBabelNet() {
		return glossBabelNet;
	}

	public void setGlossBabelNet(String glossBabelNet) {
		this.glossBabelNet = glossBabelNet;
	}

	public List<String> getWikiCategories() {
		return wikiCategories;
	}

	public void setWikiCategories(List<String> wikiCategories) {
		this.wikiCategories = wikiCategories;
	}

	public List<BabelSynset> getHypernyms() {
		return hypernyms;
	}

	public void setHypernyms(List<BabelSynset> hypernyms) {
		this.hypernyms = hypernyms;
	}

	public void setEntireValue(String entireValue) {
		this.entireValue = entireValue;
	}
	
	
	@Override
	public String toString(){
		if(hyperlinked)
			return "------- hyperlink: " + id + " " + ref + " " + entireValue +"("+value+")" +" " + babelSense+ " " + nerTag +" "  + posTag + " " + gender +" " + number + "\thypernyms: "+ hypernyms; 
					
		return id + " " + ref + " " + entireValue +"("+value+")" +" " + babelSense+ " " + nerTag +" "  + posTag + " " + gender +" " + number + "\thypernyms: "+ hypernyms; 
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		result = prime * result + ((nerTag == null) ? 0 : nerTag.hashCode());
		result = prime * result + ref;
		result = prime * result + ((entireValue == null) ? 0 : entireValue.hashCode());
		return result;
	}

	
	/*
	 * Two mentions are equal if they are the same object, they have the same id, they refer to same mention
	 * If they are NN, then they are 
	 * 
	 * */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		
		Mention other = (Mention) obj;
		if(other.getId() == this.getId())
			return true;
		
		return false;
	}
	
	public boolean has2Words(){
		return entireValue.split(" ").length == 2;
	}
	
	public boolean has3Words(){
		return entireValue.split(" ").length == 3;
	}
	
	public int hasTotalWords(){
		return entireValue.toLowerCase().split("\\s+").length;
	}
	
	
	public int hasWordsInValue(){
		return value.toLowerCase().split("\\s+").length;
	}
	
	public boolean containsAND(){
		String [] words = value.toLowerCase().split("\\s+");
		for(String s : words){
			if(s.trim().equals("and"))
				return true;

		}

		return false;
	}
	
}
