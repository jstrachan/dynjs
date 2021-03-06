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
import me.qmx.jitescript.CodeBlock;

import org.antlr.runtime.tree.Tree;
import org.dynjs.compiler.JSCompiler;
import org.dynjs.parser.CodeVisitor;
import org.dynjs.parser.VerifierUtils;
import org.dynjs.runtime.ExecutionContext;
import org.dynjs.runtime.Reference;

public class CompoundAssignmentExpression extends AbstractExpression {

    private final AbstractBinaryExpression rootExpr;

    public CompoundAssignmentExpression(Tree tree, AbstractBinaryExpression rootExpr) {
        super(tree);
        this.rootExpr = rootExpr;
    }
    
    public AbstractBinaryExpression getRootExpr() {
        return this.rootExpr;
    }

    @Override
    public CodeBlock getCodeBlock() {
        return new CodeBlock() {
            {
                append(rootExpr.getCodeBlock());
                // value

                dup();
                // value value

                append(rootExpr.getLhs().getCodeBlock());
                // value value reference

                swap();
                // value reference value

                aload(JSCompiler.Arities.EXECUTION_CONTEXT);
                // value reference value context

                swap();
                // value reference context value

                invokevirtual(p(Reference.class), "putValue", sig(void.class, ExecutionContext.class, Object.class));
                // value
            }
        };
    }
    
    public String toString() {
        return rootExpr.getLhs() + " " + rootExpr.getOp() + "=" + rootExpr.getRhs();
    }

    @Override
    public void accept(ExecutionContext context, CodeVisitor visitor, boolean strict) {
        visitor.visit( context, this, strict);
        
    }
}
