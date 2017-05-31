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
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.Highlighter;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Shell implementation using JLine to capture input and trigger completions.
 *
 * @author Eric Bottard
 * @author Florent Biville
 */
@Component
public class JLineShell extends AbstractShell {

	LineReader lineReader;

	@Autowired
	private Terminal terminal;

	@PostConstruct
	public void init() throws Exception {
		ExtendedDefaultParser parser = new ExtendedDefaultParser();
		parser.setEofOnUnclosedQuote(true);
		parser.setEofOnEscapedNewLine(true);

		LineReaderBuilder lineReaderBuilder = LineReaderBuilder.builder()
				.terminal(terminal)
				.appName("Foo")
				.completer(new CompleterAdapter())
				.highlighter(new Highlighter() {

					@Override
					public AttributedString highlight(LineReader reader, String buffer) {
						int l = 0;
						String best = null;
						for (String command : methodTargets.keySet()) {
							if (buffer.startsWith(command) && command.length() > l) {
								l = command.length();
								best = command;
							}
						}
						if (best != null) {
							return new AttributedStringBuilder(buffer.length()).append(best, AttributedStyle.BOLD).append(buffer.substring(l)).toAttributedString();
						}
						else {
							return new AttributedString(buffer, AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
						}
					}
				})
				.parser(parser);

		lineReader = lineReaderBuilder.build();

	}


	@Override
	protected Input readInput() {
		try {
			lineReader.readLine(new AttributedString("shell:>", AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW)).toAnsi(terminal));
		}
		catch (UserInterruptException e) {
			if (e.getPartialLine().isEmpty()) {
				resultHandler.handleResult(new ExitRequest(1));
			} else {
				return Input.EMPTY;
			}
		}
		return new JLineInput(lineReader.getParsedLine());
	}

	@Override
	public String readLine(String prompt, boolean masked) {
		try {
			return lineReader.readLine(prompt, masked == true ? '*' : null);
		} catch (UserInterruptException ex) {
			return null;
		}
	}
	
	/**
	 * Sanitize the buffer input given the customizations applied to the JLine parser (<em>e.g.</em> support for
	 * line continuations, <em>etc.</em>)
	 */
	static private List<String> sanitizeInput(List<String> words) {
		words = words.stream()
			.map(s -> s.replaceAll("^\\n+|\\n+$", "")) // CR at beginning/end of line introduced by backslash continuation
			.map(s -> s.replaceAll("\\n+", " ")) // CR in middle of word introduced by return inside a quoted string
			.filter(w -> w.length() > 0) // Empty word introduced when using quotes, no idea why...
			.collect(Collectors.toList());
		return words;
	}

	// Overridden so it can be called from CompleterAdapter


	@Override
	public List<CompletionProposal> complete(CompletionContext context) {
		return super.complete(context);
	}

	/**
	 * A bridge between JLine's {@link Completer} contract and our own.
	 * @author Eric Bottard
	 */
	private class CompleterAdapter implements Completer {

		@Override
		public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
			CompletingParsedLine cpl = (line instanceof CompletingParsedLine) ? ((CompletingParsedLine) line) : t -> t;

			CompletionContext context = new CompletionContext(sanitizeInput(line.words()), line.wordIndex(), line.wordCursor());

			List<CompletionProposal> proposals = JLineShell.this.complete(context);
			proposals.stream()
				.map(p -> new Candidate(
					cpl.emit(p.value()).toString(),
					p.displayText(),
					p.category(),
					p.description(),
					null,
					null,
					true)
				)
				.forEach(candidates::add);
		}
	}

	private static class JLineInput implements Input {

		private final ParsedLine parsedLine;

		JLineInput(ParsedLine parsedLine) {
			this.parsedLine = parsedLine;
		}

		@Override
		public String rawText() {
			return parsedLine.line();
		}

		@Override
		public List<String> words() {
			return sanitizeInput(parsedLine.words());
		}
	}

}

