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

package org.springframework.shell2.legacy;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.shell2.legacy.LegacyCommands.REGISTER_METHOD;

import java.util.List;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.shell.converters.BooleanConverter;
import org.springframework.shell.converters.EnumConverter;
import org.springframework.shell.converters.StringConverter;
import org.springframework.shell.core.Converter;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell2.ParameterDescription;
import org.springframework.shell2.ParameterResolver;
import org.springframework.shell2.Utils;
import org.springframework.shell2.ValueResult;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = LegacyParameterResolverTest.Config.class)
public class LegacyParameterResolverTest {

	private static final int NAME_OR_ANONYMOUS = 0;
	private static final int TYPE = 1;
	private static final int COORDINATES = 2;
	private static final int FORCE = 3;

	@Autowired
	ParameterResolver parameterResolver;

	@Rule
	public ExpectedException thrown = ExpectedException.none();

	@Test
	public void supportsParameterAnnotatedWithCliOption() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, NAME_OR_ANONYMOUS);

		boolean result = parameterResolver.supports(methodParameter);

		assertThat(result).isTrue();
	}

	@Test
	public void resolvesParameterAnnotatedWithCliOption() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, NAME_OR_ANONYMOUS);

		resolveAndAssert(methodParameter, "--foo bar --name baz --qix bux", "baz", 2, 3, false);
	}

	@Test
	public void resolvesAnonymousParameterAnnotatedWithCliOption() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, NAME_OR_ANONYMOUS);

		resolveAndAssert(methodParameter, "--foo bar baz --qix bux", "baz", 2, 2, true);

		resolveAndAssert(methodParameter, "baz --foo bar --qix bux", "baz", 0, 0, true);
	}

	@Test
	public void usesLegacyConverters() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, TYPE);

		resolveAndAssert(methodParameter, "--foo bar --name baz --qix bux --type processor", ArtifactType.processor, 6, 7, false);
	}

	@Test
	public void testUnspecifiedDefaultValue() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, FORCE);

		resolveAndAssert(methodParameter, "--foo bar --name baz --qix bux", false, -1, -1, true);
	}

	@Test
	public void testSpecifiedDefaultValue() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, FORCE);

		resolveAndAssert(methodParameter, "--force --foo bar --name baz --qix bux", true, 0, 0, false);
		resolveAndAssert(methodParameter, "--foo bar --name baz --qix bux --force", true, 6, 6, false);
	}

	@Test
	public void testParameterNotFound() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, COORDINATES);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Could not find parameter values for [--coordinates, --coords] in [--force, --foo, bar, --name, baz, --qix, bux]");
		resolve(methodParameter, "--force --foo bar --name baz --qix bux");
	}

	@Test
	public void testParameterFoundWithSameNameTooManyTimes() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, COORDINATES);

		thrown.expect(IllegalArgumentException.class);
		thrown.expectMessage("Option --coordinates has already been set");
		resolve(methodParameter, "--force --coordinates bar --coordinates baz --qix bux");
	}

	@Test
	public void testNoConverterFound() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SUM_METHOD, 0);

		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("No converter found for --v1 from '1' to type int");
		resolve(methodParameter, "--v1 1 --v2 2");
	}

	@Test
	public void testNoConverterFoundForUnspecifiedValue() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SUM_METHOD, 0);

		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("No converter found for --v1 from '38' to type int");
		resolve(methodParameter, "--v2 2");
	}

	@Test
	public void testNoConverterFoundForSpecifiedValue() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SUM_METHOD, 1);

		thrown.expect(IllegalStateException.class);
		thrown.expectMessage("No converter found for --v2 from '42' to type int");
		resolve(methodParameter, "--v1 1 --v2");
	}
	
	@Test
	public void testDescribeBothDefaultsNotDeclared() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.REGISTER_METHOD, 1);
		
		ParameterDescription description = parameterResolver.describe(methodParameter).findFirst().get();
		
		assertThat(description.keys()).containsExactly("--type");
		assertThat(description.formal()).isEqualTo(Utils.unCamelify(ArtifactType.class.getSimpleName()));
		assertThat(description.defaultValue().isPresent()).isFalse();
		assertThat(description.mandatoryKey()).isTrue();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}
	
	@Test
	public void testDescribeBothDefaultsDeclared() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SOME_METHOD, 1);
		
		ParameterDescription description = parameterResolver.describe(methodParameter).findFirst().get();
		
		assertThat(description.keys()).containsExactly("--option");
		assertThat(description.formal()).isEqualTo(boolean.class.getName());
		assertThat(description.defaultValue().get()).isEqualTo("false");
		assertThat(description.defaultValueWhenFlag().get()).isEqualTo("true");
		assertThat(description.mandatoryKey()).isTrue();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}
	
	@Test
	public void testDescribeOnlySpecifiedDefaultDeclared() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SUM_METHOD, 1);
		
		ParameterDescription description = parameterResolver.describe(methodParameter).findFirst().get();
		
		assertThat(description.keys()).containsExactly("--v2");
		assertThat(description.formal()).isEqualTo(int.class.getName());
		assertThat(description.defaultValue().get()).isEqualTo("null");
		assertThat(description.defaultValueWhenFlag().get()).isEqualTo("42");
		assertThat(description.mandatoryKey()).isTrue();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}
	
	@Test
	public void testDescribeOnlyUnspecifiedDefaultDeclared() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SUM_METHOD, 0);
		
		ParameterDescription description = parameterResolver.describe(methodParameter).findFirst().get();
		
		assertThat(description.keys()).containsExactly("--v1");
		assertThat(description.formal()).isEqualTo(int.class.getName());
		assertThat(description.defaultValue().get()).isEqualTo("38");
		assertThat(description.mandatoryKey()).isTrue();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}
	
	@Test
	public void testDescribeDefaultKey() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.LEGACY_ECHO_METHOD, 0);
		
		ParameterDescription description = parameterResolver.describe(methodParameter).findFirst().get();
		
		assertThat(description.keys()).isEmpty();
		assertThat(description.formal()).isEqualTo(Utils.unCamelify(String.class.getSimpleName()));
		assertThat(description.defaultValue().isPresent()).isFalse();
		assertThat(description.mandatoryKey()).isFalse();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}
	
	@Test
	public void testDescribeNonMandatoryNoDefaults() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SOME_METHOD, 0);
		
		ParameterDescription description = parameterResolver.describe(methodParameter).findFirst().get();
		
		assertThat(description.keys()).containsExactly("--key");
		assertThat(description.formal()).isEqualTo(Utils.unCamelify(String.class.getSimpleName()));
		assertThat(description.defaultValue().get()).isEqualTo("null");
		assertThat(description.mandatoryKey()).isTrue();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}

	private ValueResult resolveAndAssert(MethodParameter methodParameter, String command, Object expectedValue, int firstWordIndex, int lastWordIndex,
			boolean wordsForValuesIsSameAsWords) {
		List<String> words = asList(command.split(" "));
		ValueResult result = parameterResolver.resolve(methodParameter, words);
		assertThat(result.resolvedValue()).isEqualTo(expectedValue);
		assertThat(result.wordsUsed().nextSetBit(0)).isEqualTo(firstWordIndex);
		assertThat(result.wordsUsed().previousSetBit(Integer.MAX_VALUE)).isEqualTo(lastWordIndex);
		assertThat(result.wordsUsed().equals(result.wordsUsedForValue())).isEqualTo(wordsForValuesIsSameAsWords);
		if (!result.wordsUsedForValue().isEmpty()) {
			assertThat(result.wordsUsedForValue(words)).containsExactly(expectedValue.toString());
		}
		return result;
	}
	
	private Object resolve(MethodParameter methodParameter, String command) {
		ValueResult result = parameterResolver.resolve(methodParameter, asList(command.split(" ")));
		return result.resolvedValue();
	}

	@Configuration
	static class Config {

		@Bean
		public Converter<String> stringConverter() {
			return new StringConverter();
		}

		@Bean
		public Converter<Boolean> booleanConverter() {
			return new BooleanConverter();
		}

		@Bean
		public Converter<Enum<?>> enumConverter() {
			return new EnumConverter();
		}

		@Bean
		public ParameterResolver parameterResolver() {
			return new LegacyParameterResolver();
		}
	}
}
