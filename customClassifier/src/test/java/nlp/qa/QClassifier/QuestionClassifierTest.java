package nlp.qa.QClassifier;


import org.apache.log4j.BasicConfigurator;

import junit.framework.TestCase;

public class QuestionClassifierTest extends TestCase {


    public QuestionClassifierTest(String name) {
        super(name);
    }

    public void testClassify() throws Exception {
    	System.out.println(this.getClass().getResource( "/models/answer.bin" ).toURI().toString());
    	BasicConfigurator.configure();
    	QuestionClassifier classifier = new QuestionClassifier();

		assertEquals( "M" , classifier.parse( "how much does your watch cost" ));
		assertEquals( "L" , classifier.parse( "Where is the cafe"));
		assertEquals( "P" , classifier.parse( "who is your teacher"));
		assertEquals( "L" , classifier.parse( "where is shanghi rank in terms of world population?"));
		assertEquals( "M" , classifier.parse( "how much will a bachular education from nyu cost"));
    }

}


