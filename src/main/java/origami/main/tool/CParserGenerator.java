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

package origami.main.tool;

import origami.nez.peg.Grammar;
import origami.util.OStringUtils;

public class CParserGenerator extends ParserGenerator {

	public CParserGenerator() {
		// this.fileBase = "cnez";
		// super(".c");
	}

	@Override
	protected void initLanguageSpec() {
		SupportedRange = true;
		SupportedMatch2 = true;
		SupportedMatch3 = true;
		SupportedMatch4 = true;
		SupportedMatch5 = true;
		SupportedMatch6 = true;
		SupportedMatch7 = true;
		SupportedMatch8 = true;

		this.addType("$parse", "int");
		this.addType("$tag", "int");
		this.addType("$label", "int");
		this.addType("$table", "int");
		this.addType("$arity", "int");
		this.addType("$text", "const unsigned char");
		this.addType("$index", "const unsigned char");
		if (UsingBitmap) {
			this.addType("$set", "int");
		} else {
			this.addType("$set", "const unsigned char");
		}
		this.addType("$range", "const unsigned char __attribute__((aligned(16)))");
		this.addType("$string", "const char *");

		this.addType("memo", "int");
		if (UsingBitmap) {
			this.addType(_set(), "int");
		} else {
			this.addType(_set(), "const unsigned char *");/* boolean */
		}
		this.addType(_index(), "const unsigned char *");
		this.addType(_temp(), "int");/* boolean */
		this.addType(_pos(), "const unsigned char *");
		this.addType(_tree(), "size_t");
		this.addType(_log(), "size_t");
		this.addType(_table(), "size_t");
		this.addType(_state(), "ParserContext *");
	}

	@Override
	protected String _True() {
		return "1";
	}

	@Override
	protected String _False() {
		return "0";
	}

	@Override
	protected String _Null() {
		return "NULL";
	}

	/* Expression */

	@Override
	protected String _Field(String o, String name) {
		return o + "->" + name;
	}

	@Override
	protected String _Func(String name, String... args) {
		StringBuilder sb = new StringBuilder();
		sb.append("ParserContext_");
		sb.append(name);
		sb.append("(");
		sb.append(_state());
		for (int i = 0; i < args.length; i++) {
			sb.append(",");
			sb.append(args[i]);
		}
		sb.append(")");
		return sb.toString();
	}

	@Override
	protected String _text(byte[] text) {
		return super._text(text) + ", " + _int(text.length);
	}

	@Override
	protected String _text(String key) {
		if (key == null) {
			return _Null() + ", 0";
		}
		return nameMap.get(key) + ", " + _int(OStringUtils.utf8(key).length);
	}

	@Override
	protected String _defun(String type, String name) {
		if (this.crossRefNames.contains(name)) {
			return type + " " + name;
		}
		return "static inline " + type + " " + _rename(name);
	}

	@Override
	protected String _rename(String name) {
		char[] l = name.toCharArray();
		StringBuilder result = new StringBuilder();
		for (char ch : l) {
			if (('a' <= ch && ch <= 'z') || ('A' <= ch && ch <= 'Z') || ('0' <= ch && ch <= '9') || ch == '_') {
				result.append(ch);
				continue;
			}
			result.append((int) ch);
		}
		return result.toString();
	}

	/* Statement */

	@Override
	protected void DeclConst(String type, String name, String expr) {
		Statement("static " + type + " " + name + " = " + expr);
	}

	// Grammar Generator

	@Override
	protected void generateHeader(Grammar g) {
		importFileContent("cnez-runtime.txt");
	}

	@Override
	protected void generatePrototypes() {
		LineComment("Prototypes");
		for (String name : this.crossRefNames) {
			Statement(_defun("int", name) + "(ParserContext *c)");
		}
	}

	@Override
	protected void generateFooter(Grammar g) {
		importFileContent("cnez-utils.txt");
		//
		BeginDecl("void* " + _ns() + "parse(const char *text, size_t len, void *thunk, void* (*fnew)(symbol_t, const unsigned char *, size_t, size_t, void *), void  (*fset)(void *, size_t, symbol_t, void *, void *), void  (*fgc)(void *, int, void *))");
		{
			VarDecl("void*", "result", _Null());
			VarDecl(_state(), "ParserContext_new((const unsigned char*)text, len)");
			Statement(_Func("initTreeFunc", "thunk", "fnew", "fset", "fgc"));
			this.InitMemoPoint();
			If(_funccall(_funcname(g.getStartProduction())));
			{
				VarAssign("result", _Field(_state(), _tree()));
				If("result == NULL");
				{
					Statement("result = c->fnew(0, (const unsigned char*)text, (c->pos - (const unsigned char*)text), 0, c->thunk)");
				}
				EndIf();
			}
			EndIf();
			Statement(_Func("free"));
			Return("result");
		}
		EndDecl();
		BeginDecl("static void* cnez_parse(const char *text, size_t len)");
		{
			Return(_ns() + "parse(text, len, NULL, NULL, NULL, NULL)");
		}
		EndDecl();
		BeginDecl("long " + _ns() + "match(const char *text, size_t len)");
		{
			VarDecl("long", "result", "-1");
			VarDecl(_state(), "ParserContext_new((const unsigned char*)text, len)");
			Statement(_Func("initNoTreeFunc"));
			this.InitMemoPoint();
			If(_funccall(_funcname(g.getStartProduction())));
			{
				VarAssign("result", _cpos() + "-" + _Field(_state(), "inputs"));
			}
			EndIf();
			Statement(_Func("free"));
			Return("result");
		}
		EndDecl();
		BeginDecl("const char* " + _ns() + "tag(symbol_t n)");
		{
			Return("_tags[n]");
		}
		EndDecl();
		BeginDecl("const char* " + _ns() + "label(symbol_t n)");
		{
			Return("_labels[n]");
		}
		EndDecl();
		L("#ifndef UNUSE_MAIN");
		BeginDecl("int main(int ac, const char **argv)");
		{
			Return("cnez_main(ac, argv, cnez_parse)");
		}
		EndDecl();
		L("#endif/*MAIN*/");
		L("// End of File");
		// generateHeaderFile();
		// this.showManual("cnez-man.txt", new String[] { "$cmd$", _basename()
		// });
	}

	private void generateHeaderFile() {
		// FIXME : this.setFileBuilder(".h");
		Statement("typedef unsigned long int symbol_t");
		int c = 1;
		for (String s : this.tagList) {
			if (s.equals("")) {
				continue;
			}
			L("#define _" + s + " ((symbol_t)" + c + ")");
			c++;
		}
		L("#define MAXTAG " + c);
		c = 1;
		for (String s : this.labelList) {
			if (s.equals("")) {
				continue;
			}
			L("#define _" + s + " ((symbol_t)" + c + ")");
			c++;
		}
		L("#define MAXLABEL " + c);
		Statement("void* " + _ns() + "parse(const char *text, size_t len, void *, void* (*fnew)(symbol_t, const char *, size_t, size_t, void *), void  (*fset)(void *, size_t, symbol_t, void *, void *), void  (*fgc)(void *, int, void *))");
		Statement("long " + _ns() + "match(const char *text, size_t len)");
		Statement("const char* " + _ns() + "tag(symbol_t n)");
		Statement("const char* " + _ns() + "label(symbol_t n)");
		this.close();
	}

}
