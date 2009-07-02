package main.java.logic;

import java.util.List;
/**
 * Representation of a quantified boolean formula
 * @author thomasm
 * 
 */
public interface Qbf {
	// We have a QBF
	// We assign a variable (with 1 and 0)
	// We get 2 sub-QBFs
	// IF Universal-quantifier THEN both sub-QBFs have to be in SAT
	// IF Exist. Quantifier THEN either or sub-qbf must be in SAT
	// -> IF Universal-Quant. AND one QBF is not in SAT THEN break
	
	// FOR sub-qbfs DO
	// simplify (substitute?) (zb. a && 1 -> a; a )
	// feed sub-qbf to qpro	
	// END
	// test
	/**
	 * Parses an qpro-like argument-string into 
	 * internal representation
	 */
	public void parseQbf(String s);
	
	/**
	 * Tells the Qbf to assign a variable.
	 * Returns a List of resulting sub-qbfs.
	 * In boolean logic this would be 2
	 */
	public List<Qbf> assignVariable(Variable v);
	
	/**
	 * Simplifies/Reduces internal representation
	 * TODO: Do we need this? Does qPro do this internally?
	 */
	public void reduce();
	
	/**
	 * Converts internal formula-representation into qpro-
	 * readable argument string and lets qpro evaluate
	 * @return	true if formula is in SAT, false otherwise
	 */
	public boolean solve();
	
}
