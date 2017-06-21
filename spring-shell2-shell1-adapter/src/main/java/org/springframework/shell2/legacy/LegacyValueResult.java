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
package org.springframework.shell2.legacy;

import org.springframework.core.MethodParameter;
import org.springframework.shell2.ValueResult;

/**
 * Legacy extension of a {@link ValueResult} that provides key index positions if available.
 * 
 * @author Camilo Gonzalez
 */
public class LegacyValueResult extends ValueResult {

	public LegacyValueResult(MethodParameter methodParameter, Object resolvedValue, Integer fromWord, Integer toWord) {
		super(methodParameter, resolvedValue, fromWord, toWord);
	}
	
}
