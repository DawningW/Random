package io.github.dawncraft.qingchenw.random.ui.adapters.providers;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.chad.library.adapter.base.provider.BaseNodeProvider;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.ui.adapters.entities.GroupItem;

public class GroupProvider extends BaseNodeProvider
{
    @Override
    public int getItemViewType()
    {
        return 1;
    }

    @Override
    public int getLayoutId()
    {
        return R.layout.item_group;
    }

    @Override
    public void convert(BaseViewHolder holder, BaseNode node)
    {
        GroupItem entity = (GroupItem) node;
        holder.setText(R.id.nameText, entity.getName());
    }
}
