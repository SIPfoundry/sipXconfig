/*
 *
 *
 * Copyright (C) 2007 Pingtel Corp., certain elements licensed under a Contributor Agreement.
 * Contributors retain copyright to elements licensed under a Contributor Agreement.
 * Licensed to the User under the LGPL license.
 *
 *
 */
package org.sipfoundry.sipxconfig.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;

public final class DataCollectionUtil {

    private DataCollectionUtil() {
        // prevent creation
    }

    public static Collection removeByPrimaryKey(Collection c, Object primaryKey) {
        return removeByPrimaryKey(c, new Object[] {
            primaryKey
        });
    }

    public static <T extends BeanWithId> Map<Integer, T> idToObjectMap(Collection<T> c) {
        Map<Integer, T> map = new HashMap<Integer, T>();
        for (T bean : c) {
            map.put(bean.getId(), bean);
        }
        return map;
    }

    public static Map<Integer, String> idToNameMap(Collection<? extends BeanWithId> c) {
        Map<Integer, String> map = new HashMap<Integer, String>();
        for (BeanWithId bean : c) {
            map.put(bean.getId(), ((NamedObject) bean).getName());
        }
        return map;
    }

    public static Map<String, String> idAsStringToNameMap(Collection<? extends BeanWithId> c) {
        Map<String, String> map = new HashMap<String, String>();
        for (BeanWithId bean : c) {
            map.put(bean.getId().toString(), ((NamedObject) bean).getName());
        }
        return map;
    }

    /**
     * Removes items from collections by their primary key and updates the positions on the items
     *
     * @param c items must implement DataCollectionItem
     */
    public static Collection removeByPrimaryKey(Collection c, Object... primaryKeys) {
        Collection removed = findByPrimaryKey(c, primaryKeys);
        Iterator remove = removed.iterator();
        while (remove.hasNext()) {
            c.remove(remove.next());
        }
        return removed;
    }

    /**
     * Given a list of objects that know their primary keys, extract a collection of just primary
     * keys
     *
     * @param c collection of PrimaryKeySource objects
     * @return collecion of primary keys
     */
    public static Collection extractPrimaryKeys(Collection c) {
        ArrayList list = new ArrayList(c.size());
        for (Iterator i = c.iterator(); i.hasNext();) {
            list.add(((PrimaryKeySource) i.next()).getPrimaryKey());
        }
        return list;
    }

    public static Collection findByPrimaryKey(Collection c, Object[] primaryKeys) {
        List list = new ArrayList();

        Iterator find = c.iterator();
        while (find.hasNext()) {
            PrimaryKeySource item = (PrimaryKeySource) find.next();
            for (int j = 0; j < primaryKeys.length; j++) {
                if (item.getPrimaryKey().equals(primaryKeys[j])) {
                    list.add(item);
                }
            }
        }

        return list;
    }

    public static <T extends PrimaryKeySource> T findByKey(Collection<T> c, Object primaryKey) {
        // mark items to be deleted
        Iterator<T> find = c.iterator();
        while (find.hasNext()) {
            T item = find.next();
            if (item.getPrimaryKey().equals(primaryKey)) {
                return item;
            }
        }

        return null;
    }

    /**
     * Moves items from collections by their primary key and updates the positions on the items.
     * Items that are attempted to move out of list bounds are quietly moved to beginning or end
     * of list.
     *
     * @param step how many slots to move items, positive or negative
     * @param c items must implement DataCollectionItem
     */
    public static void moveByPrimaryKey(List c, Object[] primaryKeys, int step) {
        Set toBeMovedSet = new HashSet(primaryKeys.length);
        CollectionUtils.addAll(toBeMovedSet, primaryKeys);

        if (step < 0) {
            // move up
            int minPosition = 0;
            for (int i = 0; i < c.size() && !toBeMovedSet.isEmpty(); i++) {
                PrimaryKeySource item = (PrimaryKeySource) c.get(i);
                if (toBeMovedSet.remove(item.getPrimaryKey())) {
                    int newPosition = Math.max(minPosition, i + step);
                    c.remove(i);
                    c.add(newPosition, item);
                    minPosition = newPosition + 1;
                }
            }
        } else if (step > 0) {
            // move down
            int maxPosition = c.size() - 1;
            for (int i = c.size() - 1; i >= 0 && !toBeMovedSet.isEmpty(); i--) {
                PrimaryKeySource item = (PrimaryKeySource) c.get(i);
                if (toBeMovedSet.remove(item.getPrimaryKey())) {
                    int newPosition = Math.min(i + step, maxPosition);
                    c.remove(i);
                    c.add(newPosition, item);
                    maxPosition = newPosition - 1;
                }
            }
        }
    }

    /**
     * Deep copy for bean collection. Duplicate and not clone is called which makes it hibernate
     * friendly. (all new objects have UNSAVED id set).
     *
     * We do not want to deal with cloning the collection itself. The caller has to provide a
     * newly created (or at least empty) destination collection.
     *
     * @param from source collection
     * @param to destination (empty collection)
     */
    public static void duplicate(Collection from, Collection to) {
        if (to.size() > 0) {
            throw new IllegalArgumentException("destination collection has to be empty");
        }
        for (Iterator i = from.iterator(); i.hasNext();) {
            BeanWithId bean = (BeanWithId) i.next();
            to.add(bean.duplicate());
        }
    }

    public static <T extends BeanWithId> Collection<T> merge(Collection<T> a, Collection<T> b) {
        Map<Integer, T> map = new HashMap<Integer, T>();
        for (T t : a) {
            map.put(t.getId(), t);
        }
        for (T t : b) {
            map.put(t.getId(), t);
        }
        return map.values();
    }

    public static <T extends BeanWithId> Collection<T> remove(Collection<T> a, Collection<T> b) {
        Map<Integer, T> map = new HashMap<Integer, T>();
        for (T t : a) {
            map.put(t.getId(), t);
        }
        for (T t : b) {
            map.remove(t.getId());
        }
        return map.values();
    }

    /**
     * Move a single item in the list by specified offset
     *
     * If the move would take the item outside of the valid list range the item is moved to the
     * beginning or end of the list instead.
     *
     * @param list to be modified by this funtion
     * @param index of the element to be moved
     * @param offset by which item will be moved, negative for moving "up"/"towards beginning of
     *        the list"
     * @return new index of an item
     *
     */
    public static int move(List list, int index, int offset) {
        Object item = list.remove(index);
        int newIndex = index + offset;
        if (newIndex < 0) {
            newIndex = 0;
        }
        if (newIndex > list.size()) {
            newIndex = list.size();
        }
        list.add(newIndex, item);
        return newIndex;
    }

    public static <E> List<E> getPage(List<E> all, int first, int pageSize) {
        int last = first + pageSize;
        if (last > all.size()) {
            last = all.size();
        }
        return all.subList(first, last);
    }

    public static String toString(Object o) {
        if (o instanceof Collection<?>) {
            StringBuilder sb = new StringBuilder();
            Iterator<?> i = ((Collection<?>) o).iterator();
            for (int ndx = 0; i.hasNext(); ndx++) {
                if (sb.length() == 0) {
                    sb.append("[");
                } else {
                    sb.append(",");
                }
                sb.append(toString(i.next()));
                if (!i.hasNext()) {
                    sb.append("]");
                }
            }
            return sb.toString();
        } else {
            return ReflectionToStringBuilder.toString(o);
        }
    }
}
