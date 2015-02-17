package nlp.qa.QClassifier;



import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import opennlp.model.MaxentModel;
import opennlp.tools.chunker.ChunkerME;
import opennlp.tools.chunker.ChunkerModel;
import opennlp.tools.doccat.DoccatModel;
import opennlp.tools.parser.Parser;
import opennlp.tools.postag.POSModel;
import opennlp.tools.postag.POSTaggerME;

import org.apache.solr.common.params.SolrParams;
import org.apache.solr.common.util.NamedList;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.search.QParser;
import org.apache.solr.search.QParserPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
*   Decides whether to run on Default mode or Answer type search mode, always runs on answer type mode
*/

public class QuestionQParserPlugin extends QParserPlugin {

	private transient static Logger log = LoggerFactory
			.getLogger(QuestionQParserPlugin.class);
	private Map<String, String> answerTypeMap;
	protected MaxentModel model;
	protected double[] probs;
	protected AnswerTypeContextGenerator atcg;
	private POSTaggerME tagger;
	private ChunkerME chunker;

	@Override
	public QParser createParser(String qStr, SolrParams localParams,
			SolrParams params, SolrQueryRequest req) {
		log.info("-------------------");
		log.info("BEGIN createParser");
		answerTypeMap = new HashMap<String, String>();
		
    answerTypeMap.put("L", "neloc");
		answerTypeMap.put("T", "netim|nedat");
		answerTypeMap.put("P", "neperson");
		answerTypeMap.put("O", "neorgan");
		answerTypeMap.put("M", "nemoney");
		
		QParser qParser;
		
		log.info(params.toString() + " --> SOLR PARAMS");
		log.info(params.get(QAParams.COMPONENT_NAME) + " --> SOLR PARAMS : Component name");

        Boolean reroute = false;
        //debug purpose
        if (qStr.equals("*:*")) {
        	reroute = true;
        }

        if (reroute) {
        // removes stop words as defined in solr schema
        	qParser = req.getCore().getQueryPlugin("edismax")
					.createParser(qStr, localParams, params, req);
			log.info("qParser created for edismax");
        	
        } else if (params.getBool(QAParams.COMPONENT_NAME, false) == true) {
			AnswerTypeClassifier atc = new AnswerTypeClassifier(model, probs,
					atcg);

			Parser parser = new ChunkParser(chunker, tagger);
			qParser = new QuestionQParser(qStr, localParams, params, req, parser, atc, answerTypeMap);
			log.info("qParser created for qa type");

		} else {

			qParser = req.getCore().getQueryPlugin("edismax")
					.createParser(qStr, localParams, params, req);
			log.info("qParser created for edismax");
		}
		
		return qParser;
		
	}

/*
*   Loads models to memory soon as solr is started
*/

	public void init(@SuppressWarnings("rawtypes") NamedList initArgs) {
		
		log.info("************************");
		String modelDirectory = "/models/";
		String wordnetDirectory = "/wordnet";

		File modelsDir = new File(modelDirectory);
		try {
			log.info("LOADING chunkerStream and chunkermodel");
			InputStream chunkerStream = new FileInputStream(new File(modelsDir,
					"en-chunker.bin"));
			ChunkerModel chunkerModel = new ChunkerModel(chunkerStream);
			chunker = new ChunkerME(chunkerModel);

			log.info("LOADING posStream and posModel");
			InputStream posStream = new FileInputStream(new File(modelsDir,
					"en-pos-maxent.bin"));
			POSModel posModel = new POSModel(posStream);
			tagger = new POSTaggerME(posModel);

			log.info("LOADING answer model");
			model = new DoccatModel(new FileInputStream(
					new File(modelDirectory, "en-answer.bin")))
					.getChunkerModel();

			probs = new double[model.getNumOutcomes()];

			log.info("LOADING AnswerTypeContextGenerator with wordnet");
			File wordnet_file = new File(wordnetDirectory, "dict");
			atcg = new AnswerTypeContextGenerator(wordnet_file);


		} catch (IOException e) {
			log.error("there was an exception !!!");
			throw new RuntimeException(e);
		}
		log.info("************************");
	}

}
