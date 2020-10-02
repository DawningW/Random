package io.github.dawncraft.qingchenw.random.ui.widgets;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder>
{
    public Context context;
    public Map<String, List<String>> map;

    public RecyclerAdapter(Context context, Map<String, List<String>> map)
    {
        this.context = context;
        this.map = map;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_element, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final Holder holder, int position)
    {
        holder.nameText.setText(map);
        holder.nameText.setOnClickListener(new View.OnClickListener()
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
        holder.deleteButton.setOnClickListener(new View.OnClickListener()
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
    public int getItemCount()
    {
        return map.size();
    }

    public void insertItem(int position, String name)
    {
        map.add(position, name);
        notifyItemInserted(position);
    }

    public void insertItems(int position, List<String> items)
    {
        map.addAll(items);
        notifyItemRangeInserted(position, items.size());
    }

    public void changeItem(int position, String name)
    {
        map.set(position, name);
        notifyItemChanged(position);
    }

    public void moveItem(int fromPosition, int toPosition)
    {
        // 不能用交换,因为该方法一直在被调用
        // Collections.swap(list, fromPosition, toPosition);
        String item = map.get(fromPosition);
        map.remove(fromPosition);
        map.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void deleteItem(int position)
    {
        map.remove(position);
        notifyItemRemoved(position);
    }

    public void clearItem()
    {
        int count = map.size();
        map.clear();
        notifyItemRangeRemoved(0, count);
    }

    public class Holder extends RecyclerView.ViewHolder
    {
        public LinearLayout itemLayout;
        public TextView nameText;
        public ImageButton deleteButton;
        public ImageButton moveButton;

        Holder(@NonNull View itemView)
        {
            super(itemView);
            itemLayout = itemView.findViewById(R.id.itemLayout);
            nameText = itemView.findViewById(R.id.nameText);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            moveButton = itemView.findViewById(R.id.moveButton);
        }

        void select()
        {
            itemLayout.setBackgroundColor(Color.LTGRAY);
        }

        void clear()
        {
            itemLayout.setBackgroundColor(Color.WHITE);
        }
    }

    public static class Callback extends ItemTouchHelper.Callback
    {
        @Override
        public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
        {
            return makeMovementFlags(ItemTouchHelper.UP | ItemTouchHelper.DOWN, 0);
        }

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder source, @NonNull RecyclerView.ViewHolder target)
        {
            if (recyclerView.getAdapter() instanceof RecyclerAdapter)
            {
                RecyclerAdapter recyclerAdapter = ((RecyclerAdapter) recyclerView.getAdapter());
                recyclerAdapter.moveItem(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int i) {}

        @Override
        public void onSelectedChanged(RecyclerView.ViewHolder viewHolder, int actionState)
        {
            if (actionState != ItemTouchHelper.ACTION_STATE_IDLE)
            {
                ((RecyclerAdapter.Holder) viewHolder).select();
            }
            super.onSelectedChanged(viewHolder, actionState);
        }

        @Override
        public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder)
        {
            ((RecyclerAdapter.Holder) viewHolder).clear();
            super.clearView(recyclerView, viewHolder);
        }

        @Override
        public boolean isLongPressDragEnabled()
        {
            return true;
        }

        @Override
        public boolean isItemViewSwipeEnabled()
        {
            return false;
        }
    }
}
