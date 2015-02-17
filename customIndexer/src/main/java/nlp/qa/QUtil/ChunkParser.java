package nlp.qa.QUtil;



import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.postag.POSTaggerME;
import opennlp.tools.util.Span;



/*
*   Overrides shallow parser, uses wordNet for probabilities used
*/

public class ChunkParser implements Parser {

  private ChunkerME chunker;
  private POSTaggerME tagger;

  public ChunkParser(ChunkerME chunker, POSTaggerME tagger) {
    this.chunker = chunker;
    this.tagger = tagger;
  }
  
  public Parse parse(Parse tokens) {

    Parse[] children = tokens.getChildren();
    String[] words = new String[children.length];
    double[] probs = new double[words.length];
    for (int i = 0, il = children.length; i < il; i++) {
      words[i] = children[i].toString();
    }

    String[] tags = tagger.tag(words);
    tagger.probs(probs);
    for (int j = 0; j < words.length; j++) {
      Parse word = children[j];
      double prob = probs[j];
      tokens.insert(new Parse(word.getText(), word.getSpan(), tags[j], prob, j));
      tokens.addProb(Math.log(prob));
    }
    
    
    String[] chunks = chunker.chunk(words, tags);
    chunker.probs(probs);
    int chunkStart = -1;
    String chunkType = null;
    double logProb=0;
    for (int ci=0,cn=chunks.length;ci<cn;ci++) {
      if (ci > 0 && !chunks[ci].startsWith("I-") && !chunks[ci-1].equals("O")) {
        Span span = new Span(children[chunkStart].getSpan().getStart(),children[ci-1].getSpan().getEnd());
        tokens.insert(new Parse(tokens.getText(), span, chunkType, logProb,children[ci-1]));
        logProb=0;
      }            
      if (chunks[ci].startsWith("B-")) {
        chunkStart = ci;
        chunkType = chunks[ci].substring(2);
      }
      logProb+=Math.log(probs[ci]);
    }
    if (!chunks[chunks.length-1].equals("O")) {
      int ci = chunks.length;
      Span span = new Span(children[chunkStart].getSpan().getStart(),children[ci-1].getSpan().getEnd());
      tokens.insert(new Parse(tokens.getText(), span, chunkType, logProb,children[ci-1]));
    }
    return tokens;
  }

  public Parse[] parse(Parse tokens, int numParses) {
    return new Parse[] {parse(tokens)};
  }
  
}
