/* Generated By:JavaCC: Do not edit this line. TSVFileConstants.java */
package org.biolegato.tables;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface TSVFileConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int CDELIM = 1;
  /** RegularExpression Id. */
  int RDELIM = 2;
  /** RegularExpression Id. */
  int SQSTART = 3;
  /** RegularExpression Id. */
  int DQSTART = 4;
  /** RegularExpression Id. */
  int CHAR = 5;
  /** RegularExpression Id. */
  int CHARS = 6;
  /** RegularExpression Id. */
  int SQEND = 7;
  /** RegularExpression Id. */
  int SCHAR = 8;
  /** RegularExpression Id. */
  int DQEND = 9;
  /** RegularExpression Id. */
  int DCHAR = 10;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int ACCEPTSQ = 1;
  /** Lexical state. */
  int SQUOTE = 2;
  /** Lexical state. */
  int DQUOTE = 3;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\"\\t\"",
    "<RDELIM>",
    "\"\\\'\"",
    "\"\\\"\"",
    "<CHAR>",
    "<CHARS>",
    "\"\\\'\"",
    "<SCHAR>",
    "\"\\\"\"",
    "<DCHAR>",
  };

}
