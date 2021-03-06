package org.dynjs.runtime.builtins.types.array.prototype;

import org.dynjs.runtime.AbstractNativeFunction;
import org.dynjs.runtime.ExecutionContext;
import org.dynjs.runtime.GlobalObject;
import org.dynjs.runtime.JSObject;
import org.dynjs.runtime.Types;

public class Reverse extends AbstractNativeFunction {

    public Reverse(GlobalObject globalObject) {
        super(globalObject);
    }

    @Override
    public Object call(ExecutionContext context, Object self, Object... args) {
        // 15.4.4.8
        JSObject o = Types.toObject(context, self);
        long len = Types.toUint32(context, o.get(context, "length"));

        long middle = (long) Math.floor(len / 2);
        long lower = 0;

        while (lower != middle) {
           long upper = len - lower - 1;

            Object lowerValue = o.get(context, "" + lower);
            Object upperValue = o.get(context, "" + upper);

            boolean lowerExists = o.hasProperty(context, "" + lower);
            boolean upperExists = o.hasProperty(context, "" + upper);

            if (lowerExists && upperExists) {
                o.put(context, "" + lower, upperValue, true);
                o.put(context, "" + upper, lowerValue, true);
            } else if (upperExists) {
                o.put(context, "" + lower, upperValue, true);
                o.delete(context, "" + upper, true);
            } else if (lowerExists) {
                o.put(context, "" + upper, lowerValue, true);
                o.delete(context, "" + lower, true);
            } else {
                // no action required
            }
            ++lower;
        }

        return o;
    }

}
