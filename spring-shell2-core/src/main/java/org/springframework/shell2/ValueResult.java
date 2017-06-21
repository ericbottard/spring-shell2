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
package org.springframework.shell2;

import javax.naming.spi.ResolveResult;

import org.springframework.core.MethodParameter;

/**
 * A {@link ResolveResult} for a successful {@link ParameterResolver#resolve} operation.
 * 
 * @author Camilo Gonzalez
 */
public class ValueResult {

	private final MethodParameter methodParameter;

	private final Object resolvedValue;

	private final Integer fromWord;

	private final Integer toWord;

	public ValueResult(MethodParameter methodParameter, Object resolvedValue, Integer fromWord, Integer toWord) {
		this.methodParameter = methodParameter;
		this.resolvedValue = resolvedValue;
		this.fromWord = fromWord;
		this.toWord = toWord;
	}

	/**
	 * The {@link MethodParameter} that was the target of the {@link ParameterResolver#resolve}
	 * operation.
	 */
	public MethodParameter methodParameter() {
		return methodParameter;
	}

	/**
	 * Represents the resolved value for the {@link MethodParameter} associated with this result.
	 */
	public Object resolvedValue() {
		return resolvedValue;
	}

	/**
	 * The index of the first input word used by the {@link ParameterResolver}. null indicates that
	 * no words were used (e.g. default values).
	 */
	public Integer firstWordUsed() {
		return fromWord;
	}

	/**
	 * The index of the last input word used by the {@link ParameterResolver}. null indicates that
	 * no words were used (e.g. default values).
	 */
	public Integer lastWordUsed() {
		return toWord;
	}

}
