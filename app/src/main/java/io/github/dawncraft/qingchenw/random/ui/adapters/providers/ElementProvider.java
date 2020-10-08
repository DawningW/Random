package io.github.dawncraft.qingchenw.random.ui.adapters.providers;

import com.chad.library.adapter.base.provider.BaseNodeProvider;

import io.github.dawncraft.qingchenw.random.R;

public class ElementProvider extends BaseNodeProvider
{
    @Override
    public int getItemViewType()
    {
        return 2;
    }

    @Override
    public int getLayoutId()
    {
        return R.layout.item_element;
    }

}
