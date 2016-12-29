package com.github.sdw8001.scheduleview.header;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by sdw80 on 2016-11-25.
 *
 */

public abstract class GroupableHeader {
    private final ArrayList<String> mGroupingKeys = new ArrayList<>();
    private final HashMap<String, Object> mColumns = new HashMap<>();

    public void put(String key, Object value) {
        mColumns.put(key, value);
    }

    public Object get(String key) {
        return mColumns.get(key);
    }

    public void putGroupingKey(int index, String key) {
        if (mGroupingKeys.contains(key))
            mGroupingKeys.set(mGroupingKeys.indexOf(key), key);
        else
            mGroupingKeys.add(index, key);
    }

    public Object getGroupingKey(String key) {
        return mGroupingKeys.get(mGroupingKeys.indexOf(key));
    }
}
