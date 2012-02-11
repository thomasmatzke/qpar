/*
Copyright (c) 2011 Thomas Matzke

This file is part of qpar.

qpar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package qpar.common.dom.formula;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import qpar.common.dom.heuristic.Heuristic;
import qpar.common.parser.jjtree.ParseException;
import qpar.common.parser.jjtree.Qbf_parser;
import qpar.common.parser.jjtree.SimpleNode;
import qpar.common.parser.jjtree.TokenMgrError;

/**
 * A QBF object contains one QBF as well as methods to split it up into subQBFs
 * and merging subresults back together
 * 
 */
public class Qbf {

	private static final Logger LOGGER = LoggerFactory.getLogger(Qbf.class);

	Heuristic h = null;
	// private static int idCounter = 0;

	private HashMap<Integer, Integer> literalCount = new HashMap<Integer, Integer>();
	public List<Integer> eVars = new ArrayList<Integer>();
	public List<Integer> aVars = new ArrayList<Integer>();
	public List<Integer> vars = new ArrayList<Integer>();
	public SimpleNode root = null;

	/**
	 * constructor
	 * 
	 * @param filename
	 *            The file containing the QBF that will be stored in this object
	 * @throws IOException
	 */
	public Qbf(final String filename) throws IOException {
		assert (filename != null);
		// this.id = Qbf.getUniqueId();

		long start = System.currentTimeMillis();
		// parse the formula, get various vectors of vars
		try {
			Qbf_parser parser = new Qbf_parser(new FileInputStream(filename));
			parser.ReInit(new FileInputStream(filename), null);
			LOGGER.debug("Begin parsing...");
			parser.Input();
			LOGGER.debug("Succesful parse");
			this.literalCount = parser.getLiteralCount();
			this.eVars = parser.getEVars();
			this.aVars = parser.getAVars();
			this.vars = parser.getVars();
			this.root = parser.getRootNode();

			// root.dump("> ");
			parser.doPostprocessing();
			LOGGER.debug("postprocessing...");
			// root.dump("> ");

			// logger.debug("Postprocessed Formula: \n" +
			// root.subTreetoString("  "));
		} catch (ParseException e) {
			LOGGER.error("Parse error", e);
			return;
		} catch (TokenMgrError e) {
			LOGGER.error(filename, e);
			throw e;
		}
		long end = System.currentTimeMillis();
		LOGGER.debug("Existential quantified Variables: " + this.eVars);
		LOGGER.debug("Number of e.q.vs: " + this.eVars.size());
		LOGGER.debug("Universally quantified Variables: " + this.aVars);
		LOGGER.debug("Number of u.q.vs: " + this.aVars.size());
		LOGGER.debug("All variables: " + this.vars);
		LOGGER.debug("Number of all v.: " + this.vars.size());
		LOGGER.debug("Finished parsing QBF from " + filename + ", Took: " + (end - start) / 1000 + " seconds.");
		LOGGER.debug("Generating dependency graph...");
		start = System.currentTimeMillis();
		end = System.currentTimeMillis();
		LOGGER.debug("Dependency graph generated. Took " + (end - start) / 1000 + " seconds.");
		// This hampers performance severely!! only use if debugging
		// logger.debug("Dependency Tree: \n" + dependencyGraphRoot.dump());
	}

	// synchronized private static int getUniqueId() {
	// idCounter++;
	// return idCounter;
	// }

	public boolean isUniversalQuantified(final Integer v) {
		return this.aVars.contains(v);
	}

	public HashMap<Integer, Integer> getLiteralCount() {
		return this.literalCount;
	}

	public void setHeuristic(final Heuristic h) {
		this.h = h;
	}
}
