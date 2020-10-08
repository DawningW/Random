package io.github.dawncraft.qingchenw.random.ui.adapters.entities;

import com.chad.library.adapter.base.entity.node.BaseExpandNode;
import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class GroupItem extends BaseExpandNode
{
    private List<BaseNode> childNode;
    private String name;

    public GroupItem(String name, List<BaseNode> childNode)
    {
        this.name = name;
        this.childNode = childNode;
        setExpanded(false);
    }

    public String getName()
    {
        return name;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode()
    {
        return childNode;
    }
}
