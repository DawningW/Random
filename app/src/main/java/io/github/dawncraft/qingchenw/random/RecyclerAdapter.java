package io.github.dawncraft.qingchenw.random;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.Holder>
{
    public Context context;
    public List<String> list;

    public RecyclerAdapter(Context context, List<String> list)
    {
        this.context = context;
        this.list = list;
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
        holder.nameText.setText(list.get(position));
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
                            Utils.toast(context, R.string.list_menu_add_empty);
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
        return list.size();
    }

    public void insertItem(int position, String name)
    {
        list.add(position, name);
        notifyItemInserted(position);
    }

    public void insertItems(int position, List<String> items)
    {
        list.addAll(items);
        notifyItemRangeInserted(position, items.size());
    }

    public void changeItem(int position, String name)
    {
        list.set(position, name);
        notifyItemChanged(position);
    }

    public void moveItem(int fromPosition, int toPosition)
    {
        // 不能用交换,因为该方法一直在被调用
        // Collections.swap(list, fromPosition, toPosition);
        String item = list.get(fromPosition);
        list.remove(fromPosition);
        list.add(toPosition, item);
        notifyItemMoved(fromPosition, toPosition);
    }

    public void deleteItem(int position)
    {
        list.remove(position);
        notifyItemRemoved(position);
    }

    public void clearItem()
    {
        int count = list.size();
        list.clear();
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
