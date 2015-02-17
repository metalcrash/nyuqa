package nyuqa;

import java.io.IOException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;

import scoring.DocProcessor;
import scoring.RealCounter;
 
/**
 * @author metalcrash
 */
public class NyuQAServlet extends HttpServlet {
 
    private static final long serialVersionUID = 1L;
    
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException{
        // TODO Auto-generated method stub
    	//resp.setContentType("text/html");
        //PrintWriter out = resp.getWriter();
        //out.println(req.getParameter("qstn"));
    	long startTime = System.currentTimeMillis();
        RequestDispatcher rd = req.getRequestDispatcher("index.jsp"); 
        
        SolrDocumentList doclista=null;
        SolrDocumentList doclistb=null;
        if(req.getParameter("qstn")==null||req.getParameter("qstn").equals("\\s*")){
        	req.setAttribute("type", 0);
        }
        else{
        	String question=req.getParameter("qstn");
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
	        query.setQuery(question);
	        query.setParam("wt", "json");
	        query.setParam("debugQuery", "true");
	        String querystr="nothing...";
	        String[] top3=null;
	        String[] keywords=null;
	        try
	        {
	            QueryResponse qr = server.query(query);
	            try{
	            	querystr=(String)qr.getDebugMap().get("querystring");
	            }catch(NullPointerException e){
	            	querystr="call me null";
	            }
	            doclista = qr.getResults();
	        }
	        catch (SolrServerException e)
	        {
	            e.printStackTrace();
	        }
	        int emptyItem=0;
	        Entry entry=new Entry(doclista,querystr);
			entry.score();
			top3=entry.getTop3();
			for(int i=0;i<3;i++){
				if(top3[i].equals(""))emptyItem++;
			}
			if(emptyItem!=0){
		        query = new SolrQuery();
		        query.setRequestHandler("/select");
		        query.setQuery(question);
		        query.setParam("wt", "json");
		        try
		        {
		            QueryResponse qr = server.query(query);
		            doclistb = qr.getResults();
		        }
		        catch (SolrServerException e)
		        {
		            e.printStackTrace();
		        }
				entry=new Entry(doclistb,querystr);
				entry.score();
				String[] top3b=entry.getTop3();
		        for(int j=2,i=emptyItem-1;i>=0;j--,i--){
		        	top3[i]=top3b[j];
		        }
			}
			int length=0;
			for(int i=0;i<3;i++){
				if(!top3[i].equals("")){
					length+=1;
				}
			}
			if(length==0){
	        	//type 2:no doc return from solr
	        	req.setAttribute("type", 2);
		        req.setAttribute("query", question);
			}else{
				//normal return
				long endTime = System.currentTimeMillis();
				//req.setAttribute("keywords", keywords);
		        req.setAttribute("time", endTime-startTime);
				req.setAttribute("type", 1);
		        req.setAttribute("query", question);
		        req.setAttribute("answers", top3);
		        req.setAttribute("length", length);
			}
        }
        rd.forward(req,resp);
    }
 
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {
        // TODO Auto-generated method stub
        super.doPost(req, resp);
    }
    
/*    @Override
	protected void service(HttpServletRequest req, HttpServletResponse res)
			throws ServletException, IOException {
		res.getWriter().println("Hello World!");
	}*/
 
}