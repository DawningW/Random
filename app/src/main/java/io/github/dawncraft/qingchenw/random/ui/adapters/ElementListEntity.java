package io.github.dawncraft.qingchenw.random.ui.adapters;

import com.chad.library.adapter.base.entity.JSectionEntity;

public class ElementListEntity<T> extends JSectionEntity
{
    private boolean isHeader;
    private T header;

    public ElementListEntity(boolean isHeader, T header)
    {
        this.isHeader = isHeader;
        this.header = header;
    }

    @Override
    public boolean isHeader()
    {
        return isHeader;
    }

    public T getHeader()
    {
        return header;
    }
}
