package io.github.dawncraft.qingchenw.random.ui;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.entity.node.BaseNode;
import com.leinardi.android.speeddial.SpeedDialActionItem;
import com.leinardi.android.speeddial.SpeedDialView;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.github.dawncraft.qingchenw.random.R;
import io.github.dawncraft.qingchenw.random.RandomApplication;
import io.github.dawncraft.qingchenw.random.ui.adapters.ElementListAdapter;
import io.github.dawncraft.qingchenw.random.ui.adapters.entities.ElementItem;
import io.github.dawncraft.qingchenw.random.ui.adapters.entities.GroupItem;
import io.github.dawncraft.qingchenw.random.utils.ElementList;
import io.github.dawncraft.qingchenw.random.utils.FileUtils;
import io.github.dawncraft.qingchenw.random.utils.SystemUtils;
import io.github.dawncraft.qingchenw.random.utils.Utils;

public class ListActivity extends AppCompatActivity implements SpeedDialView.OnActionSelectedListener
{
    // 选择文件的请求代码
    public static final int FILE_SELECT_CODE = 1;

    // 适配器
    private ElementListAdapter recyclerAdapter;

    // 控件
    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.floatingActionButton)
    public SpeedDialView floatingActionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化ButterKnife
        ButterKnife.bind(this);
        // 初始化布局
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        // 初始化适配器
        recyclerAdapter = new ElementListAdapter();
        recyclerView.setAdapter(recyclerAdapter);
        // 读取集合
        String str = RandomApplication.sharedPreferences.getString("elements", "");
        recyclerAdapter.setList(generateList(ElementList.fromString(str)));
        // 初始化菜单
        floatingActionButton.setOnActionSelectedListener(this);

    }

    @Override
    protected void onPause()
    {
        super.onPause();
        Map<String, List<String>> map = new LinkedHashMap<>();
        for (BaseNode node : recyclerAdapter.getData())
        {
            GroupItem group = (GroupItem) node;
            map.put(group.getName(), new ArrayList<>());
            List<String> list = map.get(group.getName());
            for (BaseNode node2 : group.getChildNode())
            {
                ElementItem element = (ElementItem) node2;
                list.add(element.getName());
            }
        }
        ElementList elementList = new ElementList(map);
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("elements", elementList.serialize());
        editor.apply();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode)
        {
            case FILE_SELECT_CODE:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = data.getData();
                    if (uri != null)
                    {
                        String path = SystemUtils.getPath(this, uri);
                        loadDialog(path);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (floatingActionButton.isOpen())
        {
            floatingActionButton.close();
        }
        else
        {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onActionSelected(SpeedDialActionItem actionItem)
    {
        switch (actionItem.getId())
        {
            case R.id.action_add:
                inputDialog();
                break;
            case R.id.action_add_more:
                batchDialog();
                break;
            case R.id.action_read:
                loadDialog("");
                break;
            case R.id.action_save:
                saveDialog();
                break;
            case R.id.action_clear:
                clearDialog();
                break;
            default: return false;
        }
        return true;
    }

    private List<GroupItem> generateList(ElementList elementList)
    {
        List<GroupItem> list = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : elementList.getMap().entrySet())
        {
            List<BaseNode> list2 = new ArrayList<>();
            for (String name : entry.getValue())
            {
                list2.add(new ElementItem(name));
            }
            list.add(new GroupItem(entry.getKey(), list2));
        }
        return list;
    }

    public void inputDialog()
    {
        final EditText editText = new EditText(this);
        editText.setHint(R.string.list_menu_add_hint);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.list_menu_add_title).setView(editText);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String text = editText.getText().toString();
                if (!text.isEmpty())
                {
                    recyclerAdapter.insertItem(recyclerAdapter.getItemCount(), text);
                }
                else
                {
                    SystemUtils.toast(ListActivity.this, R.string.list_menu_add_empty);
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    /* 默认添加元素的数量 */
    private static final int DEFAULT_ADD_NUMBER = 50;

    public void batchDialog()
    {
        final EditText editText = new EditText(this);
        editText.setText(String.valueOf(DEFAULT_ADD_NUMBER));
        editText.setHint(R.string.list_menu_addmore_hint);
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setSelection(editText.getText().length());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.list_menu_addmore_title).setView(editText);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                int num = Integer.parseInt(editText.getText().toString());
                if (num > 0)
                {
                    List<String> items = new ArrayList<>();
                    for (int i = 1; i <= num; i++) items.add(String.valueOf(i));
                    recyclerAdapter.insertItems(recyclerAdapter.getItemCount(), items);
                }
                else
                {
                    SystemUtils.toast(ListActivity.this,
                            String.format(getString(R.string.list_menu_addmore_invalid), Integer.MAX_VALUE));
                }
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    public void loadDialog(String path)
    {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(this);
        editText.setHint(R.string.list_menu_load_hint);
        editText.setText(path);
        linearLayout.addView(editText);
        final Button button = new Button(this);
        button.setText(R.string.list_menu_load_choose);
        linearLayout.addView(button);
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText(R.string.list_menu_load_empty);
        linearLayout.addView(checkBox);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.list_menu_load_title).setView(linearLayout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String str = FileUtils.readFile(editText.getText().toString());
                if (!Utils.isStrNullOrEmpty(str))
                {
                    ElementList temp = ElementList.fromCSV(str);
                    if (checkBox.isChecked()) recyclerAdapter.clearItem();
                    recyclerAdapter.insertItems(recyclerAdapter.getItemCount(), temp.getMap());
                    return;
                }
                SystemUtils.toast(ListActivity.this,R.string.list_menu_load_invalid);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        final Dialog dialog = builder.create();
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try
                {
                    // Intent.createChooser(intent, "选择逗号分隔符文件");
                    startActivityForResult(intent, FILE_SELECT_CODE);
                    dialog.dismiss();
                } catch (ActivityNotFoundException e) {
                    SystemUtils.toast(ListActivity.this, R.string.list_menu_load_no_file_manager);
                }
            }
        });
        dialog.show();
    }

    public void saveDialog()
    {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(this);
        editText.setHint(R.string.list_menu_save_hint);
        linearLayout.addView(editText);
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText(R.string.list_menu_save_cover);
        linearLayout.addView(checkBox);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.list_menu_save_title).setView(linearLayout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String path = editText.getText().toString();
                if (!new File(path).exists() || checkBox.isChecked())
                {
                    FileUtils.writeFile(path, elementList.write());
                    return;
                }
                SystemUtils.toast(ListActivity.this,R.string.list_menu_save_failed);
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }

    public void clearDialog()
    {
        final TextView textView = new TextView(this);
        textView.setText(R.string.list_menu_clear_hint);
        textView.setTextSize(18);
        int padding = SystemUtils.dp2px(this, 16);
        textView.setPaddingRelative(padding, padding, padding, padding);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.list_menu_clear_title).setView(textView);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                recyclerAdapter.clearItem();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
        builder.create().show();
    }
}
