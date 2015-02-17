package nlp.qa.QIndexer;

import java.io.IOException;
import java.io.Reader;

import opennlp.tools.sentdetect.SentenceDetector;
import opennlp.tools.util.Span;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;


/*
*   Extension to tokenizer (standard is whiteSpace) but this ensures output is openNLP compatible
*   Takes in a paragraph, detects a sentence and then tokenizes it. Uses sentTokenFactory QUtil
*/

public final class SentenceTokenizer extends Tokenizer {
  
  private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

  private SentenceDetector detector;
  private Span[] sentences = null;
  private char[] inputSentence;
  private int tokenOffset = 0;
  
  public SentenceTokenizer(Reader in, SentenceDetector detector) {
    super(in);
    this.detector = detector;
  }
  
  public void reset(Reader in) throws IOException {
    super.reset();
    sentences = null;
  }
  
  public void fillSentences() throws IOException {
    char[] c = new char[256];
    int sz = 0;
    StringBuilder b = new StringBuilder();
    
    while ((sz = input.read(c)) >= 0) {
      b.append(c, 0, sz);
    }
    
    String tmp = b.toString();
    inputSentence = tmp.toCharArray();
    sentences = detector.sentPosDetect(tmp);
    tokenOffset = 0;
  }
  
  @Override
  public boolean incrementToken() throws IOException {
    if (sentences == null) {
      fillSentences();
    }
    
    if (tokenOffset >= sentences.length) {
      return false;
    }
    
    Span sentenceSpan = sentences[tokenOffset];
    clearAttributes();
    int start = sentenceSpan.getStart();
    int end   = sentenceSpan.getEnd();
    termAtt.copyBuffer(inputSentence, start, end-start);
    posIncrAtt.setPositionIncrement(1);
    offsetAtt.setOffset(start, end);
    tokenOffset++;
    
    return true;
  }
}
