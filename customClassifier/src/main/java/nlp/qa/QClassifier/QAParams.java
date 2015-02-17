package nlp.qa.QClassifier;

/*
*   Define Frequently used constants
*/


public interface QAParams {
  public static final String QA_PREFIX = "qa.";
  
  public static final String PRIMARY_WINDOW_SIZE = QA_PREFIX + "pws";

  public static final String ADJACENT_WINDOW_SIZE = QA_PREFIX + "aws";

  public static final String SECONDARY_WINDOW_SIZE = QA_PREFIX + "sws";

  public static final String QUERY_FIELD = QA_PREFIX + "qf";

  public static final String QA_ROWS = QA_PREFIX + "rows";

  public static final String SLOP = QA_PREFIX + "qSlop";

  public static final String BIGRAM_WEIGHT = QA_PREFIX + "bw";

  public static final String ADJACENT_WEIGHT = QA_PREFIX + "aw";

  public static final String SECOND_ADJ_WEIGHT = QA_PREFIX + "saw";

  public static final String COMPONENT_NAME = "qa";
}
