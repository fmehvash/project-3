package simplf; 

class Environment {
    private final Environment enclosing;
    private AssocList values;

    Environment() {
        //throw new UnsupportedOperationException("TODO: implement environments.");
        this.values = null; // Start with an empty list
        this.enclosing = null;
    }

    Environment(Environment enclosing) {
        //throw new UnsupportedOperationException("TODO: implement environments.");
        this.values = null; // Start with an empty list
        this.enclosing = enclosing;
    }

    Environment(AssocList assocList, Environment enclosing) {
        //throw new UnsupportedOperationException("TODO: implement environments.");
        this.values = assocList;
        this.enclosing = enclosing;
    }

    // Return a new version of the environment that defines the variable "name"
    // and sets its initial value to "value". Take care to ensure the proper aliasing
    // relationship. There is an association list implementation in Assoclist.java.
    // If your "define" function adds a new entry to the association list without
    // modifying the previous list, this should yield the correct aliasing
    // relationsip.
    //
    // For example, if the original environment has the association list
    // [{name: "x", value: 1}, {name: "y", value: 2}]
    // the new environment after calling define(..., "z", 3) should have the following
    //  association list:
    // [{name: "z", value: 3}, {name: "x", value: 1}, {name: "y", value: 2}]
    // This should be constructed by building a new class of type AssocList whose "next"
    // reference is the previous AssocList.
    public void define(Token name, Object value) {
        values = new AssocList(name.lexeme, value, values);
    }

    void assign(Token name, Object value) {
        //throw new UnsupportedOperationException("TODO: implement environments.");
        if (containsKey(name.lexeme)) {
            update(name.lexeme, value);
        } else if (enclosing != null) {
            enclosing.assign(name, value);
        } else {
            throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
        }
    }

    Object get(Token name) {
        //throw new UnsupportedOperationException("TODO: implement environments.");
        AssocList current = values;
        while (current != null) {
            if (current.name.equals(name.lexeme)) {
                return current.value;
            }
            current = current.next;
        }
        if (enclosing != null) return enclosing.get(name);

        throw new RuntimeError(name, "Undefined variable '" + name.lexeme + "'.");
    }

    private boolean containsKey(String name) {
        AssocList current = values;
        while (current != null) {
            if (current.name.equals(name)) {
                return true;
            }
            current = current.next;
        }
        return false;
    }

    private void update(String name, Object value) {
        AssocList current = values;
        while (current != null) {
            if (current.name.equals(name)) {
                current.value = value;
                return;
            }
            current = current.next;
        }
    }
}

