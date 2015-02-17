package nlp.qa.QIndexer;


import java.io.IOException;
import java.util.Map;

import nlp.qa.QUtil.NameFinderFactory;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.util.TokenFilterFactory;


/*
*   Encapsulating the Name Factory object instantiation in factory class
*/


public class NameFilterFactory extends TokenFilterFactory {
	private NameFinderFactory factory;
	  
	
	public NameFilterFactory(Map<String, String> args) {
		super(args);
		try {
		      factory = new    NameFinderFactory(args);
		    }
		    catch (IOException e) {
			      throw (RuntimeException) new RuntimeException("its bad");
		    }
		    
	}


	@Override
  public NameFilter create(TokenStream ts) {
    return new NameFilter(ts,
        factory.getModelNames(), factory.getNameFinders());
  }
  
}
