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
import org.springframework.shell2.CompletionProposal;
import org.springframework.shell2.ParameterDescription;
import org.springframework.shell2.ParameterResolver;
import org.springframework.shell2.Shell;
import org.springframework.shell2.Utils;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * {@link ParameterResolver} implementation that supports interactive parameters (e.g. passwords)
 * 
 * @author Camilo Gonzalez
 */
@Component
public class InteractiveParameterResolver implements ParameterResolver {

	private static final String PROMPT_SUFFIX = ":> ";

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
		ShellOption option = methodParameter.getParameterAnnotation(ShellOption.class);
		String defaultValue = null;
		if (!ShellOption.NONE.equals(option.defaultValue())) {
			defaultValue = option.defaultValue();
		}

		StringBuilder prompt = new StringBuilder();
		prompt.append(getKeysForParameter(methodParameter.getMethod(), methodParameter.getParameterIndex())
				.collect(Collectors.joining("/")));
		if (defaultValue != null) {
			prompt.append(" (");
			prompt.append(defaultValue);
			prompt.append(" )");
		}
		prompt.append(PROMPT_SUFFIX);

		String readValue = shell.readLine(prompt.toString(), isParameterMasked(methodParameter));

		if (StringUtils.isEmpty(readValue) && defaultValue != null) {
			readValue = option.defaultValue();
		}

		return conversionService.convert(readValue, TypeDescriptor.valueOf(String.class),
				new TypeDescriptor(methodParameter));
	}

	@Override
	public ParameterDescription describe(MethodParameter parameter) {
		String prefix = "--";
		ShellOption option = parameter.getParameterAnnotation(ShellOption.class);
		ParameterDescription description = ParameterDescription.outOf(parameter);
		description.mandatoryKey(false);
		description.formal("");
		description.keys(getKeysForParameter(parameter.getMethod(), parameter.getParameterIndex())
				.map(key -> prefix + key)
				.collect(Collectors.toList()));
		if (!option.defaultValue().equals(ShellOption.NONE)) {
			description.defaultValue(option.defaultValue());
		}
		description.help(option.help());
		return description;
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter methodParameter, CompletionContext context) {
		return Collections.emptyList();
	}

	private boolean isParameterMasked(MethodParameter parameter) {
		return parameter.getParameterAnnotation(ShellOption.class).masked();
	}

	private Stream<String> getKeysForParameter(Method method, int index) {
		Parameter p = method.getParameters()[index];
		ShellOption option = p.getAnnotation(ShellOption.class);
		if (option != null && option.value().length > 0) {
			return Arrays.stream(option.value());
		} else {
			return Stream.of(Utils.createMethodParameter(p).getParameterName());
		}
	}

	@Autowired // ctor injection impossible b/c of circular dependency
	public void setShell(Shell shell) {
		this.shell = shell;
	}
}
