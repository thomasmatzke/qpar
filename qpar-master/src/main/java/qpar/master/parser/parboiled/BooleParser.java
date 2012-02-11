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
package qpar.master.parser.parboiled;

import org.parboiled.BaseParser;

/**
 * @author thomasm
 * 
 */
public class BooleParser extends BaseParser<Object> {

}

// <input> ::= <exp> EOF
// <exp> ::= <NOT> <exp> | <q_set> <exp> | <LP> <exp> <op> <exp> <RP> | <LP>
// <exp> <RP> | <VAR>
// <q_set> ::= <quant> <LSP> <var_list> <RSP>
// <quant> ::= <EXISTS> | <FORALL>
// <var_list> ::= <VAR> <var_list> | <VAR>
// <op> ::= <OR> | <AND>
// <NOT> ::= "!"
// <LP> ::= "("
// <RP> ::= ")"
// <LSP> ::= "["
// <RSP> ::= "]"
// <OR> ::= "|"
// <AND> ::= "&"
// <EXISTS> ::= "exists"
// <FORALL> ::= "forall"
// <VAR> ::= {A sequence of non-special ASCII characters}

