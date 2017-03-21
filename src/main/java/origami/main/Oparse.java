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

import origami.nez.ast.Source;
import origami.nez.ast.Tree;
import origami.nez.parser.ParserSource;
import origami.nez.parser.Parser;
//import origami.nez.tool.LineTreeWriter;

public class Oparse extends OCommand {
	
	protected void initOption(OOption options) {
		super.initOption(options);
		options.set(ParserOption.ThrowingParserError, false);		
	}

	@Override
	public void exec(OOption options) throws Exception {
		Parser parser = getParser(options);
		
		TreeWriter treeWriter = options.newInstance(TreeWriter.class);
		treeWriter.init(options);
		if (options.value(ParserOption.InlineGrammar, null) != null) {
			Source input = ParserSource.newStringSource(options.value(ParserOption.InlineGrammar, null));
			Tree<?> node = parser.parse(input);
			if (node != null) {
				treeWriter.write(node);
			}
		}
		String[] files = options.list(ParserOption.InputFiles);
		this.checkInputSource(files);
		for (String file : files) {
			Source input = ParserSource.newFileSource(file, null);
			Tree<?> node = parser.parse(input);
			if (node != null) {
				treeWriter.write(node);
			}
		}
		treeWriter.close();
	}

}
