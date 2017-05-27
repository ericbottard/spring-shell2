/*
 * Copyright 2015-2017 the original author or authors.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell2.CompletionContext;
import org.springframework.shell2.CompletionProposal;
import org.springframework.shell2.ParameterDescription;
import org.springframework.shell2.ParameterResolver;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

/**
 * Resolves parameters by looking at the {@link CliOption} annotation and acting accordingly.
 *
 * @author Eric Bottard
 * @author Camilo Gonzalez
 */
@Component
public class LegacyParameterResolver implements ParameterResolver {

	private static final String CLI_OPTION_NULL = "__NULL__";
	
	/**
	 * Prefix used by Spring Shell 1 for the argument keys (<em>e.g.</em> command --key value).
	 */
	private static final String CLI_PREFIX = "--";
	
	@Autowired(required = false)
	private Collection<Converter<?>> converters = new ArrayList<>();

	@Override
	public boolean supports(MethodParameter parameter) {
		return parameter.hasParameterAnnotation(CliOption.class);
	}

	@Override
	public Object resolve(MethodParameter methodParameter, List<String> words) {
		CliOption cliOption = methodParameter.getParameterAnnotation(CliOption.class);
		Optional<Converter<?>> converter = converters.stream()
				.filter(c -> c.supports(methodParameter.getParameterType(), cliOption.optionContext()))
				.findFirst();

		Map<String, String> values = parseOptions(words);
		Map<String, Object> seenValues = convertValues(values, methodParameter, converter);
		switch (seenValues.size()) {
			case 0:
				if (!cliOption.mandatory()) {
					String value = cliOption.unspecifiedDefaultValue();
					return converter
							.orElseThrow(noConverterFound(cliOption.key()[0], value, methodParameter.getParameterType()))
							.convertFromText(value, methodParameter.getParameterType(), cliOption.optionContext());
				}
				else {
					throw new IllegalArgumentException("Could not find parameter values for " + prettifyKeys(Arrays.asList(cliOption.key())) + " in " + words);
				}
			case 1:
				return seenValues.values().iterator().next();
			default:
				throw new RuntimeException("Option has been set multiple times via " + prettifyKeys(seenValues.keySet()));
		}
	}

	@Override
	public ParameterDescription describe(MethodParameter parameter) {
		CliOption option = parameter.getParameterAnnotation(CliOption.class);
		ParameterDescription result = ParameterDescription.outOf(parameter);
		result.help(option.help());
		List<String> keys = Arrays.asList(option.key());
		result.keys(keys.stream()
				.filter(key -> !key.isEmpty())
				.map(key -> CLI_PREFIX + key)
				.collect(Collectors.toList()));
		if (!option.mandatory()) {
			result.defaultValue(CLI_OPTION_NULL.equals(option.unspecifiedDefaultValue()) ? "null" : option.unspecifiedDefaultValue());
		}
		if(!CLI_OPTION_NULL.equals(option.specifiedDefaultValue())) {
			result.whenFlag(option.specifiedDefaultValue());
		}
		boolean containsEmptyKey = keys.contains("");
		result.mandatoryKey(!containsEmptyKey);
		return result;
	}

	@Override
	public List<CompletionProposal> complete(MethodParameter parameter, CompletionContext context) {
		// DevNote: in Legacy the user is either completing an argument (-- prefix) or the value of the argument (e.g. no arity to consider).
		// Other cases to be considered include default keys and default values
		
		CliOption option = parameter.getParameterAnnotation(CliOption.class);
		List<String> keys = Arrays.asList(option.key());
		
		int currentWordIndex = context.getWordIndex();		
		
		// 1) has a key for this parameter already been defined?
		boolean keyAlreadyDefined = context.getWords().stream()
				.filter(word -> word.startsWith(CLI_PREFIX) && keys.contains(word.substring(CLI_PREFIX.length())))
				.map(word -> true)
				.findFirst().orElse(false);
		
		if (keyAlreadyDefined) {
			// if a key has already been provided for this parameter, then there is no need to provide completion
			// proposals
			return Collections.emptyList();
		}
		
		// 2) is the user currently completing an argument key?
		String currentWordUpToCursor = context.currentWordUpToCursor();
		if (currentWordUpToCursor != null && currentWordUpToCursor.startsWith(CLI_PREFIX)) {
			// return the keys of this parameter that aren't empty and match the current key being completed
			return keys.stream()
					.filter(key -> !key.isEmpty() && (CLI_PREFIX + key).startsWith(currentWordUpToCursor))
					.map(key -> completionProposalFor(parameter, option, key))
					.collect(Collectors.toList());
		}
		
		// 3) is the previous word an argument key?
		boolean isPreviousWordArgumentKey = currentWordIndex > 0 && context.getWords().get(currentWordIndex-1).startsWith(CLI_PREFIX);
		
		if(isPreviousWordArgumentKey) {
			// in Shell 1 we don't provide value completions, and if the previous word is a key then
			// we shouldn't provide any completions if there are no default values for that option
			return Collections.emptyList();
		}
		
		// 4) if none of the above, then return all non-empty keys as CompletionProposals
		return keys.stream()
				.filter(key -> !key.isEmpty())
				.map(key -> completionProposalFor(parameter, option, key))
				.collect(Collectors.toList());
	}

	private CompletionProposal completionProposalFor(MethodParameter parameter, CliOption option, String key) {
		CompletionProposal completionProposal = new CompletionProposal(CLI_PREFIX + key);
		completionProposal.category(parameter.getParameterName());
		completionProposal.description(option.help());
		return completionProposal;
	}

	private Map<String, String> parseOptions(List<String> words) {
		Map<String, String> values = new HashMap<>();
		for (int i = 0; i < words.size(); i++) {
			String word = words.get(i);
			if (word.startsWith("--")) {
				String key = word.substring(CLI_PREFIX.length());
				// If next word doesn't exist or starts with '--', this is an unary option. Store null
				String value = i < words.size() - 1 && !words.get(i + 1).startsWith(CLI_PREFIX) ? words.get(++i) : null;
				Assert.isTrue(!values.containsKey(key), String.format("Option --%s has already been set", key));
				values.put(key, value);
			} // Must be the 'anonymous' option
			else {
				Assert.isTrue(!values.containsKey(""), "Anonymous option has already been set");
				values.put("", word);
			}
		}
		return values;
	}

	private Map<String, Object> convertValues(Map<String, String> values, MethodParameter methodParameter, Optional<Converter<?>> converter) {
		Map<String, Object> seenValues = new HashMap<>();
		CliOption option = methodParameter.getParameterAnnotation(CliOption.class);
		for (String key : option.key()) {
			if (values.containsKey(key)) {
				String value = values.get(key);
				if (value == null && !CLI_OPTION_NULL.equals(option.specifiedDefaultValue())) {
					value = option.specifiedDefaultValue();
				}
				Class<?> parameterType = methodParameter.getParameterType();
				seenValues.put(key, converter
						.orElseThrow(noConverterFound(key, value, parameterType))
						.convertFromText(value, parameterType, option.optionContext()));
			}
		}
		return seenValues;
	}

	/**
	 * Return the list of possible keys for an option, suitable for displaying in an error message.
	 */
	private String prettifyKeys(Collection<String> keys) {
		return keys.stream().map(s -> "".equals(s) ? "<anonymous>" : "--" + s).collect(Collectors.joining(", ", "[", "]"));
	}

	private Supplier<IllegalStateException> noConverterFound(String key, String value, Class<?> parameterType) {
		return () -> new IllegalStateException("No converter found for --" + key + " from '" + value + "' to type " + parameterType);
	}
	
}
