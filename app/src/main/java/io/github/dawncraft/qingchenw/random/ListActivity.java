package io.github.dawncraft.qingchenw.random;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
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

import java.io.File;
import java.util.Collections;

import butterknife.BindAnim;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity
{
    // 分隔符
    public static final CharSequence DELIMITER = ",";

    // 适配器
    public RecyclerAdapter recyclerAdapter;
    // 触摸
    public ItemTouchHelper itemTouchHelper;

    // 控件
    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.menuLayout)
    public LinearLayout menuLayout;
    @BindView(R.id.floatingActionButton)
    public FloatingActionButton floatingActionButton;

    // 动画
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
        // 初始化布局
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        // 初始化适配器
        recyclerAdapter = new RecyclerAdapter(this, MainActivity.elements);
        recyclerView.setAdapter(recyclerAdapter);
        // 初始化触摸
        itemTouchHelper = new ItemTouchHelper(new RecyclerAdapter.Callback());
        itemTouchHelper.attachToRecyclerView(recyclerView);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.putString("elements", Utils.join(DELIMITER, MainActivity.elements.toArray(new String[]{})));
        editor.apply();
    }

    @Override
    public void onBackPressed()
    {
        if(menuLayout.getVisibility() == View.VISIBLE)
        {
            menuLayout.setVisibility(View.GONE);
            // 隐藏菜单
            menuLayout.startAnimation(dismissMenuAnim);
        }
        else
        {
            super.onBackPressed();
        }
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
                    recyclerAdapter.insertItemRange(recyclerAdapter.getItemCount() + 1,
                            recyclerAdapter.getItemCount() + num);
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
                    String[] strArray = str.split(String.valueOf(DELIMITER));
                    if(strArray.length > 0)
                    {
                        if(checkBox.isChecked()) recyclerAdapter.clearItem();
                        Collections.addAll(MainActivity.elements, strArray);
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
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        final EditText editText = new EditText(this);
        editText.setHint("输入逗号分隔符文件路径");
        linearLayout.addView(editText);
        final CheckBox checkBox = new CheckBox(this);
        checkBox.setText("强制覆盖文件");
        linearLayout.addView(checkBox);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("保存到文件").setView(linearLayout);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String path = editText.getText().toString();
                if(!new File(path).exists() || checkBox.isChecked())
                {
                    Utils.writeFile(path, Utils.join(DELIMITER, MainActivity.elements.toArray(new String[]{})));
                    return;
                }
                Utils.toast(ListActivity.this,"无法写入文件,可能是文件已存在");
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
        final TextView textView = new TextView(this);
        textView.setText("真的要清空集合吗");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("清空集合").setView(textView);
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                recyclerAdapter.clearItem();
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
