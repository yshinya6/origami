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

package origami;

import origami.code.OAssignCode;
import origami.code.OCode;
import origami.util.OScriptUtils;

public class AsmTest {

	public Object eval(OEnv env, OCode code) throws Throwable {
		return OScriptUtils.eval(env, code);
	}

	// public void testNull() throws Throwable {
	// OEnv env = new OrigamiContext();
	// assert this.eval(env, new ONullCode(env)) == null;
	// }

	public void testBool() throws Throwable {
		OEnv env = new OrigamiContext();
		assert this.eval(env, env.v(true)).equals(true);
	}

	public void testInt() throws Throwable {
		OEnv env = new OrigamiContext();
		assert this.eval(env, env.v(1)).equals(1);
	}

	public void testDouble() throws Throwable {
		OEnv env = new OrigamiContext();
		assert this.eval(env, env.v(1.0)).equals(1.0);
	}

	public void testString() throws Throwable {
		OEnv env = new OrigamiContext();
		assert this.eval(env, env.v("hoge")).equals("hoge");
	}

	public void testLocalVariable() throws Throwable {
		OEnv env = new OrigamiContext();
		OAssignCode body = new OAssignCode(true, "v", env.v(1));
		assert this.eval(env, body).equals(1);
	}

}
