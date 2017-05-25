package cor.utilities;

import java.util.ArrayList;

import info.debatty.java.stringsimilarity.Cosine;

public class CosineSimilarity {

	private Cosine cs;
	private String	list = "a aboard about above across after against all along alongside although amid amidst among amongst an and another anti any anybody anyone anything around as astride at aught bar barring because before behind below beneath beside besides between beyond both but by circa concerning considering despite down during each either enough everybody everyone except excepting excluding few fewer following for from he her hers herself him himself his hisself i idem if ilk in including inside into it its itself like many me mine minus more most myself naught near neither nobody none nor nothing notwithstanding of off on oneself onto opposite or other otherwise our ourself ourselves outside over own past pending per plus regarding round save self several she since so some somebody someone something somewhat such suchlike sundry than that the thee theirs them themselves there they thine this thou though through throughout thyself till to tother toward towards twain under underneath unless unlike until up upon us various versus via vis-a-vis we what whatall whatever whatsoever when whereas wherewith wherewithal which whichever whichsoever while who whoever whom whomever whomso whomsoever whose whosoever with within without worth ye yet yon yonder you you-all yours yourself";
	private ArrayList<String> stoplist	=	null;

	
	public CosineSimilarity(){
		cs = new Cosine();
		stoplist = new ArrayList<String>();
		getStopWords();

	}
	
	// get stop words (Ted Pedersens's list)
	private void getStopWords(){
		String[] editor = list.split("\\s");
		for(int i = 0; i < editor.length; i++)
			stoplist.add(editor[i]);
 	}
	
	public double calculate(String sentence1, String sentence2){
		for(String stopWord : stoplist){
			sentence1 = sentence1.replace(stopWord, "");
			sentence2 = sentence2.replace(stopWord, "");
		}
		
		double result = cs.similarity(sentence1, sentence2);
		return result;
	}
}
