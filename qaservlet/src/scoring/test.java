package scoring;

import nyuqa.Entry;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

public class test {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		HttpSolrServer server = null;
        try
        {
            server = new HttpSolrServer("http://localhost:8983/solr/collection1/");
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        SolrQuery query = new SolrQuery();
        query.setRequestHandler("/answer");
        query.setQuery("who is the president of nyu");
        query.setParam("wt", "json");
        query.setParam("debugQuery", "true");
        String querystr="nothing...";
        String[] top3=null;
        float[] scores=null;
        try
        {
            QueryResponse qr = server.query(query);
            try{
            	querystr=(String)qr.getDebugMap().get("querystring");
            }catch(NullPointerException e){
            	querystr="call me null";
            }
            SolrDocumentList doclist = qr.getResults();
			//String[] keywords={"adimission","phd"};
			Entry entry=new Entry(doclist,querystr);
			entry.score();
			top3=entry.getTop3();
			scores=entry.getScores();
			/*for(int i=0;i<doclist.size();i++){
				String[] tokens={"nothing!"};
				try{
					DocProcessor dp=new DocProcessor(doclist.get(i));
					dp.tokenizing();
					dp.stem();
					tokens=dp.getTokens();
					RealCounter rc=new RealCounter(tokens,keywords);
					rc.countEach();
					int kwscore=rc.getAccum();
					int[] eachCount=rc.getEachCount();
				}catch(NullPointerException f){
					continue;
				}
			}*/
        }
        catch (SolrServerException e)
        {
            e.printStackTrace();
        }
        /*for(int i=2;i>=0;i--){
	        System.out.println("top "+(3-i)+": "+scores[i]+" :");
	        System.out.println(top3[i]);
        }*/
	}

}
