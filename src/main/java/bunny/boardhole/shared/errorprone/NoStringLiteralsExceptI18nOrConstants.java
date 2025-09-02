package bunny.boardhole.shared.errorprone;

import com.google.errorprone.BugPattern;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.AnnotationTreeMatcher;
import com.google.errorprone.matchers.Description;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.Tree;
import java.util.Collections;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Ensures that string literals are not used directly in validation annotations.
 * Only references to constants or {i18n.key} patterns are allowed.
 */
@BugPattern(
    summary = "Use constants or {i18n.key} patterns instead of string literals",
    severity = BugPattern.SeverityLevel.ERROR
)
public class NoStringLiteralsExceptI18nOrConstants extends BugChecker implements AnnotationTreeMatcher {

    private static final Pattern I18N_PATTERN = Pattern.compile("\\{i18n\\.[^}]+\\}");

    // Placeholder for explicitly whitelisted literal values.
    private static final Set<String> WHITELIST = Collections.emptySet();

    @Override
    public Description matchAnnotation(AnnotationTree tree, VisitorState state) {
        for (ExpressionTree argument : tree.getArguments()) {
            ExpressionTree value = argument;
            String name = "value";

            if (argument instanceof AssignmentTree assignment) {
                IdentifierTree id = (IdentifierTree) assignment.getVariable();
                name = id.getName().toString();
                value = assignment.getExpression();
            }

            // Only inspect "message" arguments commonly used in validation annotations.
            if (!"message".equals(name)) {
                continue;
            }

            if (value instanceof LiteralTree literal && literal.getKind() == Tree.Kind.STRING_LITERAL) {
                String literalValue = (String) literal.getValue();
                if (!isAllowed(literalValue)) {
                    return buildDescription(literal)
                        .setMessage("String literal must reference a constant or follow {i18n.key} pattern")
                        .build();
                }
            }
        }
        return Description.NO_MATCH;
    }

    private boolean isAllowed(String value) {
        return WHITELIST.contains(value) || I18N_PATTERN.matcher(value).matches();
    }
}
