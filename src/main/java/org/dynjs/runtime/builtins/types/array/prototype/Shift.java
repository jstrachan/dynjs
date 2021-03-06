package org.dynjs.runtime.builtins.types.array.prototype;

import org.dynjs.runtime.AbstractNativeFunction;
import org.dynjs.runtime.ExecutionContext;
import org.dynjs.runtime.GlobalObject;
import org.dynjs.runtime.JSObject;
import org.dynjs.runtime.Types;

public class Shift extends AbstractNativeFunction {

    public Shift(GlobalObject globalObject) {
        super(globalObject);
    }

    @Override
    public Object call(ExecutionContext context, Object self, Object... args) {
        // 15.4.4.9
        JSObject o = Types.toObject(context, self);
        long len = Types.toUint32(context, o.get(context, "length"));

        if (len == 0) {
            o.put(context, "length", 0, true);
            return Types.UNDEFINED;
        }

        Object first = o.get(context, "0");

        for (long k = 1; k < len; ++k) {
            boolean fromPresent = o.hasProperty(context, "" + k);

            if (fromPresent) {
                o.put(context, "" + (k - 1), o.get(context, "" + k), true);
            } else {
                o.delete(context, "" + (k - 1), true);
            }
        }

        o.delete(context, "" + (len - 1), true);
        o.put(context, "length", len - 1, true);

        return first;
    }

}
