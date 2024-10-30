package simplf;
import simplf.RuntimeError;
import java.util.List;

class SimplfFunction implements SimplfCallable {
    private final Stmt.Function declaration;
    private Environment closure;

    SimplfFunction(Stmt.Function declaration, Environment closure) {
        //throw new UnsupportedOperationException("TODO: implement functions");
        this.declaration = declaration;
        this.closure = closure;
    }

    public void setClosure(Environment environment) {
        //throw new UnsupportedOperationException("TODO: implement functions");
        this.closure = environment;
    }

    @Override
    public Object call(Interpreter interpreter, List<Object> arguments) {
        Environment environment = new Environment(closure);
        for (int i = 0; i < declaration.params.size(); i++) {
            environment.define(declaration.params.get(i), arguments.get(i));
        }

        // Call `callFunction` to evaluate the function body and return the last expression
        return interpreter.callFunction(declaration, environment);
    }

    @Override
    public String toString() {
        return "<fn " + declaration.name.lexeme + ">";
    }

    @Override
    public int arity() {
        return declaration.params.size();
    }

    public class Return extends RuntimeException {
        final Object value;

        public Return(Object value) {
            super(null, null, false, false);
            this.value = value;
        }
    }

}