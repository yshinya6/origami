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

package origami.code;

import origami.lang.OEnv;
import origami.lang.OLocalVariable;
import origami.lang.OMethodDecl;
import origami.lang.type.OType;
import origami.lang.type.OUntypedType;
import origami.lang.type.OVarType;
import origami.nez.ast.Tree;
import origami.rule.SyntaxAnalysis;
import origami.rule.TypeAnalysis;
import origami.util.ODebug;

public class OUntypedCode extends OParamCode<Tree<?>> implements SyntaxAnalysis, TypeAnalysis {

	public OUntypedCode(OEnv env, Tree<?> t) {
		super(t, env.t(OUntypedType.class));
	}

	public Tree<?> getSyntaxTree() {
		return this.getHandled();
	}

	@Override
	public Object eval(OEnv env) throws Throwable {
		return this.getHandled();
	}

	public OCode typeCheck(OEnv env, OType type) {
		return this.typeCheck(env, type, this.getSyntaxTree());
	}

	public OCode typeCheck(OEnv env0, OMethodDecl mdecl) {
		// ODebug.trace("typing body: %s", mdecl);
		OEnv env = env0.newEnv();
		this.setFunctionContext(env, mdecl);
		OType[] op = mdecl.getThisParamTypes();
		String[] names = mdecl.getThisParamNames();
		for (int i = 0; i < op.length; i++) {
			env.add(names[i], new OLocalVariable(true, names[i], op[i]));
			// ODebug.trace("'%s' %s", names[i], env.get(names[i],
			// NameDecl.class));
		}
		OCode typedCode = null;
		Tree<?> t = this.getSyntaxTree();
		if (mdecl.returnType.isUntyped()) {
			mdecl.returnType = new OVarType("return", mdecl.returnType);
			typedCode = this.typeBlock(env, mdecl, t);
			ODebug.trace("varType=%s", mdecl.returnType);
			mdecl.returnType = ((OVarType) mdecl.returnType).thisType();
		} else {
			typedCode = this.typeBlock(env, mdecl, t);
		}
		// ODebug.trace("code %s : %s => %s", t, codeType, code);
		ODebug.trace("typed method: %s", this);
		return typedCode;
	}

	private OCode typeBlock(OEnv env, OMethodDecl mdecl, Tree<?> t) {
		OCode code = this.typeExprOrErrorCode(env, t);
		code = this.typeCheck(env, mdecl.getReturnType(), code);
		if (!code.hasReturnCode()) {
			code = new OReturnCode(env, code);
		}
		return code;
	}

}
