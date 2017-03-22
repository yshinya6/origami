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

package origami.main;

import origami.ODebug;
import origami.OrigamiContext;
import origami.nez.ast.SourcePosition;
import origami.nez.parser.ParserSource;
import origami.nez.peg.Grammar;

public class Orun extends origami.main.OCommand {

	@Override
	public void exec(OOption options) throws Throwable {
		String[] files = options.list(ParserOption.InputFiles);
		if (options.value(ParserOption.GrammarFile, null) == null) {
			if (files.length > 0) {
				String ext = SourcePosition.extractFileExtension(files[0]);
				options.set(ParserOption.GrammarFile, ext + ".nez");
			} 
		}
		Grammar g = getGrammar(options, "iroha.nez");
		OrigamiContext env = new OrigamiContext(g, options);
		ODebug.setDebug(this.isDebug());

		for (String file : files) {
			env.loadScriptFile(file);
		}
		if (files.length == 0 || isDebug()) {
			displayVersion(g.getName());
			p(Yellow, MainFmt.Tips__starting_with_an_empty_line_for_multiple_lines);
			p("");

			int startline = linenum;
			String prompt = bold(">>> ");
			String input = null;
			while ((input = this.readMulti(prompt)) != null) {
				if (checkEmptyInput(input)) {
					continue;
				}
				env.shell("<stdin>", startline, input);
				startline = linenum;
			}
		}
	}

	public boolean isDebug() {
		return false;
	}
}
