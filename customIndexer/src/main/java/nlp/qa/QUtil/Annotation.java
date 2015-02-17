package nlp.qa.QUtil;

import opennlp.tools.util.Span;

/*
*   Annotates entity based on probability from wordNet
*/

public class Annotation implements Comparable<Annotation> 
{
	private String name;
	private Span span;
	private double probility;
	
	public Annotation(){};
	public Annotation(String n, Span s , double probs){
		this.name= n;
		this.span = s;
		this.probility = probs;
	}
	

	public Span getSpan() { return this.span; } 
	public double getProb() { return this.probility; }
	public String getName() { return this.name; }
	
    public String toString() {
	return name + " " + span + " " + probility;
    }

    public int compareTo(Annotation n) {
        return probility > n.probility ? 1 : -1; 
    }


}
