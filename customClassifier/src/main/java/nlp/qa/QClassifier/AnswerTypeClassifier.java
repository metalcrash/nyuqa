package nlp.qa.QClassifier;

/* Trains the document to output answer type, invokes Answer Type context generator
 * All format requirements of openNLP stream type are met here
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import opennlp.maxent.GIS;
import opennlp.maxent.GISModel;
import opennlp.model.MaxentModel;
import opennlp.model.TwoPassDataIndexer;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;


public class AnswerTypeClassifier {

  
  private MaxentModel model;
  private double[] probs;
  private AnswerTypeContextGenerator atcg;

  public AnswerTypeClassifier(MaxentModel model, double[] probs, AnswerTypeContextGenerator atcg) {
    this.model = model;
    this.probs = probs;
    this.atcg = atcg;
  }

  public String computeAnswerType(Parse question) {
    double[] probs = computeAnswerTypeProbs(question);
    return model.getBestOutcome(probs);
  }

  public double[] computeAnswerTypeProbs(Parse question) {
    String[] context = atcg.getContext(question);
    return model.eval(context, probs);
  }
  
  public static void main(String[] args) throws IOException {
   
	String trainFile = "qtrain.dat";
	String ofile = "en-answer.bin";
    File outFile = new File(ofile);
    File modelsDir = new File("/models/");
    String wordnetDir = "/wordnet/";
    
    InputStream chunkerStream = new FileInputStream(
        new File(modelsDir,"en-chunker.bin"));
    ChunkerModel chunkerModel = new ChunkerModel(chunkerStream);
    ChunkerME chunker = new ChunkerME(chunkerModel);
    
    
    InputStream posStream = new FileInputStream(
        new File(modelsDir,"en-pos-maxent.bin"));
    POSModel posModel = new POSModel(posStream);
    POSTaggerME tagger =  new POSTaggerME(posModel);
    Parser parser = new ChunkParser(chunker, tagger);
    AnswerTypeContextGenerator actg = new AnswerTypeContextGenerator(new File(wordnetDir));

    AnswerTypeEventStream es = new AnswerTypeEventStream(trainFile,
            actg, parser);
    GISModel model = GIS.trainModel(100, new TwoPassDataIndexer(es, 3));
    new DoccatModel("en", model).serialize(new FileOutputStream(outFile));
    System.out.println("Done");

  }
}
