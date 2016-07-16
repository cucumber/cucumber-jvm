package cucumber.runtime;

import gherkin.pickles.PickleTag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TagExpression {
    private final Map<String, Integer> limits = new HashMap<String, Integer>();
    private And and = new And();

    public TagExpression(List<String> tagExpressions) {
        for (String tagExpression : tagExpressions) {
            add(tagExpression.split("\\s*,\\s*"));
        }
    }

    public boolean evaluate(Collection<PickleTag> tags) {
        return and.isEmpty() || and.eval(tags);
    }

    public Map<String, Integer> limits() {
        return limits;
    }

    public boolean isEmpty() {
        return and.isEmpty();
    }

    private void add(String[] tags) {
        Or or = new Or();
        for (String tag : tags) {
            boolean negation;
            tag = tag.trim();
            if (tag.startsWith("~")) {
                tag = tag.substring(1);
                negation = true;
            } else {
                negation = false;
            }
            String[] tagAndLimit = tag.split(":");
            if (tagAndLimit.length == 2) {
                tag = tagAndLimit[0];
                int limit = Integer.parseInt(tagAndLimit[1]);
                if (limits.containsKey(tag) && limits.get(tag) != limit) {
                    throw new BadTagLimitException(tag, limits.get(tag), limit);
                }
                limits.put(tag, limit);
            }

            if (negation) {
                or.add(new Not(new TagExp(tag)));
            } else {
                or.add(new TagExp(tag));
            }
        }
        and.add(or);
    }

    private interface Expression {
        boolean eval(Collection<PickleTag> tags);
    }

    private class Not implements Expression {
        private final Expression expression;

        public Not(Expression expression) {
            this.expression = expression;
        }

        public boolean eval(Collection<PickleTag> tags) {
            return !expression.eval(tags);
        }
    }

    private class And implements Expression {
        private List<Expression> expressions = new ArrayList<Expression>();

        public void add(Expression expression) {
            expressions.add(expression);
        }

        public boolean eval(Collection<PickleTag> tags) {
            boolean result = true;
            for (Expression expression : expressions) {
                result = expression.eval(tags);
                if (!result) break;
            }
            return result;
        }

        public boolean isEmpty() {
            return expressions.isEmpty();
        }
    }

    private class Or implements Expression {
        private List<Expression> expressions = new ArrayList<Expression>();

        public void add(Expression expression) {
            expressions.add(expression);
        }

        public boolean eval(Collection<PickleTag> tags) {
            boolean result = false;
            for (Expression expression : expressions) {
                result = expression.eval(tags);
                if (result) break;
            }
            return result;
        }
    }

    private class TagExp implements Expression {
        private final String tagName;

        public TagExp(String tagName) {
            if (!tagName.startsWith("@")) {
                throw new BadTagException(tagName);
            }
            this.tagName = tagName;
        }

        public boolean eval(Collection<PickleTag> tags) {
            for (PickleTag tag : tags) {
                if (tagName.equals(tag.getName())) {
                    return true;
                }
            }
            return false;
        }
    }

    private class BadTagException extends RuntimeException {
        public BadTagException(String tagName) {
            super("Bad tag: \"" + tagName + "\"");
        }
    }

    private class BadTagLimitException extends RuntimeException {
        public BadTagLimitException(String tag, int limitA, int limitB) {
            super("Inconsistent tag limits for " + tag + ": " + limitA + " and " + limitB);
        }
    }
}
