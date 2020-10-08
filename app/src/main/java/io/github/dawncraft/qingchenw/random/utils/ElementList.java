package io.github.dawncraft.qingchenw.random.utils;

import android.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
    private Map<String, List<String>> elements;

    // 请使用最下面的两个from方法实例化此类
    private ElementList()
    {
        this(new LinkedHashMap<>());
    }

    // 如果你有Map请使用这个构造函数实例化此类,否则请老老实实地读字符串去
    public ElementList(Map<String, List<String>> map)
    {
        this.elements = map;
    }

    public Map<String, List<String>> getMap()
    {
        return elements;
    }

    public void merge(ElementList list)
    {
        for (Map.Entry<String, List<String>> entry : list.getMap().entrySet())
        {
            if (!getMap().containsKey(entry.getKey())) getMap().put(entry.getKey(), new ArrayList<>());
            getMap().get(entry.getKey()).addAll(entry.getValue());
        }
    }

    public List<Pair<String, String>> toList()
    {
        List<Pair<String, String>> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : getMap().entrySet())
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
        Iterator<Map.Entry<String, List<String>>> iterator = getMap().entrySet().iterator();
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
            if (!getMap().containsKey(name)) getMap().put(name, new ArrayList<>());
            Collections.addAll(getMap().get(name), elements);
        }
    }

    /**
     * 导出为csv
     */
    public String write()
    {
        StringBuilder sb = new StringBuilder();
        Iterator<Map.Entry<String, List<String>>> iterator = getMap().entrySet().iterator();
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
            if (!getMap().containsKey(name)) getMap().put(name, new ArrayList<>());
            getMap().get(name).addAll(list);
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
