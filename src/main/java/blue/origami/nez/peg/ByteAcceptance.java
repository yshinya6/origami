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

package blue.origami.nez.peg;

import blue.origami.nez.peg.Expression.PNonTerminal;

public enum ByteAcceptance {
	Accept, Unconsumed, Reject;

	static Analyzer analyzer = new Analyzer();

	public static ByteAcceptance acc(Production p, int c) {
		return acc(p.getGrammar(), p.getLocalName(), p.getExpression(), c);
	}

	public static ByteAcceptance acc(Grammar g, String name, Expression e, int c) {
		ByteAcceptance[] acc = g.getProperty(name, ByteAcceptance[].class);
		if (acc == null) {
			acc = new ByteAcceptance[256];
			g.setProperty(name, acc);
		}
		if (acc[c] == null) {
			acc[c] = analyzer.compute(e, c);
		}
		return acc[c];
	}

	public final static ByteAcceptance acc(Expression e, int ch) {
		if (e instanceof PNonTerminal) {
			return acc(((PNonTerminal) e).getProduction(), ch);
		}
		return analyzer.accept(e, ch);
	}

	public final static boolean isDisjoint(Expression e, Expression e2) {
		for (int ch = 0; ch < 256; ch++) {
			if (acc(e, ch) == Reject) {
				continue;
			}
			if (acc(e2, ch) == Reject) {
				continue;
			}
			return false;
		}
		return true;
	}

	private final static class Analyzer extends ExpressionVisitor<ByteAcceptance, Integer> {

		public ByteAcceptance compute(Expression e, int c) {
			return e.visit(this, c);
		}

		public ByteAcceptance accept(Expression e, Integer ch) {
			return e.visit(this, ch);
		}

		@Override
		public ByteAcceptance visitNonTerminal(Expression.PNonTerminal e, Integer ch) {
			Production p = e.getProduction();
			return ByteAcceptance.acc(p, ch);
			// if (this.memo != null) {
			// return memo.acc(p, ch);
			// }
			// try {
			// return accept(p.getExpression(), ch);
			// } catch (StackOverflowError ex) {
			// Verbose.debug(e + " at " + e.getLocalName());
			// }
			// return Accept;
		}

		@Override
		public ByteAcceptance visitEmpty(Expression.PEmpty e, Integer ch) {
			return Unconsumed;
		}

		@Override
		public ByteAcceptance visitFail(Expression.PFail e, Integer ch) {
			return Reject;
		}

		@Override
		public ByteAcceptance visitByte(Expression.PByte e, Integer ch) {
			return (e.byteChar == ch) ? Accept : Reject;
		}

		@Override
		public ByteAcceptance visitByteSet(Expression.PByteSet e, Integer ch) {
			return (e.is(ch)) ? Accept : Reject;
		}

		@Override
		public ByteAcceptance visitAny(Expression.PAny e, Integer ch) {
			return (ch == 0) ? Reject : Accept;
			// return Accept;
		}

		@Override
		public ByteAcceptance visitPair(Expression.PPair e, Integer ch) {
			ByteAcceptance r = accept(e.get(0), ch);
			if (r == Unconsumed) {
				return accept(e.get(1), ch);
			}
			return r;
		}

		@Override
		public ByteAcceptance visitChoice(Expression.PChoice e, Integer ch) {
			boolean hasUnconsumed = false;
			for (int i = 0; i < e.size(); i++) {
				ByteAcceptance r = accept(e.get(i), ch);
				if (r == Accept) {
					return r;
				}
				if (r == Unconsumed) {
					hasUnconsumed = true;
				}
			}
			return hasUnconsumed ? Unconsumed : Reject;
		}

		@Override
		public ByteAcceptance visitDispatch(Expression.PDispatch e, Integer ch) {
			int index = e.indexMap[ch] & 0xff;
			if (index == 0) {
				return Reject;
			}
			return accept(e.get(index - 1), ch);
		}

		@Override
		public ByteAcceptance visitOption(Expression.POption e, Integer ch) {
			ByteAcceptance r = accept(e.get(0), ch);
			return (r == Accept) ? r : Unconsumed;
		}

		@Override
		public ByteAcceptance visitRepetition(Expression.PRepetition e, Integer ch) {
			if (e.isOneMore()) {
				return accept(e.get(0), ch);
			}
			ByteAcceptance r = accept(e.get(0), ch);
			return (r == Accept) ? r : Unconsumed;
		}

		@Override
		public ByteAcceptance visitAnd(Expression.PAnd e, Integer ch) {
			ByteAcceptance r = accept(e.get(0), ch);
			return (r == Reject) ? r : Unconsumed;
		}

		@Override
		public ByteAcceptance visitNot(Expression.PNot e, Integer ch) {
			Expression inner = e.get(0);
			if (inner instanceof Expression.PByte || inner instanceof Expression.PByteSet
					|| inner instanceof Expression.PAny) {
				return accept(inner, ch) == Accept ? Reject : Unconsumed;
			}
			/* The code below works only if a single character in !(e) */
			/* we must accept 'i' for !'int' 'i' */
			return Unconsumed;
		}

		@Override
		public ByteAcceptance visitTree(Expression.PTree e, Integer ch) {
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitLinkTree(Expression.PLinkTree e, Integer ch) {
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitTag(Expression.PTag e, Integer ch) {
			return Unconsumed;
		}

		@Override
		public ByteAcceptance visitReplace(Expression.PReplace e, Integer ch) {
			return Unconsumed;
		}

		@Override
		public ByteAcceptance visitDetree(Expression.PDetree e, Integer ch) {
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitSymbolScope(Expression.PSymbolScope e, Integer ch) {
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitSymbolAction(Expression.PSymbolAction e, Integer ch) {
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitSymbolPredicate(Expression.PSymbolPredicate e, Integer ch) {
			if (e.funcName == NezFunc.exists) {
				return Unconsumed;
			}
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitScan(Expression.PScan e, Integer ch) {
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitRepeat(Expression.PRepeat e, Integer ch) {
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitIf(Expression.PIfCondition e, Integer ch) {
			return Unconsumed;
		}

		@Override
		public ByteAcceptance visitOn(Expression.POnCondition e, Integer ch) {
			return accept(e.get(0), ch);
		}

		@Override
		public ByteAcceptance visitTrap(Expression.PTrap e, Integer ch) {
			return Unconsumed;
		}
	}
}
