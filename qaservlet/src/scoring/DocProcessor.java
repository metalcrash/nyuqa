package scoring;

import java.io.*;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;

import opennlp.tools.tokenize.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.tartarus.snowball.ext.englishStemmer;

public class DocProcessor {
    private String content;
    private String url;
    private String[] tokens;
    private InputStream is;
    
    //*construct for two kinds of input:solr document or String of content
    public DocProcessor(SolrDocument doc) throws NullPointerException{
		//System.out.println(doc);
		content=doc.get("body").toString();
		content=content.replaceAll("^\\[(\\s)*", "");
		content=content.replaceAll("(\\s)*\\]$", "");
		url=doc.get("url").toString();
		System.out.println("url:"+content);
		if(content==null)throw new NullPointerException();
    	try {
    		String classPath = this.getClass().getClassLoader().getResource("/").getPath();
	    	is = new FileInputStream(classPath+"en-token.bin");
	    	//is = new FileInputStream("en-token.bin");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    public DocProcessor(String content){
    	this.content=content;
    	try {
    		String classPath = this.getClass().getClassLoader().getResource("/").getPath();
	    	is = new FileInputStream(classPath+"en-token.bin");
	    	//is = new FileInputStream("en-token.bin");
		} catch (IOException e) {
			e.printStackTrace();
		}
    }
    //*
    public float score(String[] keywords){
    	RealCounter rc=new RealCounter(tokens,keywords);
    	//count score by keywords appearance and distance between them
    	float scoreF=rc.scorePass();
    	//complete scoring formula
		float score=scoreF+2*isFaq();
		return score;
    }
    public static void process(SolrDocumentList doclist) {
    	File writename = new File("cmp3.txt");   
        try {
			writename.createNewFile();
	        BufferedWriter out = new BufferedWriter(new FileWriter(writename));
			String[] keywords={"adimission","phd"};
			for(int i=0;i<doclist.size();i++){
				try{
					DocProcessor dp=new DocProcessor(doclist.get(i));
					dp.tokenizing();
					dp.stem();
					String[] tokens=dp.getTokens();
					for(int t=0;t<tokens.length;t++){
						out.write(t+":"+tokens[t]+"\r\n");
					}
					RealCounter rc=new RealCounter(tokens,keywords);
					rc.countEach();
					int accumscore=rc.getAccum();
					int[] eachCount=rc.getEachCount();
					System.out.println(i+":");
					System.out.println("accumulate count:"+accumscore);
					for(int j=0;j<keywords.length;j++){
						System.out.println("count of "+keywords[j]+":"+eachCount[j]);
					}
				}catch(NullPointerException f){
					continue;
				}
			}
			out.flush(); 
	        out.close();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    public void tokenizing(){
		TokenizerModel model;
		try {
			model = new TokenizerModel(this.is);
			Tokenizer tokenizer = new TokenizerME(model);
			//System.out.println(this.content);
	    	this.tokens=tokenizer.tokenize(this.content);
		} catch (IOException e) {
			e.printStackTrace();
		} catch(NullPointerException f){
			
		}
    }
    public void stem(){
    	for(int i=0;i<tokens.length;i++){
    		englishStemmer st=new englishStemmer();
    		//System.out.println("*:"+tokens[i]);
    		char[] ca=tokens[i].toCharArray();
    		//System.out.println("**:"+new String(ca));
    		st.setCurrent(tokens[i]);
    		//System.out.println("***:"+new String(st.b));
    		st.stem();
    		String result=st.getCurrent();
    		tokens[i]=new String(result);
    	}
    }
    public int isFaq(){
    	String faqP="(^|\\W)[Ff][Aa][Qq](s)?";
    	Pattern faq=Pattern.compile(faqP);
    	Matcher matcher=faq.matcher(url);
    	if(matcher.find())return 1;
    	else return 0;
    }
    public void toLower(){
    	try {
    		for(int i=0;i<this.tokens.length;i++)this.tokens[i].toLowerCase();
    	} catch(NullPointerException f){
			
		}
    }
    public String[] getTokens(){
    	return this.tokens;
    }
    public String getContent(){
    	return this.content;
    }
}
