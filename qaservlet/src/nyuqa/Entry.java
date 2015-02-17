package nyuqa;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;

import org.apache.solr.common.SolrDocumentList;

import scoring.DocProcessor;
import scoring.RealCounter;

public class Entry {
	private SolrDocumentList doclist;
	private String[] keywords;
	private String querystr;
	private String[] top3;
	private float[] scores;
	public Entry(SolrDocumentList doclist,String querystr){
		this.doclist=doclist;
		this.querystr=querystr;
		this.querystr=removeStopWords();
		DocProcessor dp=new DocProcessor(this.querystr);
		dp.tokenizing();
		dp.stem();
		this.keywords=dp.getTokens();
		//System.out.println("*"+this.querystr);
	}
	public void score(){
		//scoring and find top 3 within 1 pass
		scores=new float[3];
		top3=new String[3];
		for(int i=0;i<3;i++){
			top3[i]=new String();
			scores[i]=(float)0;
		}
		//System.out.println(doclist.size());
		for(int i=0;i<doclist.size();i++){
			try{
				DocProcessor dp=new DocProcessor(doclist.get(i));
				dp.tokenizing();
				dp.stem();
				//System.out.println(dp.getContent());
				/*for(int j=2;j>=0;j--){
					System.out.println("top"+(3-j)+": "+scores[j]);
					System.out.println(top3[j]);
				}*/
				float score=dp.score(this.keywords);
				for(int j=2;j>=0;j--){
					if(score>scores[j]){
						for(int k=0;k<j;k++){
							scores[k]=scores[k+1];
							top3[k]=top3[k+1];
						}
						scores[j]=score;
						top3[j]=dp.getContent();
						break;
					}
				}
				/*for(int j=2;j>=0;j--){
					System.out.println("top"+(3-j)+": "+scores[j]);
					System.out.println(top3[j]);
				}
				System.out.println("*******************************");*/
			}catch(NullPointerException f){
				continue;
			}
		}
	}
	public String[] getTop3(){
		return this.top3;
	}
	public float[] getScores(){
		return this.scores;
	}
	public String[] getKeywords(){
		return this.keywords;
	}
	private String removeStopWords(){
		String classPath = this.getClass().getClassLoader().getResource("/").getPath();
		File file = new File(classPath+"stopwords.txt");
        BufferedReader reader = null;
        HashSet<String> stoplist = new HashSet<String>();
        try {
            reader = new BufferedReader(new FileReader(file));
            String curStop=null;
            while ((curStop = reader.readLine()) != null) {
                stoplist.add(curStop);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        String[] words=this.querystr.split("\\s+");
        int l=words.length;
        for(int i=0;i<words.length;i++){
        	if(stoplist.contains(words[i])){
        		words[i]="";
        		l-=1;
        	}
        }
        System.out.println("*l:"+l);
        String querystr="";
        for(int i=0;i<words.length;i++){
        	if(!words[i].equals("")){
        		querystr+=words[i]+" ";
        	}
       	}
        return querystr;
	}
}
