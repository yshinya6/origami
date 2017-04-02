package blue.nez.parser;

import blue.origami.util.OOption.Key;

public enum ParserOption implements Key {
	WindowSize, Pass, Unoptimized, PassPath, StrictChecker, TrapActions, //
	TreeConstruction, PackratParsing, Coverage, GrammarFile, GrammarPath, Start, //
	PartialFailure, ThrowingParserError, InlineGrammar, InputFiles;

	@Override
	public String toString() {
		return this.name();
	}

	@Override
	public Key keyOf(String key) {
		return ParserOption.valueOf(key);
	}
}
