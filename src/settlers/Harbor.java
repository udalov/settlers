package settlers;

public class Harbor {

    private final Resource resource;

    Harbor(Resource resource) {
        this.resource = resource;
    }

    public Resource resource() { return resource; }

    public String toString() {
        return resource == null ? "3:1" : resource.toString();
    }

    public int hashCode() {
        return resource == null ? 0 : resource.ordinal() + 1;
    }

    public boolean equals(Object o) {
        return o instanceof Harbor && ((Harbor)o).resource == resource;
    }
}

