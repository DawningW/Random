package io.github.dawncraft.qingchenw.random.ui.adapters;

import com.chad.library.adapter.base.BaseNodeAdapter;
import com.chad.library.adapter.base.entity.node.BaseNode;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import io.github.dawncraft.qingchenw.random.ui.adapters.entities.ElementItem;
import io.github.dawncraft.qingchenw.random.ui.adapters.entities.GroupItem;
import io.github.dawncraft.qingchenw.random.ui.adapters.providers.ElementProvider;
import io.github.dawncraft.qingchenw.random.ui.adapters.providers.GroupProvider;

public class ElementListAdapter extends BaseNodeAdapter
{
    public static final int EXPAND_COLLAPSE_PAYLOAD = 100;

    public ElementListAdapter()
    {
        super();
        addNodeProvider(new GroupProvider());
        addNodeProvider(new ElementProvider());
    }

    @Override
    protected int getItemType(@NotNull List<? extends BaseNode> data, int position)
    {
        BaseNode node = data.get(position);
        if (node instanceof GroupItem)
        {
            return 1;
        }
        else if (node instanceof ElementItem)
        {
            return 2;
        }
        return -1;
    }

    public enum ItemType
    {
        GROUP(1), ELEMENT(2), ADD_GROUP(3), ADD_ELEMENT(4);

        int _id;

        ItemType(int id)
        {
            this._id = id;
        }

        public int getID()
        {
            return _id;
        }
    }

    /*
    @Override
    protected void convert(BaseViewHolder holder, ElementListEntity<String> item)
    {
        holder.setText(R.id.nameText, item.getHeader());
        holder.getView(R.id.itemLayout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(final View view)
            {
                final EditText editText = new EditText(view.getContext());
                editText.setHint(R.string.list_item_edit_hint);
                editText.setText(holder.nameText.getText());
                AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());
                builder.setTitle(R.string.list_item_edit_title).setView(editText);
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which)
                    {
                        String text = editText.getText().toString();
                        if (!text.isEmpty())
                        {
                            changeItem(holder.getLayoutPosition(), text);
                        }
                        else
                        {
                            SystemUtils.toast(context, R.string.list_menu_add_empty);
                        }
                    }
                });
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.create().show();
            }
        });
        holder.getView(R.id.deleteButton).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                view.setClickable(false);
                deleteItem(holder.getLayoutPosition());
            }
        });
    }
    */
}
