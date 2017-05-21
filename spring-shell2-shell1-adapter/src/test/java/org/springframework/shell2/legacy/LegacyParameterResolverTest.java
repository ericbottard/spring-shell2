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

		Object result = resolve(methodParameter, "--foo bar --name baz --qix bux");

		assertThat(result).isEqualTo("baz");
	}

	@Test
	public void resolvesAnonymousParameterAnnotatedWithCliOption() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, NAME_OR_ANONYMOUS);

		Object result = resolve(methodParameter, "--foo bar baz --qix bux");
		assertThat(result).isEqualTo("baz");

		// As first param
		result = resolve(methodParameter, "baz --foo bar --qix bux");
		assertThat(result).isEqualTo("baz");
	}

	@Test
	public void usesLegacyConverters() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, TYPE);

		Object result = resolve(methodParameter, "--foo bar --name baz --qix bux --type processor");

		assertThat(result).isSameAs(ArtifactType.processor);
	}

	@Test
	public void testUnspecifiedDefaultValue() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, FORCE);

		Object result = resolve(methodParameter, "--foo bar --name baz --qix bux");

		assertThat(result).isEqualTo(false);
	}

	@Test
	public void testSpecifiedDefaultValue() throws Exception {
		MethodParameter methodParameter = Utils.createMethodParameter(REGISTER_METHOD, FORCE);

		assertThat(resolve(methodParameter, "--force --foo bar --name baz --qix bux")).isEqualTo(true);
		assertThat(resolve(methodParameter, "--foo bar --name baz --qix bux --force")).isEqualTo(true);
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
		
		ParameterDescription description = parameterResolver.describe(methodParameter);
		
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
		
		ParameterDescription description = parameterResolver.describe(methodParameter);
		
		assertThat(description.keys()).containsExactly("--option");
		assertThat(description.formal()).isEqualTo(boolean.class.getName());
		assertThat(description.defaultValue().get()).isEqualTo("false, or true if used as --option");
		assertThat(description.mandatoryKey()).isTrue();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}
	
	@Test
	public void testDescribeOnlySpecifiedDefaultDeclared() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SUM_METHOD, 1);
		
		ParameterDescription description = parameterResolver.describe(methodParameter);
		
		assertThat(description.keys()).containsExactly("--v2");
		assertThat(description.formal()).isEqualTo(int.class.getName());
		assertThat(description.defaultValue().get()).isEqualTo("42 if used as --v2");
		assertThat(description.mandatoryKey()).isFalse();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}
	
	@Test
	public void testDescribeOnlyUnspecifiedDefaultDeclared() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.SUM_METHOD, 0);
		
		ParameterDescription description = parameterResolver.describe(methodParameter);
		
		assertThat(description.keys()).containsExactly("--v1");
		assertThat(description.formal()).isEqualTo(int.class.getName());
		assertThat(description.defaultValue().get()).isEqualTo("38");
		assertThat(description.mandatoryKey()).isFalse();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}
	
	@Test
	public void testDescribeDefaultKey() {
		MethodParameter methodParameter = Utils.createMethodParameter(LegacyCommands.LEGACY_ECHO_METHOD, 0);
		
		ParameterDescription description = parameterResolver.describe(methodParameter);
		
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
		
		ParameterDescription description = parameterResolver.describe(methodParameter);
		
		assertThat(description.keys()).containsExactly("--key");
		assertThat(description.formal()).isEqualTo(Utils.unCamelify(String.class.getSimpleName()));
		assertThat(description.defaultValue().get()).isEqualTo("null");
		assertThat(description.mandatoryKey()).isFalse();
		
		String expectedHelp = methodParameter.getParameterAnnotation(CliOption.class).help();
		assertThat(description.help()).isEqualTo(expectedHelp);
	}

	private Object resolve(MethodParameter methodParameter, String command) {
		return parameterResolver.resolve(methodParameter, asList(command.split(" ")));
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
