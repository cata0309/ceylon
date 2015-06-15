package com.redhat.ceylon.compiler.js;

import java.util.ArrayList;
import java.util.List;

import com.redhat.ceylon.compiler.js.util.TypeUtils;
import com.redhat.ceylon.compiler.typechecker.tree.Tree;
import com.redhat.ceylon.model.typechecker.model.Class;
import com.redhat.ceylon.model.typechecker.model.Constructor;
import com.redhat.ceylon.model.typechecker.model.ParameterList;
import com.redhat.ceylon.model.typechecker.model.TypeDeclaration;

public class Constructors {

    static void classConstructor(final Tree.Constructor that, final Tree.ClassDefinition cdef,
            final List<Tree.Constructor> constructors, final GenerateJsVisitor gen) {
        gen.comment(that);
        Constructor d = that.getDeclarationModel();
        final Class container = cdef.getDeclarationModel();
        final String fullName = gen.getNames().name(container) + "_" + gen.getNames().name(d);
        if (!TypeUtils.isNativeExternal(d) || !gen.stitchNative(d, that)) {
            generateConstructor(that, cdef, constructors, fullName, gen);
        }
        //Add reference to metamodel
        gen.out(fullName, ".$crtmm$=");
        TypeUtils.encodeForRuntime(d, that.getAnnotationList(), gen);
        gen.endLine(true);
        gen.out(gen.getNames().name(container), ".", fullName, "=", fullName);
        gen.endLine(true);
        if (gen.outerSelf(container)) {
            gen.out(".", fullName, "=", fullName);
            gen.endLine(true);
        }
    }

    private static void generateConstructor(final Tree.Constructor that, final Tree.ClassDefinition cdef,
            final List<Tree.Constructor> constructors, final String fullName,
            final GenerateJsVisitor gen) {
        final Constructor d = that.getDeclarationModel();
        final Class container = cdef.getDeclarationModel();
        final Tree.DelegatedConstructor delcons = that.getDelegatedConstructor();
        final TypeDeclaration superdec;
        final ParameterList superplist;
        final boolean callAbstract;
        if (delcons == null) {
            superdec = null;
            superplist = null;
            callAbstract = false;
        } else {
            superdec = delcons.getType().getDeclarationModel();
            /** Is the delegated constructor is within the same class we call its abstract version */
            callAbstract = superdec instanceof Class ? superdec==container :
                ((Constructor)superdec).getContainer()==container;
            superplist = superdec instanceof Class ? ((Class)superdec).getParameterList() :
                ((Constructor)superdec).getFirstParameterList();
        }
        gen.out("function ", fullName, "$$a");
        final boolean withTargs = TypeGenerator.generateParameters(
                cdef.getTypeParameterList(), that.getParameterList(), container, gen);
        final String me = gen.getNames().self(container);
        gen.beginBlock();
        gen.initParameters(that.getParameterList(), container, null);
        if (delcons != null) {
            TypeGenerator.callSuperclass(delcons.getType(), delcons.getInvocationExpression(),
                    container, superplist, that, callAbstract, null, gen);
        }
        //If there's a delegated constructor, run the statements after that one and before this one
        gen.out("\n/* A STMTS " + d, " */\n");
        gen.generateConstructorStatements(that, classStatementsBetweenConstructors(cdef, delcons, that));
        gen.out("\n/* A END STMTS " + d, " STMTS */\n");
        gen.out("return ", me, ";");
        gen.endBlockNewLine(true);
        gen.out("function ", fullName);
        TypeGenerator.generateParameters(cdef.getTypeParameterList(), that.getParameterList(),
                container, gen);
        gen.beginBlock();
        if (!d.isAbstract()) {
            gen.out("\n/* 3 " + d, " !isAbstract*/\n");
            gen.out("$init$", gen.getNames().name(container), "();");
            gen.endLine();
            gen.declareSelf(container);
            gen.referenceOuter(container);
            gen.out("\n/* 3 END " + d, " !isAbstract*/\n");
        }
        gen.initParameters(that.getParameterList(), container, null);
        if (!d.isAbstract()) {
            gen.out("\n/* 4 " + d, " !isAbstract*/\n");
            //Call common initializer
            gen.out(gen.getNames().name(container), "$$c(");
            if (withTargs) {
                gen.out("$$targs$$,");
            }
            gen.out(me, ");");
            gen.endLine();
            gen.out("\n/* 4 END " + d, " !isAbstract*/\n");
        }
        gen.out(fullName, "$$a");
        TypeGenerator.generateParameters(cdef.getTypeParameterList(), that.getParameterList(),
                container, gen);
        gen.endLine(true);
        if (d.isNative()) {
            gen.stitchConstructorHelper(cdef, "_cons_before");
        }
        gen.out("\n/* B STMTS */\n");
        gen.visitStatements(classStatementsAfterConstructor(cdef, that));
        gen.out("\n/* B END STMTS */\n");
        if (d.isNative()) {
            gen.stitchConstructorHelper(cdef, "_cons_after");
        }
        gen.out("return ", me, ";");
        gen.endBlockNewLine(true);
    }

    /** Gather all class initializer statements that are between two constructors (or just before
     * the second constructor, if the first one is null), including the statements from the
     * second constructor */
    private static List<? extends Tree.Statement> classStatementsBetweenConstructors(
            final Tree.ClassDefinition cdef, final Tree.DelegatedConstructor dc, final Tree.Constructor c2) {
        ArrayList<Tree.Statement> stmts = new ArrayList<>(cdef.getClassBody().getStatements().size());
        //Find the constructor
        Tree.Constructor c1 = null;
        if (dc != null) {
            TypeDeclaration xtd = (TypeDeclaration)((Tree.ExtendedTypeExpression)dc.getInvocationExpression().getPrimary()).getDeclaration();
            if (xtd instanceof Class && xtd == cdef.getDeclarationModel()) {
                //It's the default constructor
                for (Tree.Statement st : cdef.getClassBody().getStatements()) {
                    if (st instanceof Tree.Constructor && ((Tree.Constructor) st).getDeclarationModel().getName()==null) {
                        c1 = (Tree.Constructor)st;
                        break;
                    }
                }
            } else {
                for (Tree.Statement st : cdef.getClassBody().getStatements()) {
                    if (st instanceof Tree.Constructor && ((Tree.Constructor) st).getDeclarationModel() == xtd) {
                        c1 = (Tree.Constructor)st;
                        break;
                    }
                }
            }
        }
        boolean go = false;
        for (Tree.Statement st : cdef.getClassBody().getStatements()) {
            if (st == c1 || c1 == null) {
                go = true;
            }
            if (st == c2) {
                stmts.addAll(c2.getBlock().getStatements());
                return stmts;
            }
            if (go && st instanceof Tree.Constructor == false) {
                stmts.add(st);
            }
        }
        //Never happen!
        return null;
    }

    static List<? extends Tree.Statement> classStatementsAfterConstructor(
            final Tree.ClassDefinition cdef, final Tree.Constructor cnstr) {
        final ArrayList<Tree.Statement> stmts = new ArrayList<>(cdef.getClassBody().getStatements().size());
        boolean go=false;
        for (Tree.Statement st : cdef.getClassBody().getStatements()) {
            if (st == cnstr) {
                go=true;
            } else if (go && st instanceof Tree.Constructor == false) {
                stmts.add(st);
            }
        }
        return stmts;
    }
}
