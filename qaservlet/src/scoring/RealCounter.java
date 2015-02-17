package scoring;

public class RealCounter {
	private int[] counter;
	private int accum;
	private String[] tokens;
	private String[] terms;
	public RealCounter(String[] tokens, String[] terms){
		this.accum=0;
		this.tokens=tokens;
		this.counter=new int[terms.length];
		for(int i=0;i<counter.length;i++)counter[i]=0;
		this.terms=terms;
	}
	public float scorePass(){
		int spaceCount=1;
		float scores=0;
		boolean found=false;
		for(int i=0;i<tokens.length;i++){
			if(found){
				boolean match=false;
				for(int j=0;j<terms.length;j++){
					if(tokens[i].equalsIgnoreCase(terms[j])){
						scores+=1/(float)spaceCount;
						spaceCount=1;
						match=true;
						break;
					}
				}
				if(!match)spaceCount+=1;
			}else{
				for(int j=0;j<terms.length;j++){
					if(tokens[i].equalsIgnoreCase(terms[j])){
						scores+=1/(float)spaceCount;
						spaceCount=0;
						found=true;
						break;
					}
				}
			}
		}
		return scores;
	}
	public void countEach(){
		for(int i=0;i<terms.length;i++){
			int c=count(terms[i]);
			accum+=c;
			counter[i]=c;
		}
	}
	public int getAccum(){
		return this.accum;
	}
	public int[] getEachCount(){
		return this.counter;
	}
	public int count(String term){
		int c=0;
		for(int i=0;i<tokens.length;i++){
			if(tokens[i].equalsIgnoreCase(term))c+=1;
		}
		return c;
	}
}
