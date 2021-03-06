package me.tomassetti.turin.parser.ast.imports;

import com.google.common.collect.ImmutableList;
import me.tomassetti.turin.compiler.errorhandling.ErrorCollector;
import me.tomassetti.turin.definitions.TypeDefinition;
import me.tomassetti.turin.resolvers.SymbolResolver;
import me.tomassetti.turin.parser.ast.Node;
import me.tomassetti.turin.parser.ast.QualifiedName;
import me.tomassetti.turin.symbols.Symbol;

import java.util.Optional;

public class SingleFieldImportDeclaration extends ImportDeclaration {

    private QualifiedName packagePart;
    private String typeName;
    private QualifiedName fieldsPath;
    private String alias;

    public SingleFieldImportDeclaration(QualifiedName packagePart, String typeName, QualifiedName fieldsPath) {
        this.packagePart = packagePart;
        this.packagePart.setParent(this);
        this.typeName = typeName;
        this.fieldsPath = fieldsPath;
        this.fieldsPath.setParent(this);
    }

    public SingleFieldImportDeclaration(QualifiedName packagePart, String typeName, QualifiedName fieldsPath, String alias) {
        this.packagePart = packagePart;
        this.packagePart.setParent(this);
        this.typeName = typeName;
        this.fieldsPath = fieldsPath;
        this.fieldsPath.setParent(this);
        this.alias = alias;
    }

    private String exposedName() {
        if (alias == null) {
            return fieldsPath.getName();
        } else {
            return alias;
        }
    }

    private Symbol importedValueCache = null;

    private void findImportedValue(SymbolResolver resolver) {
        if (importedValueCache != null) {
            return;
        }
        String canonicalTypeName = packagePart.qualifiedName() + "." + typeName;
        TypeDefinition typeDefinition = resolver.getTypeDefinitionIn(canonicalTypeName, this);

        if (typeDefinition.hasField(fieldsPath, true)) {
            importedValueCache = typeDefinition.getField(fieldsPath);
        }
    }

    @Override
    protected boolean specificValidate(SymbolResolver resolver, ErrorCollector errorCollector) {
        findImportedValue(resolver);
        if (importedValueCache == null) {
            errorCollector.recordSemanticError(getPosition(), "Import not resolved: " + packagePart.qualifiedName()
                    + "." + typeName + "." + fieldsPath.qualifiedName());
            return false;
        }
        return super.specificValidate(resolver, errorCollector);
    }

    @Override
    public Optional<Symbol> findAmongImported(String name, SymbolResolver resolver) {
        if (exposedName().equals(name)) {
            findImportedValue(resolver);
            if (importedValueCache == null) {
                return Optional.empty();
            }
            return Optional.of(importedValueCache);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public Iterable<Node> getChildren() {
        return ImmutableList.of(packagePart, fieldsPath);
    }
}
