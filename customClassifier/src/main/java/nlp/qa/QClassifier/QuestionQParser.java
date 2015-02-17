package nlp.qa.QClassifier;


import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import opennlp.tools.cmdline.parser.ParserTool;
import opennlp.tools.parser.Parse;
import opennlp.tools.parser.Parser;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.spans.SpanNearQuery;
import org.apache.lucene.search.spans.SpanQuery;
import org.apache.lucene.search.spans.SpanTermQuery;
import org.apache.solr.common.SolrException;
import org.apache.solr.common.SolrException.ErrorCode;
import org.apache.solr.common.params.SolrParams;
import org.apache.solr.request.SolrQueryRequest;
import org.apache.solr.schema.SchemaField;
import org.apache.solr.search.QParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
*   It is going to detect question type (using max_ent openNLP) and create lucene query type
*/

public class QuestionQParser extends QParser implements QAParams {
	
	private transient static Logger log = LoggerFactory
			.getLogger(QuestionQParser.class);
	private Parser parser;
	private AnswerTypeClassifier atc;
	private Map<String, String> atm;

	public QuestionQParser(String qstr, SolrParams localParams,
			SolrParams params, SolrQueryRequest req, Parser parser,
			AnswerTypeClassifier atc, Map<String, String> answerTypeMap) {
		super(qstr, localParams, params, req);
		this.parser = parser;
		this.atc = atc;
		this.atm = answerTypeMap;
	}

	@Override
	public Query parse() {

		log.info("Start Parse");
		Parse parse = ParserTool.parseLine(qstr, parser, 1)[0];
		String type = atc.computeAnswerType(parse);
  
		String mt = atm.get(type);
		String field = params.get(QUERY_FIELD);
		
		log.info( "query: " + qstr + " Type: " + type + " : " + mt  + " Field: " + field);


		SchemaField sp = req.getSchema().getFieldOrNull(field);
		if (sp == null) {
			log.error("qf was null");
			throw new SolrException(ErrorCode.SERVER_ERROR, "Undefined field: "
					+ field);
		}
		
		List<SpanQuery> sql = new ArrayList<SpanQuery>();
		List<String> ne_sql = new ArrayList<String>();
		// Parses the question to check if question type is detected
		try {
			Analyzer analyzer = sp.getType().getQueryAnalyzer();
			TokenStream ts = analyzer.tokenStream(field, new StringReader(qstr));
			CharTermAttribute charTermAttribute = ts.addAttribute(CharTermAttribute.class);
			ts.reset();
			while (ts.incrementToken()) {
			    String term = charTermAttribute.toString();
				sql.add(new SpanTermQuery(new Term(field, term)));
			}
			ts.close();
		} catch (IOException e) {
			log.error("there was an exception " + e.getMessage());
			throw new SolrException(ErrorCode.SERVER_ERROR,
					"Solr QuestionQparser error ");
		}catch (Exception e) {
			log.error("there was an exception " + e.getMessage());
			throw new SolrException(ErrorCode.SERVER_ERROR,
					"Solr QuestionQparser error ");
		} 
		
    // if 2 question types detected boolean query to matcg either
		Boolean doBool = false;
		
		if (mt != null) {
			String[] parts = mt.split("\\|");

			if (parts.length == 1) {
				
				sql.add(new SpanTermQuery(new Term(field, mt.toLowerCase())));
			} else {
				doBool = true;
				for (int pi = 0; pi < parts.length; pi++) {
					ne_sql.add(parts[pi].toLowerCase());
				}
			}
		}
		if(!doBool){
			SpanNearQuery toReturn = new SpanNearQuery(sql.toArray(new SpanQuery[sql.size()]),12 , false);
			log.info(toReturn.toString());
			return toReturn;
		}else {
			BooleanQuery toReturnFinal = new BooleanQuery();

			for(String x : ne_sql){
				List<SpanQuery> temp_sql = new ArrayList<SpanQuery>();
				temp_sql.add(new SpanTermQuery(new Term(field, x)));;
				for(SpanQuery item : sql){
					temp_sql.add(item);
				}

				SpanNearQuery toReturn = new SpanNearQuery(temp_sql.toArray(new SpanQuery[temp_sql.size()]),
						params.getInt(QAParams.SLOP, 15), false);
				toReturnFinal.add(toReturn,
	                       Occur.SHOULD);
			}

			return toReturnFinal;
		}
	}

}