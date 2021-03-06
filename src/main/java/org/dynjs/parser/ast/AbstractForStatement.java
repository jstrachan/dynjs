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

import static me.qmx.jitescript.util.CodegenUtils.ci;
import static me.qmx.jitescript.util.CodegenUtils.p;
import static me.qmx.jitescript.util.CodegenUtils.sig;

import java.util.List;

import me.qmx.jitescript.CodeBlock;

import org.antlr.runtime.tree.Tree;
import org.dynjs.compiler.CodeBlockUtils;
import org.dynjs.parser.CodeVisitor;
import org.dynjs.parser.Statement;
import org.dynjs.runtime.BlockManager;
import org.dynjs.runtime.Completion;
import org.dynjs.runtime.ExecutionContext;
import org.objectweb.asm.tree.LabelNode;

public abstract class AbstractForStatement extends AbstractIteratingStatement {

    private final Expression test;
    private final Expression increment;
    private final Statement block;

    public AbstractForStatement(final Tree tree, final BlockManager blockManager, final Expression test, final Expression increment, final Statement block) {
        super(tree, blockManager);
        this.test = test;
        this.increment = increment;
        this.block = block;
    }

    public abstract CodeBlock getFirstChunkCodeBlock();

    public Expression getTest() {
        return this.test;
    }

    public Expression getIncrement() {
        return this.increment;
    }

    public Statement getBlock() {
        return this.block;
    }

    public List<VariableDeclaration> getVariableDeclarations() {
        return this.block.getVariableDeclarations();
    }

    @Override
    public CodeBlock getCodeBlock() {
        return new CodeBlock() {
            {
                LabelNode begin = new LabelNode();
                LabelNode bringForward = new LabelNode();
                LabelNode hasValue = new LabelNode();
                LabelNode checkCompletion = new LabelNode();
                LabelNode doIncrement = new LabelNode();
                LabelNode doBreak = new LabelNode();
                LabelNode doContinue = new LabelNode();
                LabelNode end = new LabelNode();

                append(getFirstChunkCodeBlock());
                // <empty>

                append(normalCompletion());
                // completion

                label(begin);

                if (test != null) {
                    append(test.getCodeBlock());
                    append(jsGetValue());
                    append(jsToBoolean());
                    invokevirtual(p(Boolean.class), "booleanValue", sig(boolean.class));
                    // completion bool
                    iffalse(end);
                    // completion
                }

                // completion(prev)
                append(CodeBlockUtils.invokeCompiledStatementBlock(getBlockManager(), "For", block));
                // completion(prev) completion(cur)
                dup();
                // completion(prev) completion(cur) completion(cur)
                append(jsCompletionValue());
                // completion(prev) completion(cur) val(cur)
                ifnull(bringForward);
                // completion(prev) completion(cur)
                go_to(hasValue);

                // ----------------------------------------
                // bring previous forward

                label(bringForward);
                // completion(prev) completion(cur)
                dup_x1();
                // completion(cur) completion(prev) completion(cur)
                swap();

                // completion(cur) completion(cur) completion(prev)
                append(jsCompletionValue());
                // completion(cur) completion(cur) val(prev)
                putfield(p(Completion.class), "value", ci(Object.class));
                // completion(cur)
                go_to(checkCompletion);

                // ----------------------------------------
                // has value

                label(hasValue);
                // completion(prev) completion(cur)
                swap();
                // completion(cur) completion(prev)
                pop();
                // completion(cur)

                // ----------------------------------------
                // handle current completion

                label(checkCompletion);
                // completion
                dup();
                // completion completion

                append(handleCompletion(doIncrement, /* break */doBreak, /* continue */doContinue, /* return */end));

                // ----------------------------------------
                // do increment

                label(doIncrement);
                // completion
                if (increment != null) {
                    append(increment.getCodeBlock());
                    append(jsGetValue());
                    pop();
                }
                // completion

                go_to(begin);

                // ----------------------------------------
                // BREAK
                label(doBreak);
                // completion(block,BREAK)
                dup();
                // completion completion
                append(jsCompletionTarget());
                // completion target
                append(isInLabelSet());
                // completion bool
                iffalse(end);
                // completion
                append(convertToNormal());
                // completion(block,NORMAL)
                go_to(end);

                // ----------------------------------------
                // CONTINUE

                label(doContinue);
                // completion(block,CONTINUE)
                dup();
                // completion completion
                append(jsCompletionTarget());
                // completion target
                append(isInLabelSet());
                // completion bool
                iffalse(end);
                // completion
                append(convertToNormal());
                // completion(block,NORMAL)
                go_to(doIncrement);

                label(end);
                // completion

            }
        };
    }
}
