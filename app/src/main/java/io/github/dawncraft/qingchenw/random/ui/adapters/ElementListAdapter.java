package io.github.dawncraft.qingchenw.random.ui.adapters;

import android.content.DialogInterface;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AlertDialog;

import com.chad.library.adapter.base.BaseSectionQuickAdapter;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;

import java.util.List;

import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;

public class ElementListAdapter extends BaseSectionQuickAdapter<ElementListEntity<String>, BaseViewHolder>
{
    public ElementListAdapter(List<ElementListEntity<String>> data)
    {
        super(R.layout.item_element, R.layout.item_group, data);
    }

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

    @Override
    protected void convertHeader(BaseViewHolder holder, ElementListEntity<String> item)
    {
        holder.setText(R.id.nameText, item.getHeader());
    }
}
