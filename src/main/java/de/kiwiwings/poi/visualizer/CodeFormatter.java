package de.kiwiwings.poi.visualizer;

import javafx.css.*;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;
import org.fxmisc.richtext.model.PlainTextChange;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import org.reactfx.Subscription;

import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeFormatter {
    private static final String PATTERN_RULE = "-fx-pattern";

    private final CodeArea codeArea;
    private final List<Map.Entry<String, Pattern>> styles;

    public CodeFormatter(CodeArea codeArea) {
        this.codeArea = codeArea;
        styles = extractStyles(codeArea);

        // final URL cssUrl = CodeFormatter.class.getResource(cssFile);
        // codeArea.getStylesheets().add(cssUrl.toExternalForm());

        // add line numbers to the left of area
        codeArea.setParagraphGraphicFactory(LineNumberFactory.get(codeArea));

        // recompute the syntax highlighting 500 ms after user stops editing area
        Subscription cleanupWhenNoLongerNeedIt = codeArea

            // plain changes = ignore style changes that are emitted when syntax highlighting is reapplied
            // multi plain changes = save computation by not rerunning the code multiple times
            //   when making multiple changes (e.g. renaming a method at multiple parts in file)
            .multiPlainChanges()

            // do not emit an event until 500 ms have passed since the last emission of previous stream
            .successionEnds(Duration.ofMillis(500))

            // run the following code block when previous stream emits an event
            .subscribe(this::computeHighlighting);

        // when no longer need syntax highlighting and wish to clean up memory leaks
        // run: `cleanupWhenNoLongerNeedIt.unsubscribe();`
    }

    private void computeHighlighting(final List<PlainTextChange> changes) {
        final String text = codeArea.getText();
        if (text.isEmpty()) {
            return;
        }

        final SortedMap<Integer, Integer> startMap = new TreeMap<>();
        final SortedMap<Integer, String> colorMap = new TreeMap<>();

        // Match all regexes on this snippet, store positions
        for (Map.Entry<String, Pattern> entry : styles) {

            Matcher matcher = entry.getValue().matcher(text);

            while (matcher.find()) {
                startMap.put(matcher.start(1), matcher.end());
                colorMap.put(matcher.start(1), entry.getKey());
            }
        }

        final StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        int last = 0;

        // Colour the parts
        if (!startMap.isEmpty()) {
            final Iterator<Map.Entry<Integer, Integer>> iter = startMap.entrySet().iterator();
            Map.Entry<Integer, Integer> curr = iter.next();

            while (curr != null) {
                final Map.Entry<Integer, Integer> next = (iter.hasNext()) ? iter.next() : null;
                final int start = curr.getKey();
                final int end = Math.min(curr.getValue(), next != null ? next.getKey() : text.length());

                if (last < start) {
                    spansBuilder.add(Collections.emptyList(), start - last);
                }

                spansBuilder.add(Collections.singleton(colorMap.get(start)), end - start);

                last = end;
                curr = next;
            }
        }

        // Paint possible remaining text black
        if (last < text.length()) {
            spansBuilder.add(Collections.emptyList(), text.length() - last);
        }

        codeArea.setStyleSpans(0, spansBuilder.create());
    }

    private static List<Map.Entry<String, Pattern>> extractStyles(final CodeArea area) {
        final List<Map.Entry<String, Pattern>> styles = new ArrayList<>();

        try {
            for (final String ss : area.getStylesheets()) {
                final Stylesheet style = new CssParser().parse(new URL(ss));

                for (final Rule rule : style.getRules()) {
                    String pattern = null;
                    for (final Declaration cssProp : rule.getDeclarations()) {
                        if (PATTERN_RULE.equals(cssProp.getProperty())) {
                            pattern = cssProp.getParsedValue().getValue().toString();
                            break;
                        }
                    }
                    if (pattern == null) {
                        continue;
                    }
                    for (final Selector sel : rule.getSelectors()) {
                        final String styleClass = sel.toString().replace("*.", "");
                        styles.add(new AbstractMap.SimpleEntry<>(styleClass, Pattern.compile(pattern)));
                    }
                }
            }
        } catch (IOException e) {
        }

        return styles;
    }
}
