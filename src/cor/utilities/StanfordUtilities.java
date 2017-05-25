package cor.utilities;

import java.util.List;
import java.util.Properties;

import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class StanfordUtilities {

	
	public static List<CoreMap> getCoreMap(String t){
		 // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
	    Properties props = new Properties();
	    
	    //props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
	    props.put("annotators", "tokenize, ssplit, parse");
	    StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

	    // read some text in the text variable
	    String text = t;

	    // create an empty Annotation just with the given text
	    Annotation document = new Annotation(text);

	    // run all Annotators on this text
	    pipeline.annotate(document);

	    // these are all the sentences in this document
	    // a CoreMap is essentially a Map that uses class objects as keys and has values with custom types
	    List<CoreMap> sentences = document.get(SentencesAnnotation.class);

	    /*
	    for(CoreMap sentence: sentences) {
	    
	    	for (CoreLabel token: sentCoreMap.get(TokensAnnotation.class)) {
	    		// this is the text of the token
	    		String word = token.get(TextAnnotation.class);
	    		// this is the POS tag of the token
	    		String pos = token.get(PartOfSpeechAnnotation.class);
	    		// this is the NER label of the token
	    		String ne = token.get(NamedEntityTagAnnotation.class);       
	    	}
	    	
		    this is the Stanford dependency graph of the current sentence
		    SemanticGraph dependencies = sentence.get(CollapsedCCProcessedDependenciesAnnotation.class);
	    }

    	*/
	    
	    
	    return sentences;
		
	}
	
	
}
