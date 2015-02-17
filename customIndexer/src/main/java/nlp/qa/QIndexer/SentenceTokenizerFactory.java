package nlp.qa.QIndexer;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;

import nlp.qa.QUtil.SentenceDetectorFactory;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.util.AttributeFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
*   Encapsulating sentenceDetector object instantiation in factory class for it
*   Solr plugin requirement
*/

public class SentenceTokenizerFactory extends TokenizerFactory {
	private SentenceDetectorFactory sentenceDetectorFactory;
	private transient static Logger log = LoggerFactory
			.getLogger(SentenceTokenizerFactory.class);

	public SentenceTokenizerFactory(Map<String, String> args) {
		super(args);

		try {
			sentenceDetectorFactory = new SentenceDetectorFactory(args);
		} catch (IOException e) {
			log.error("exception :" + e.getMessage());
			throw (RuntimeException) new RuntimeException().initCause(e);
		}

	}

	@Override
	public Tokenizer create(AttributeFactory arg0, Reader input) {
		return new SentenceTokenizer(input,
				sentenceDetectorFactory.getSentenceDetector());

	}
}