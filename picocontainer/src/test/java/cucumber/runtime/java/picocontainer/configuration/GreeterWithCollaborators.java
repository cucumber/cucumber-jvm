package cucumber.runtime.java.picocontainer.configuration;


public class GreeterWithCollaborators implements GreeterInterface {

    private GreeterCollaborator greeterCollaborator;

    public GreeterWithCollaborators(GreeterCollaborator greeterCollaborator) {
        this.greeterCollaborator = greeterCollaborator;
    }

    @Override
    public String greet() {
        if (greeterCollaborator == null) {
            throw new IllegalStateException("Collaborator was null");
        }
        return "Complex Greeting";
    }
}