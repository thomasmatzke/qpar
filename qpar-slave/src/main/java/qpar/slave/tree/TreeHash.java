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
package qpar.slave.tree;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.lang.ArrayUtils;

import qpar.common.parser.Node;

public class TreeHash {

	private static MessageDigest md;

	public static byte[] treeHash(final Node n) {

		switch (n.getNodeType()) {
		case START:
			return ArrayUtils.addAll("s".getBytes(), treeHash(n.getChildren()[0]));
		case VAR:
			return Integer.valueOf(((n.getVar()))).toString().getBytes();
		case FORALL:
			return ArrayUtils.addAll("F".getBytes(), treeHash(n.getChildren()[0]));
		case EXISTS:
			return ArrayUtils.addAll("E".getBytes(), treeHash(n.getChildren()[0]));
		case AND:
			return ArrayUtils.addAll(ArrayUtils.addAll("a".getBytes(), treeHash(n.getChildren()[0])), treeHash(n.getChildren()[1]));
		case OR:
			return ArrayUtils.addAll(ArrayUtils.addAll("o".getBytes(), treeHash(n.getChildren()[0])), treeHash(n.getChildren()[1]));
		case NOT:
			return ArrayUtils.addAll("n".getBytes(), treeHash(n.getChildren()[0]));
		case TRUE:
			return md().digest("t".getBytes());
		case FALSE:
			return md().digest("f".getBytes());
		default:
			throw new RuntimeException("Unknown Nodetype encountered: " + n.getNodeType());
		}
	}

	synchronized static private MessageDigest md() {
		if (TreeHash.md == null) {
			try {
				TreeHash.md = MessageDigest.getInstance("SHA");
			} catch (NoSuchAlgorithmException e) {
			}
		}

		return TreeHash.md;
	}

	// public static <T> T[] concatAll(T[] first, T[]... rest) {
	// int totalLength = first.length;
	// for (T[] array : rest) {
	// totalLength += array.length;
	// }
	// T[] result = Arrays.copyOf(first, totalLength);
	// int offset = first.length;
	// for (T[] array : rest) {
	// System.arraycopy(array, 0, result, offset, array.length);
	// offset += array.length;
	// }
	// return result;
	// }

}
