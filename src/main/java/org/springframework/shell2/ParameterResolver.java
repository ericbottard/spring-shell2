/*
 * Copyright 2015 the original author or authors.
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

import java.util.List;

import org.springframework.core.MethodParameter;

/**
 * Created by ericbottard on 27/11/15.
 */
public interface ParameterResolver {

	boolean supports(MethodParameter parameter);

	Object resolve(MethodParameter methodParameter, List<String> words);

	ParameterDescription describe(MethodParameter parameter);

	List<String> complete(MethodParameter parameter, List<String> words);

}
