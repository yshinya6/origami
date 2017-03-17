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

package origami.type;

import origami.trait.OStringBuilder;

public abstract class PhantomType implements OWrapperType {

	private OType base;

	public PhantomType(OType wrapped) {
		this.base = wrapped;
	}

	@Override
	public OType thisType() {
		return base;
	}

	public void setType(OType t) {
		base = t;
	}

	@Override
	public OType valueType() {
		return base;
	}

	@Override
	public String toString() {
		return OStringBuilder.stringfy(this);
	}

	@Override
	public void strOut(StringBuilder sb) {
		sb.append(this.getLocalName());
	}

}