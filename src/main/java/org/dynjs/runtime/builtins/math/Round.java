package org.dynjs.runtime.builtins.math;

import org.dynjs.runtime.AbstractNativeFunction;
import org.dynjs.runtime.ExecutionContext;
import org.dynjs.runtime.GlobalObject;
import org.dynjs.runtime.Types;
import org.dynjs.runtime.builtins.Math;

public class Round extends AbstractNativeFunction {
    
    public Round(GlobalObject globalObject) {
        super(globalObject);
    }

    @Override
    public Object call(ExecutionContext context, Object self, Object... args) {
        final Double x = Types.toNumber(context, args[0]).doubleValue();
        if (x.isNaN() || x.isInfinite()) { return x; }
        return Math.coerceLongIfPossible(java.lang.Math.round(x));
    }

}
