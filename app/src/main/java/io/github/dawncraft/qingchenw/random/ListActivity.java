package io.github.dawncraft.qingchenw.random;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindAnim;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity
{
    public static final int EXTERNAL_STORAGE_REQ_CODE = 10;
    public static List<String> elements = new ArrayList<>();

    public RecyclerAdapter recyclerAdapter;
    public ItemTouchHelper itemTouchHelper;

    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.menuLayout)
    public LinearLayout menuLayout;
    @BindView(R.id.floatingActionButton)
    public FloatingActionButton floatingActionButton;

    @BindAnim(R.anim.anim_menu_show)
    public Animation showMenuAnim;
    @BindAnim(R.anim.anim_menu_dismiss)
    public Animation dismissMenuAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        if(getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // 初始化ButterKnife
        ButterKnife.bind(this);
        // 申请权限
        int permission = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (permission != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    EXTERNAL_STORAGE_REQ_CODE);
        }
        // 初始化配置
        loadConfig();
        // 初始化布局
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        // 初始化适配器
        recyclerAdapter = new RecyclerAdapter(this, elements);
        recyclerView.setAdapter(recyclerAdapter);
        // 初始化触摸
        itemTouchHelper = new ItemTouchHelper(new RecyclerAdapter.Callback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onDestroy()
    {
        saveConfig();
        super.onDestroy();
    }

    private static void loadConfig()
    {
        elements.clear();
        int size = MainActivity.sharedPreferences.getInt("elements_size", 0);
        for(int i = 0; i < size; i++)
        {
            elements.add(MainActivity.sharedPreferences.getString("element_" + i, "unknown"));
        }
    }

    public static void saveConfig()
    {
        SharedPreferences.Editor editor = MainActivity.sharedPreferences.edit();
        int size = elements.size();
        editor.putInt("elements_size", size);
        for(int i = 0; i < size; i++)
        {
            editor.putString("element_" + i, elements.get(i));
        }
        editor.apply();
    }

    public void onClicked(View view)
    {
        switch(view.getId())
        {
            case R.id.floatingActionButton:
                switchMenu();
                break;
            case R.id.addFloatButton:
                inputNameDialog();
                break;
            case R.id.addManyFloatButton:
                inputNumberDialog();
                break;
            case R.id.readFloatButton:
                readFloatButton();
                break;
            case R.id.saveFloatButton:
                saveFloatButton();
                break;
            case R.id.clearFloatButton:
                clearDialog();
                break;
        }
    }

    public void switchMenu()
    {
        if(menuLayout.getVisibility() != View.VISIBLE)
        {
            menuLayout.setVisibility(View.VISIBLE);
            // 显示菜单
            menuLayout.startAnimation(showMenuAnim);
        }
        else
        {
            menuLayout.setVisibility(View.GONE);
            // 隐藏菜单
            menuLayout.startAnimation(dismissMenuAnim);
        }
    }

    public void inputNameDialog()
    {
        final EditText editText = new EditText(this);
        editText.setHint("请输入元素名字");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入名字").setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                recyclerAdapter.insertItem(recyclerAdapter.getItemCount(), editText.getText().toString());
                saveConfig();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dialog.show();
    }

    public void inputNumberDialog()
    {
        final EditText editText = new EditText(this);
        editText.setText(String.valueOf(50));
        editText.setHint("请输入大于0的整数");
        editText.setInputType(InputType.TYPE_CLASS_NUMBER);
        editText.setSelection(editText.getText().length());
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("输入个数").setView(editText);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                int num = Integer.valueOf(editText.getText().toString());
                if(num > 0 && num < Integer.MAX_VALUE)
                {
                    recyclerAdapter.insertItemRange(recyclerAdapter.getItemCount(),
                            recyclerAdapter.getItemCount() + num);
                    saveConfig();
                }
                else
                {
                    Utils.toast(ListActivity.this, "无效参数, 范围为: " + 0 + " ~ " + Integer.MAX_VALUE);
                }
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dialog.show();
    }

    public void readFloatButton()
    {
        LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(this);
        editText.setHint("输入逗号分隔符文件路径");
        linearLayout.addView(editText);
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText("导入前清空集合");
        linearLayout.addView(checkBox);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("从文件中读取").setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String str = Utils.readFile(editText.getText().toString());
                if(str != null)
                {
                    String[] strArray = str.split(",");
                    if(strArray.length > 0)
                    {
                        if(checkBox.isChecked()) recyclerAdapter.clearItem();
                        Collections.addAll(elements, strArray);
                        saveConfig();
                        return;
                    }
                }
                Utils.toast(ListActivity.this,"不是有效的逗号分隔符文件");
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dialog.show();
    }

    public void saveFloatButton()
    {
        LinearLayout linearLayout = new LinearLayout(this);
        final EditText editText = new EditText(this);
        editText.setHint("输入逗号分隔符文件路径");
        linearLayout.addView(editText);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("保存到文件").setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                StringBuilder strBuilder = new StringBuilder();
                for (int i = 0; i < elements.size(); i++)
                {
                    strBuilder.append(elements.get(i));
                    if(i != elements.size() - 1)
                    {
                        strBuilder.append(",");
                    }
                }
                Utils.writeFile(editText.getText().toString(), strBuilder.toString());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dialog.show();
    }

    public void clearDialog()
    {
        LinearLayout linearLayout = new LinearLayout(this);
        final TextView textView = new TextView(this);
        textView.setText("真的要清空集合吗");
        linearLayout.addView(textView);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清空集合").setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                recyclerAdapter.clearItem();
                saveConfig();
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.dismiss();
            }
        });
        Dialog dialog = builder.create();
        dialog.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dialog.show();
    }
}
