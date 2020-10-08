package io.github.dawncraft.qingchenw.random.ui.adapters.entities;

import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ElementItem extends BaseNode
{
    private String name;

    public ElementItem(String name)
    {
        this.name = name;
    }

    public String getName()
    {
        return name;
    }

    @Nullable
    @Override
    public List<BaseNode> getChildNode()
    {
        return null;
    }
}
