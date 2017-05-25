package cor.utilities;

import java.util.HashSet;
import java.util.Set;

public class Constant {

	public static final String pathInputFiles = "input/";
	public static final String pathClusterOutputFiles = "ClusterOutput/";
	public static final String pathCoNLLOutputFiles = "CoNLLOutput/";
	public static final String pathLinkedOutputFiles = "LinkedOutput/";

	public static final String default_url = "https://en.wikipedia.org/wiki/";	
	
	public static final String noun_phrase = "NP";
	public static final String noun_phrase_loc = "";
	public static final String noun_phrase_tmp = "NP-TMP";
	
	
	public static final String proper_noun = "NNP";
	public static final String nominal = "NN";
	public static final String prp = "PRP";
	public static final String pos = "POS";
	
	public final static int SENTENCES_PER_PAGE = 1;
	
	public static final double THRESHOLD_OVERLAP = 5.0;

	public static final int MAX_LEAVES_OF_NP = 5;
	public final static int MAX_REF_BTW_BRACKETS = 40;
	public final static int LIMIT_LENGTH_SENTENCE = Integer.MAX_VALUE;
	public final static int initialIdMention = 100;
	
	public static final String humanSense = "bn:00044576n";
	public static final String maleID = "bn:14384574n";

	
	
	@SuppressWarnings("serial")
	public static final Set<String> entityTags = new HashSet<String>(){
		{
			add("PERSON");  
			add("NORP");
			add("FACILITY");
			add("ORGANIZATION");
			add("GPE");
			add("LOCATION");
			add("PRODUCT");
			add("EVENT");
			add("WORK OF ART");  
			add("LAW");  
			add("LANGUAGE");  
			add("DATE");  
			add("TIME");  
			add("PERCENT");  
			add("MONEY");  
			add("QUANTITY"); 
			add("ORDINAL");  
			add("CARDINAL");
			add("MISC");
	
		}
	};
	
	@SuppressWarnings("serial")
	public static final Set<String> masculinePronouns = new HashSet<String>(){
		{
			add("he");  
			add("his");
			add("him");
		}
	};
	
	
	@SuppressWarnings("serial")
	public static final Set<String> femininePronouns = new HashSet<String>(){
		{
			add("she");  
			add("her");
			add("hers");
		}
	};
	
	@SuppressWarnings("serial")
	public static final Set<String> pluralPronouns = new HashSet<String>(){
		{
			add("we");
			add("our");
			add("us");
			add("they");
			add("their");  
			add("them");

		}
	};
	
	
	@SuppressWarnings("serial")
	public static final Set<String> Is = new HashSet<String>(){
		{
			add("I");
			add("my");
			add("mine");
			add("me");
		}
	};
	
	@SuppressWarnings("serial")
	public static final Set<String> WEs = new HashSet<String>(){
		{
			add("We");
			add("our");
			add("ours");
			add("us");
		}
	};

	
	@SuppressWarnings("serial")
	public static final Set<String> YOUs = new HashSet<String>(){
		{
			add("You");
			add("your");
			add("yours");
		}
	};
	
	
	
	/*
	 * https://en.wiktionary.org/wiki/Category:English_reporting_verbs
	 * 
	 * */
	@SuppressWarnings("serial")
	public static final Set<String> REPORTING_VERBS = new HashSet<String>(){
		{
			add("accuse");
			add("add");
			add("admit");
			add("advise");
			add("agree");
			add("announce");
			add("answer");
			
			add("answer back");
			add("apologize");
			add("argue");
			add("ask");
			add("ask after");
			add("ask in");
			add("ask round");
			
			add("beg");
			add("blame");
			add("blather");
			add("blurt");
			add("blurt out");
			add("boast");
			add("ask after");
			add("ask in");
			add("ask round");
			
			add("claim");
			add("comment");
			add("complain");
			add("confirm");
			add("congratulate");
			add("consider");
			add("cry");
			
			add("decide");
			add("demand");
			add("deny");
			add("doubt");
			
			add("encourage");
			add("enquire");
			add("estimate");
			add("explain");
			
			add("forbid");
			
			add("gabble");
			add("growl");
			add("grumble");
			add("gush");
			
			add("inquire");
			add("insist");
			add("interrupt");
			add("invite");
			
			add("jabber");
			
			add("laugh");
			
			add("mention");
			
			add("observe");
			add("offer");
			add("order");
			
			add("persuade");
			add("plead");
			add("promise");
			add("propose");
			
			add("realize");
			add("recommend");
			add("refuse");
			add("remark");
			add("remember");
			add("remind");
			add("repeat");
			add("reply");
			add("report");
			add("respond");
			add("retort");
			add("return");
			add("reveal");
			
			add("say");
			add("shout");
			add("splutter");
			add("stammer");
			add("state");
			add("stutter");
			add("suggest");
			add("suppose");
			add("swear");
			
			add("tell");
			add("threaten");
			add("thunder");
			add("titter");
			
			add("warm");
			add("whimper");
			add("whisper");
			
			add("yell");			
		}
	};
	
	
	/*
	 * https://en.wiktionary.org/wiki/Category:English_copulative_verbs
	 * 
	 * */
	@SuppressWarnings("serial")
	public static final Set<String> COPULATIVE_VERBS = new HashSet<String>(){
		{
			add("act");
			add("appear");
			add("arrive");
			
			add("be");
			add("become");
			add("bleed");
			add("break");

			add("come");
			
			add("emerge");
			
			add("fall");
			add("feel");
			
			add("get");
			add("grow");
			
			add("keep");
			
			add("look");
			
			add("play");
			add("prove");
			
			add("remain");
			add("run");
			
			add("seem");
			add("sound");
			
			add("test");			
		}
	};


}
