package io.github.coolcrabs.brachyura.util;

import java.util.ArrayList;

public class CloseableArrayList extends ArrayList<AutoCloseable> implements AutoCloseable {
    public CloseableArrayList() {
        super();
    }

    @Override
    public void close() {
        Exception e = null;
        for (AutoCloseable c : this) {
            try {
                c.close();
            } catch (Exception ex) {
                if (e == null) {
                    e = ex;
                } else {
                    e.addSuppressed(ex);
                }
            }
        }
        if (e != null) {
            throw Util.sneak(e);
        }
    }
}
