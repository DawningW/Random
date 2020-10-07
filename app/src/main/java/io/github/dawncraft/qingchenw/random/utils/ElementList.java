package io.github.dawncraft.qingchenw.random.utils;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 元素列表数据结构
 * <p>
 * Created on 2020/10/2
 *
 * @author QingChenW
 */
public class ElementList
{
    // 分隔符
    public static final CharSequence DELIMITER_ELEMENT = ",";
    public static final CharSequence DELIMITER_GROUP = ";";
    public static final CharSequence NEW_LINE = "\n";

    // 元素列表
    private List<String> groups = new ArrayList<>();
    private Map<String, List<String>> elements = new HashMap<>();

    // 请使用最下面的两个from方法实例化此类
    private ElementList() {}

    @Deprecated
    public Map<String, List<String>> getMap()
    {
        return elements;
    }

    public boolean isEmpty()
    {
        return elements.isEmpty();
    }

    public List<String> getGroups()
    {
        return new ArrayList<String>(elements.keySet());
    }

    public boolean hasGroup(String key)
    {
        return elements.containsKey(key);
    }

    public List<String> getGroup(String key)
    {
        return elements.get(key);
    }

    public void addGroup(String key)
    {
        if (!hasGroup(key))
        {
            elements.put(key, new ArrayList<String>());
        }
    }

    public void removeGroup(String key)
    {
        elements.remove(key);
    }

    public void clearGroups()
    {
        elements.clear();
    }

    public boolean hasElement(String key, String value)
    {
        if (hasGroup(key))
        {
            return getGroup(key).contains(value);
        }
        return false;
    }

    public void addElement(String key, String value)
    {
        if (hasGroup(key))
        {
            getGroup(key).add(value);
        }
    }

    public void removeElement(String key, String value)
    {
        if (hasGroup(key))
        {
            getGroup(key).remove(value);
        }
    }

    public void clearElements(String key)
    {
        if (hasGroup(key))
        {
            elements.get(key).clear();
        }
    }

    public void merge(ElementList list)
    {
        for (Map.Entry<String, List<String>> entry : list.elements.entrySet())
        {
            if (!hasGroup(entry.getKey())) addGroup(entry.getKey());
            getGroup(entry.getKey()).addAll(entry.getValue());
        }
    }

    public List<Pair<String, String>> toList()
    {
        List<Pair<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : elements.entrySet())
        {
            for (String name : entry.getValue())
            {
                list.add(Pair.create(entry.getKey(), name));
            }
        }
        return list;
    }

    /**
     * 用于内部存储的序列化
     */
    public String serialize()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, List<String>>> iterator = elements.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<String, List<String>> entry = iterator.next();
            sb.append(entry.getKey())
                    .append(":")
                    .append(Utils.join(DELIMITER_ELEMENT, entry.getValue().toArray(new String[0])));
            if (iterator.hasNext()) sb.append(DELIMITER_GROUP);
        }
        return sb.toString();
    }

    /**
     * 用于内部存储的反序列化
     */
    public void deserialize(String s)
    {
        String[] groups = s.split(String.valueOf(DELIMITER_GROUP));
        for (String group : groups)
        {
            int pos = group.indexOf(":");
            String name = group.substring(0, pos - 1);
            String[] elements = group.substring(pos + 1).split(String.valueOf(DELIMITER_ELEMENT));
            addGroup(name);
            Collections.addAll(getGroup(name), elements);
        }
    }

    /**
     * 导出为csv
     */
    public String write()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, List<String>>> iterator = elements.entrySet().iterator();
        while (iterator.hasNext())
        {
            Map.Entry<String, List<String>> entry = iterator.next();
            sb.append(entry.getKey())
                    .append(DELIMITER_ELEMENT)
                    .append(Utils.join(DELIMITER_ELEMENT, entry.getValue().toArray(new String[0])));
            if (iterator.hasNext()) sb.append(NEW_LINE);
        }
        return sb.toString();
    }

    /**
     * 从csv导入
     */
    public void read(String csv)
    {
        String[] groups = csv.split(String.valueOf(NEW_LINE));
        for (String group : groups)
        {
            String[] elements = group.split(String.valueOf(DELIMITER_ELEMENT));
            List<String> list = Arrays.asList(elements);
            String name = list.remove(0);
            addGroup(name);
            getGroup(name).addAll(list);
        }
    }

    public static ElementList fromString(String s)
    {
        ElementList list = new ElementList();
        list.deserialize(s);
        return list;
    }

    public static ElementList fromCSV(String csv)
    {
        ElementList list = new ElementList();
        list.read(csv);
        return list;
    }
}
