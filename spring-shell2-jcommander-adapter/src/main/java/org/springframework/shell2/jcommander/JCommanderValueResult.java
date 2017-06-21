/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.shell2.jcommander;

import org.springframework.core.MethodParameter;
import org.springframework.shell2.ValueResult;

/**
 * JCommander extension of a {@link ValueResult}.
 * 
 * @author Camilo Gonzalez
 */
public class JCommanderValueResult extends ValueResult {

	public JCommanderValueResult(MethodParameter methodParameter, Object resolvedValue) {
		super(methodParameter, resolvedValue, null, null);
	}

	@Override
	public Integer firstWordUsed() {
		throw new UnsupportedOperationException();
	}

	@Override
	public Integer lastWordUsed() {
		throw new UnsupportedOperationException();
	}

}
