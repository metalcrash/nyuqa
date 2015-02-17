package nlp.qa.QClassifier;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nlp.qa.QClassifier.AnswerTypeClassifier;
import nlp.qa.QClassifier.AnswerTypeContextGenerator;
import nlp.qa.QClassifier.ChunkParser;
import nlp.qa.QClassifier.QuestionClassifier;
import opennlp.model.MaxentModel;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;


public class QuestionClassifier {

	private transient static Logger log = LoggerFactory
			.getLogger(QuestionClassifier.class);
	// this is machine specific
	public static String model_dir =  "/models/";
	public static String data_dir = System.getProperty("user.dir") + "/data/";
	public static String wordnet_dir =  "/wordnet/dict/";
	//System.out.println(this.getClass().getResource( "/models/answer.bin" ).toURI().toString());

	
	
	protected MaxentModel model;
	protected double[] probs;
	protected AnswerTypeContextGenerator atcg;
	private POSTaggerME tagger;
	private ChunkerME chunker;

	public String parse(String qstr) {
		log.info("Begin parse");
		
		File modelsDir = new File(model_dir);
		try {
			
			//create chunker
			log.info("create chunker");
			InputStream chunkerStream = new FileInputStream(new File(modelsDir,
					"en-chunker.bin"));
			ChunkerModel chunkerModel = new ChunkerModel(chunkerStream);
			chunker = new ChunkerME(chunkerModel); // <co id="qqpp.chunker"/>
			
			
			//make POS TAGER
			log.info("pos tagger");
			InputStream posStream = new FileInputStream(new File(modelsDir,
					"en-pos-maxent.bin"));
			POSModel posModel = new POSModel(posStream);
			tagger = new POSTaggerME(posModel); 
			
			//LOAD answer model
			log.info("answer model");
			model = new DoccatModel(new FileInputStream( 
					new File(model_dir, "en-answer.bin")))
					.getChunkerModel();
			
			probs = new double[model.getNumOutcomes()];
			log.info("atcg");
			atcg = new AnswerTypeContextGenerator(new File(wordnet_dir));// <co id="qqpp.context"/>
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		AnswerTypeClassifier atc = new AnswerTypeClassifier(model, probs, atcg);// <co
		Parser parser = new ChunkParser(chunker, tagger);

		Parse parse = ParserTool.parseLine(qstr, parser, 1)[0];//<co id="qqp.parseLine"/>
	    return atc.computeAnswerType(parse);

		

	}
}
