package io.cucumber.plugin.event;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.junit.jupiter.api.Assertions.assertEquals;

class NodeTest {

    private final Node.Example example1 = new Node.Example() {
        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());
        }

        @Override
        public String toString() {
            return "Example #1";
        }
    };

    private final Node.Example example2 = new Node.Example() {
        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());
        }

        @Override
        public String toString() {
            return "Example #2";
        }
    };
    private final Node.Example example3 = new Node.Example() {
        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());
        }

        @Override
        public String toString() {
            return "Example #3";
        }
    };

    private final Node.Example example4 = new Node.Example() {
        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());
        }

        @Override
        public String toString() {
            return "Example #4";
        }
    };

    private final Node.Examples examplesA = new Node.Examples() {
        @Override
        public Collection<Example> elements() {
            return asList(example1, example2);
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());

        }

        @Override
        public String toString() {
            return "Examples A";
        }
    };
    private final Node.Examples examplesB = new Node.Examples() {
        @Override
        public Collection<Example> elements() {
            return asList(example3, example4);
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());

        }

        @Override
        public String toString() {
            return "Examples B";
        }
    };

    private final Node.Examples emptyExamplesA = new Node.Examples() {
        @Override
        public Collection<Example> elements() {
            return Collections.emptyList();
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());

        }

        @Override
        public String toString() {
            return "Empty Examples A";
        }
    };

    private final Node.Examples emptyExamplesB = new Node.Examples() {
        @Override
        public Collection<Example> elements() {
            return Collections.emptyList();
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());

        }

        @Override
        public String toString() {
            return "Empty Examples B";
        }
    };

    private final Node.ScenarioOutline outline = new Node.ScenarioOutline() {
        @Override
        public Collection<Examples> elements() {
            return asList(examplesA, examplesB);
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());

        }

        @Override
        public String toString() {
            return "Outline";
        }
    };

    private final Node.ScenarioOutline emptyOutline = new Node.ScenarioOutline() {
        @Override
        public Collection<Examples> elements() {
            return asList(emptyExamplesA, emptyExamplesB);
        }

        @Override
        public Location getLocation() {
            return null;
        }

        @Override
        public Optional<String> getKeyword() {
            return Optional.empty();
        }

        @Override
        public Optional<String> getName() {
            return Optional.of(toString());

        }

        @Override
        public String toString() {
            return "Empty Outline";
        }
    };

    @Test
    void findExamples1() {
        Optional<List<Node>> pathTo = outline.findPathTo(node -> Optional.of("Example #1").equals(node.getName()));
        assertEquals(Optional.of(asList(outline, examplesA, example1)), pathTo);
    }

    @Test
    void findExamples2() {
        Optional<List<Node>> pathTo = outline.findPathTo(node -> Optional.of("Example #2").equals(node.getName()));
        assertEquals(Optional.of(asList(outline, examplesA, example2)), pathTo);
    }

    @Test
    void findExamples3() {
        Optional<List<Node>> pathTo = outline.findPathTo(node -> Optional.of("Example #3").equals(node.getName()));
        assertEquals(Optional.of(asList(outline, examplesB, example3)), pathTo);
    }

    @Test
    void findExamples4() {
        Optional<List<Node>> pathTo = outline.findPathTo(node -> Optional.of("Example #4").equals(node.getName()));
        assertEquals(Optional.of(asList(outline, examplesB, example4)), pathTo);
    }

    @Test
    void findExamplesA() {
        Optional<List<Node>> pathTo = outline.findPathTo(node -> Optional.of("Examples A").equals(node.getName()));
        assertEquals(Optional.of(asList(outline, examplesA)), pathTo);
    }

    @Test
    void findEmptyExamplesA() {
        Optional<List<Node>> pathTo = emptyOutline
                .findPathTo(node -> Optional.of("Empty Examples A").equals(node.getName()));
        assertEquals(Optional.of(asList(emptyOutline, emptyExamplesA)), pathTo);
    }

    @Test
    void findExamplesB() {
        Optional<List<Node>> pathTo = outline.findPathTo(node -> Optional.of("Examples B").equals(node.getName()));
        assertEquals(Optional.of(asList(outline, examplesB)), pathTo);
    }

    @Test
    void findEmptyExamplesB() {
        Optional<List<Node>> pathTo = emptyOutline
                .findPathTo(node -> Optional.of("Empty Examples B").equals(node.getName()));
        assertEquals(Optional.of(asList(emptyOutline, emptyExamplesB)), pathTo);
    }

    @Test
    void findOutline() {
        Optional<List<Node>> pathTo = outline.findPathTo(node -> Optional.of("Outline").equals(node.getName()));
        assertEquals(Optional.of(asList(outline)), pathTo);
    }

    @Test
    void findEmptyOutline() {
        Optional<List<Node>> pathTo = emptyOutline
                .findPathTo(node -> Optional.of("Empty Outline").equals(node.getName()));
        assertEquals(Optional.of(asList(emptyOutline)), pathTo);
    }

    @Test
    void findNothingInOutline() {
        Optional<List<Node>> pathTo = outline.findPathTo(node -> Optional.of("Nothing").equals(node.getName()));
        assertEquals(Optional.empty(), pathTo);
    }

    @Test
    void findNothingInEmptyOutline() {
        Optional<List<Node>> pathTo = emptyOutline.findPathTo(node -> Optional.of("Nothing").equals(node.getName()));
        assertEquals(Optional.empty(), pathTo);
    }

    @Test
    void findInNode() {
        Optional<List<Node>> pathTo = example1.findPathTo(node -> Optional.of("Example #1").equals(node.getName()));
        assertEquals(Optional.of(asList(example1)), pathTo);
    }

    @Test
    void findNothingInNode() {
        Optional<List<Node>> pathTo = example1.findPathTo(node -> Optional.of("Nothing").equals(node.getName()));
        assertEquals(Optional.empty(), pathTo);
    }

}
