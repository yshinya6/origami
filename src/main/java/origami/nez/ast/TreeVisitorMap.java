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

package origami.nez.ast;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import origami.util.OVerbose;

public class TreeVisitorMap<V> {
	private static boolean OnWhenDebugging = false;

	protected V defaultAcceptor;
	protected HashMap<String, V> visitors;

	protected void init(Class<?> baseClass, V defualtAccepter) {
		this.defaultAcceptor = defualtAccepter;
		this.visitors = new HashMap<>();
		visitors.put(defualtAccepter.getClass().getSimpleName(), defaultAcceptor);
		if (OnWhenDebugging) {
			System.out.println("base: " + baseClass);
		}
		for (Class<?> c : baseClass.getClasses()) {
			load(baseClass, c);
		}
	}

	@SuppressWarnings("unchecked")
	private void load(Class<?> baseClass, Class<?> c) {
		try {
			Constructor<?> cc = c.getConstructor(baseClass);
			Object v = cc.newInstance(this);
			if (check(defaultAcceptor.getClass(), v.getClass())) {
				String n = c.getSimpleName();
				if (n.startsWith("_")) {
					n = n.substring(1);
				}
				if (OnWhenDebugging) {
					System.out.println(" #" + n);
				}
				visitors.put(n, (V) v);
			}
		} catch (NoSuchMethodException | SecurityException | InvocationTargetException | IllegalAccessException | InstantiationException | IllegalArgumentException e) {
			//OConsole.exit(1, e);
		}
	}

	private boolean check(Class<?> v, Class<?> e) {
		return v.isAssignableFrom(e);
	}

	public final void add(String name, V visitor) {
		visitors.put(name, visitor);
	}

	protected final V find(String name) {
		V v = visitors.get(name);
		return v == null ? defaultAcceptor : v;
	}

//	protected final void undefined(Tree<?> node) {
//		OVerbose.println("undefined: " + node);
//		throw new UndefinedException(node, this.getClass().getName() + ": undefined " + node);
//	}
//
//	@SuppressWarnings("serial")
//	public static class UndefinedException extends RuntimeException {
//		Tree<?> node;
//
//		public UndefinedException(Tree<?> node, String msg) {
//			super(node.formatSourceMessage("error", msg));
//			this.node = node;
//		}
//
//		public UndefinedException(Tree<?> node, String fmt, Object... args) {
//			super(node.formatSourceMessage("error", String.format(fmt, args)));
//			this.node = node;
//		}
//	}
}
