/**
 *  Copyright 2012 Douglas Campos, and individual contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.dynjs.parser.ast;

import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import java.util.List;

import me.qmx.jitescript.CodeBlock;

import org.antlr.runtime.tree.Tree;
import org.dynjs.compiler.JSCompiler;
import org.dynjs.parser.CodeVisitor;
import org.dynjs.runtime.ExecutionContext;
import org.dynjs.runtime.JSFunction;
import org.dynjs.runtime.JSObject;
import org.objectweb.asm.tree.LabelNode;

public class NewOperatorExpression extends AbstractUnaryOperatorExpression {

    public NewOperatorExpression(final Tree tree, final Expression expr, final List<Expression> argExprs) {
        super(tree, expr, "new" );
    }
    
    @Override
    public CodeBlock getCodeBlock() {
        return new CodeBlock() {
            {
                LabelNode end = new LabelNode();
                // 11.2.2
                
                aload(JSCompiler.Arities.EXECUTION_CONTEXT);
                // context
                invokevirtual(p(ExecutionContext.class), "incrementPendingConstructorCount", sig(void.class));
                // <empty>
                
                append( getExpr().getCodeBlock() );
                // obj
                
                aload( JSCompiler.Arities.EXECUTION_CONTEXT );
                // obj context
                invokevirtual(p(ExecutionContext.class), "getPendingConstructorCount", sig(int.class));
                // obj count
                iffalse( end );
                
                // obj
                aload( JSCompiler.Arities.EXECUTION_CONTEXT );
                // obj context
                swap();
                // context obj
                append(jsGetValue(JSFunction.class));
                // context ctor-fn

                bipush(0);
                anewarray(p(Object.class));
                /*
                int numArgs = argExprs.size();
                bipush(numArgs);
                anewarray(p(Object.class));
                // context function array
                for (int i = 0; i < numArgs; ++i) {
                    dup();
                    bipush(i);
                    append(argExprs.get(i).getCodeBlock());
                    append(jsGetValue());
                    aastore();
                }
                 */
                // context function array
                invokevirtual(p(ExecutionContext.class), "construct", sig(JSObject.class, JSFunction.class, Object[].class));
                // obj
                
                label( end );
                nop();
            }
        };
    }

    public String toString() {
        return "new " + getExpr();
    }
    
    public String dump(String indent) {
        return super.dump(indent) + "new " + getExpr().dump( indent + "  " );
                
    }

    @Override
    public void accept(ExecutionContext context, CodeVisitor visitor, boolean strict) {
        visitor.visit( context, this, strict);
    }
}
