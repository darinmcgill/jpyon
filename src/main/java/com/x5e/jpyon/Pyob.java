package com.x5e.jpyon;

import java.util.*;

/**
 * Created by darin on 6/26/16.
 */
public class Pyob {
    public String kind;
    public List<Object> ordered;
    public Map<String,Object> mapped;

    public Pyob(String kind, Collection<? extends Object> ordered, Map<String,? extends Object> mapped) {
        this.kind = kind;
        if (ordered != null) {
            this.ordered = new ArrayList<Object>(ordered);
        } else {
            this.ordered = new ArrayList<Object>();
        }
        if (mapped != null) {
            this.mapped = new LinkedHashMap<String, Object>(mapped);
        } else {
            this.mapped = new LinkedHashMap<String, Object>();
        }
    }
    public Pyob(String kind) {
        this(kind,null,null);
    }
    public Pyob(String kind,Collection<? extends Object> ordered) {
        this(kind,ordered,null);
    }
    public Pyob(String kind,Map<String,? extends Object> mapped) {
        this(kind,null,mapped);
    }

    public Object get(Object obj) {
        if (obj instanceof String) {
            return mapped.get(obj);
        }
        if (obj instanceof Integer) {
            return ordered.get((Integer) obj);
        }
        throw new RuntimeException("bad index: " + obj.toString());
    }

}