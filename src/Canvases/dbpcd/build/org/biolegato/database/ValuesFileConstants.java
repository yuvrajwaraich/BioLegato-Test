/* Generated By:JavaCC: Do not edit this line. ValuesFileConstants.java */
package org.biolegato.database;


/**
 * Token literal values and constants.
 * Generated by org.javacc.parser.OtherFilesGen#start()
 */
public interface ValuesFileConstants {

  /** End of File. */
  int EOF = 0;
  /** RegularExpression Id. */
  int SQSTART = 1;
  /** RegularExpression Id. */
  int DQSTART = 2;
  /** RegularExpression Id. */
  int CDELIM = 3;
  /** RegularExpression Id. */
  int RDELIM = 4;
  /** RegularExpression Id. */
  int CHAR = 5;
  /** RegularExpression Id. */
  int SQEND = 6;
  /** RegularExpression Id. */
  int SCHAR = 7;
  /** RegularExpression Id. */
  int DQEND = 8;
  /** RegularExpression Id. */
  int DCHAR = 9;
  /** RegularExpression Id. */
  int WSP = 10;

  /** Lexical state. */
  int DEFAULT = 0;
  /** Lexical state. */
  int SQUOTE = 1;
  /** Lexical state. */
  int DQUOTE = 2;

  /** Literal token values. */
  String[] tokenImage = {
    "<EOF>",
    "\"\\\'\"",
    "\"\\\"\"",
    "<CDELIM>",
    "<RDELIM>",
    "<CHAR>",
    "\"\\\'\"",
    "<SCHAR>",
    "\"\\\"\"",
    "<DCHAR>",
    "<WSP>",
  };

}