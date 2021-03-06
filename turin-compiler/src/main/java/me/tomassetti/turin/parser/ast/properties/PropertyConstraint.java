package me.tomassetti.turin.parser.ast.properties;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.expressions.Expression;
import me.tomassetti.turin.typesystem.TypeUsage;

public class PropertyConstraint extends Node {
    private Expression condition;
    private Expression message;

    public PropertyConstraint(Expression condition, Expression message) {
        this.condition = condition;
        this.condition.setParent(this);
        this.message = message;
        this.message.setParent(this);
    }

    public Expression getCondition() {
        return condition;
    }

    public Expression getMessage() {
        return message;
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(condition, message);
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        TypeUsage conditionType = condition.calcType();
        if (!conditionType.isPrimitive() || !conditionType.asPrimitiveTypeUsage().isBoolean()) {
            errorCollector.recordSemanticError(condition.getPosition(), "A property constraint condition must have boolean type, instead it has type " + conditionType.describe());
            return false;
        }
        TypeUsage messageType = message.calcType();
        if (!messageType.isReference() ||
                !messageType.asReferenceTypeUsage().getQualifiedName().equals(String.class.getCanonicalName())) {
            errorCollector.recordSemanticError(condition.getPosition(), "A property constraint message must have String type, instead it has type " + messageType.describe());
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }


}
