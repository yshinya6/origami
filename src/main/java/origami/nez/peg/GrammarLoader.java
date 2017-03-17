/***********************************************************************
 * Copyright 2017 Kimio Kuramitsu and ORIGAMI project
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ***********************************************************************/

package origami.nez.peg;

import java.io.IOException;

import origami.nez.ast.Source;
import origami.nez.ast.Symbol;
import origami.nez.ast.Tree;
import origami.nez.parser.CommonSource;
import origami.nez.parser.ParserFactory;

public class GrammarLoader {
	public final static Symbol _Source = Symbol.unique("Source");
	public final static Symbol _Import = Symbol.unique("Import");
	public final static Symbol _Grammar = Symbol.unique("Grammar");
	public final static Symbol _Production = Symbol.unique("Production");
	public final static Symbol _body = Symbol.unique("body");
	public final static Symbol _public = Symbol.unique("public");

	public final static Symbol _name = Symbol.unique("name");
	public final static Symbol _expr = Symbol.unique("expr");
	public final static Symbol _symbol = Symbol.unique("symbol");
	public final static Symbol _min = Symbol.unique("min");
	public final static Symbol _mask = Symbol.unique("mask"); // <scanf >

	public final static Symbol _String = Symbol.unique("String");

	public final static OGrammar load(ParserFactory factory, String path) throws IOException {
		return load(factory, CommonSource.newFileSource(path, factory.list("grammar-path")));
	}

	public final static OGrammar load(ParserFactory factory, Source s) throws IOException {
		OGrammar g = new OGrammar(s.getResourceName());
		ExpressionParser ep = new ExpressionParser(factory, g);
		importFile(factory, ep, g, s);
		return g;
	}

	public final static void importFile(ParserFactory factory, ExpressionParser ep, OGrammar g, Source s) throws IOException {
		Tree<?> t = OGrammar.NezParser.parse(s);
		update(factory, ep, g, t);
	}

	public final static void update(ParserFactory factory, ExpressionParser ep, OGrammar g, Tree<?> node) throws IOException {
		if (node.is(_Source)) {
			for (Tree<?> sub : node) {
				parse(factory, ep, g, sub);
			}
		}
	}

	static void parse(ParserFactory factory, ExpressionParser ep, OGrammar g, Tree<?> node) throws IOException {
		if (node.is(_Production)) {
			parseProduction(factory, g, ep, node);
			return;
		}
		if (node.is(_Grammar)) {
			String name = node.getText(_name, null);
			g = new OGrammar(name, g);
			ep = new ExpressionParser(factory, g);
			Tree<?> body = node.get(_body);
			for (Tree<?> sub : body) {
				parse(factory, ep, g, sub);
			}
			return;
		}
		if (node.is(_Import)) {
			String name = node.getText(_name, null);
			String path = name;
			if (!name.startsWith("/") && !name.startsWith("\\")) {
				path = extractFilePath(node.getSource().getResourceName()) + "/" + name;
			}
			importFile(factory, ep, g, CommonSource.newFileSource(path, null));
			return;
		}
	}

	static void parseProduction(ParserFactory factory, OGrammar g, ExpressionParser ep, Tree<?> node) {
		Tree<?> nameNode = node.get(_name);
		boolean isPublic = node.get(_public, null) != null;
		String name = nameNode.toText();
		if (nameNode.is(_String)) {
			name = OProduction.terminalName(name);
		}
		Expression rule = g.getLocalExpression(name);
		if (rule != null) {
			factory.reportWarning(node, "duplicated production: " + name);
			return;
		}
		g.addProduction(name, ep.newInstance(node.get(_expr)));
	}

	public final static String extractFilePath(String path) {
		int loc = path.lastIndexOf('/');
		if (loc > 0) {
			return path.substring(0, loc);
		}
		loc = path.lastIndexOf('\\');
		if (loc > 0) {
			return path.substring(0, loc);
		}
		return path;
	}

	public final static String extractFileName(String path) {
		int loc = path.lastIndexOf('/');
		if (loc > 0) {
			return path.substring(loc + 1);
		}
		loc = path.lastIndexOf('\\');
		if (loc > 0) {
			return path.substring(loc + 1);
		}
		return path;
	}

	public final static String extractFileExtension(String path) {
		int loc = path.lastIndexOf('.');
		if (loc > 0) {
			return path.substring(loc + 1);
		}
		return path;
	}

	public final static String changeFileExtension(String path, String ext) {
		int loc = path.lastIndexOf('.');
		if (loc > 0) {
			return path.substring(0, loc + 1) + ext;
		}
		return path + "." + ext;
	}

}