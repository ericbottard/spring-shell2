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

package org.springframework.shell2.standard;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.shell2.CompletionContext;
import org.springframework.shell2.ParameterDescription;
import org.springframework.shell2.ParameterResolver;
import org.springframework.shell2.Shell;
import org.springframework.shell2.Utils;
import org.springframework.stereotype.Component;

/**
 * {@link ParameterResolver} implementation that supports interactive parameters (e.g. passwords)
 * 
 * @author Camilo Gonzalez
 */
@Component
public class InteractiveParameterResolver implements ParameterResolver {

	private final ConversionService conversionService;
	
	private Shell shell;

	@Autowired
	public InteractiveParameterResolver(ConversionService conversionService) {
		this.conversionService = conversionService;
	}
	
	@Override
	public boolean supports(MethodParameter parameter) {
		if (!parameter.hasParameterAnnotation(ShellOption.class)) {
			return false;
		}

		// this resolver only supports interactive parameters
		if (parameter.getParameterAnnotation(ShellOption.class).interactive()) {
			return true;
		}
		return false;
	}

	@Override
	public Object resolve(MethodParameter methodParameter, List<String> words) {
		final String prompt = getKeysForParameter(methodParameter.getMethod(), methodParameter.getParameterIndex())
				.collect(Collectors.joining("/")) + ":> ";

		// TODO workaround to debug in IDEs (JLine returns an empty string every 2nd shell.readLine inside console IDEs
		// - potentially a bug)
		String readValue;
		while ((readValue = shell.readLine(prompt, isParameterMasked(methodParameter))).isEmpty()) {
		}

		if (ShellOption.NULL.equals(readValue)) {
			return null;
		} else {
			return conversionService.convert(readValue, TypeDescriptor.valueOf(String.class),
					new TypeDescriptor(methodParameter));
		}
	}

	private boolean isParameterMasked(MethodParameter parameter) {
		return parameter.getParameterAnnotation(ShellOption.class).masked();
	}
	
	private Stream<String> getKeysForParameter(Method method, int index) {
		Parameter p = method.getParameters()[index];
		ShellOption option = p.getAnnotation(ShellOption.class);
		if (option != null && option.value().length > 0) {
			return Arrays.stream(option.value());
		}
		else {
			return Stream.of(Utils.createMethodParameter(p).getParameterName());
		}
	}

	@Override
	public ParameterDescription describe(MethodParameter parameter) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<String> complete(MethodParameter methodParameter, CompletionContext context) {
		return Collections.emptyList();
	}
	
	@Autowired // ctor injection impossible b/c of circular dependency
	public void setShell(Shell shell) {
		this.shell = shell;
	}

}
