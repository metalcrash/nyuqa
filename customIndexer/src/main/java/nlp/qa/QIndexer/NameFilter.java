package nlp.qa.QIndexer;

import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.tokenize.SimpleTokenizer;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.util.Span;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.util.AttributeSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/*
*   Class performs Named Entity Recognition, relies on name finder factory(util)
*/

public final class NameFilter extends TokenFilter {
  
  //this is just the prefix assigned to the tags
	public static final String NE_PREFIX = "ne";
	private transient static Logger log = LoggerFactory
			.getLogger(NameFilter.class);
	private final Tokenizer tokenizer;
	private final String[] tokenTypeNames;

	private final NameFinderME[] finders;
	private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);
	private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
	private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);

	private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
	private String text;
	private int baseOffset;

	private Span[] spans;
	private String[] tokens;
	private Span[][] foundNames;

	private boolean[][] tokenTypes;

	private int spanOffset = 0;
	private final Queue<AttributeSource.State> tokenQueue = new LinkedList<AttributeSource.State>();

  /*
  * If a sentence has a NER annotation, it creates a index for it
  */
	public NameFilter(TokenStream in, String[] modelNames,
			NameFinderME[] finders) {
		super(in);
		this.tokenizer = SimpleTokenizer.INSTANCE;
		this.finders = finders;
		this.tokenTypeNames = new String[modelNames.length];
		for (int i = 0; i < modelNames.length; i++) {
			tokenTypeNames[i] = NE_PREFIX + modelNames[i];
		}
	}

  
  /*
  *   Function checks next token availability, if available, checks if any attached NER
  */
	protected boolean fillSpans() throws IOException {
		if (!input.incrementToken())
			return false;
		text = input.getAttribute(CharTermAttribute.class).toString();
		baseOffset = input.getAttribute(OffsetAttribute.class).startOffset();
		spans = tokenizer.tokenizePos(text);
		tokens = Span.spansToStrings(spans, text);
		for (String t : tokens){
			log.debug(t);
		}
		foundNames = new Span[finders.length][];
		for (int i = 0; i < finders.length; i++) {
			foundNames[i] = finders[i].find(tokens);
		}

		tokenTypes = new boolean[tokens.length][finders.length];

		for (int i = 0; i < finders.length; i++) {
			Span[] spans = foundNames[i];
			for (int j = 0; j < spans.length; j++) {
				int start = spans[j].getStart();
				int end = spans[j].getEnd();
				for (int k = start; k < end; k++) {
					tokenTypes[k][i] = true;
				}
			}
		}

		spanOffset = 0;

		return true;
	}

  /*
  *   If there is an NER attached to a span, it saves a separate index for that span and does this iteratively 
  *   for each span, Solr requirement
  */
	public boolean incrementToken() throws IOException {
		if (tokenQueue.peek() == null) {
			if (spans == null || spanOffset >= spans.length) {
				if (!fillSpans())
					return false;
			}

			if (spanOffset >= spans.length) {
				return false;
			}

			clearAttributes();
			keywordAtt.setKeyword(false);
			posIncrAtt.setPositionIncrement(1);
			offsetAtt.setOffset(baseOffset + spans[spanOffset].getStart(),
					baseOffset + spans[spanOffset].getEnd());
			termAtt.setEmpty().append(tokens[spanOffset]);

			boolean[] types = tokenTypes[spanOffset];
			for (int i = 0; i < finders.length; i++) {
				if (types[i]) {
					keywordAtt.setKeyword(true);
					posIncrAtt.setPositionIncrement(0);
					tokenQueue.add(captureState());

					posIncrAtt.setPositionIncrement(1);
					termAtt.setEmpty().append(tokenTypeNames[i]);
				}
			}

			spanOffset++;
			return true;
		}

		State state = tokenQueue.poll();
		restoreState(state);

		return true;
	}

	@Override
	public void close() throws IOException {
		super.close();
		resetState();
	}

	@Override
	public void end() throws IOException {
		super.end();
		resetState();
	}

	private void resetState() {
		this.spanOffset = 0;
		this.spans = null;
	}

}
