package io.github.dawncraft.qingchenw.random;

import android.app.Activity;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindAnim;
import butterknife.BindView;
import butterknife.ButterKnife;

public class ListActivity extends AppCompatActivity
{
    // 分隔符
    public static final CharSequence DELIMITER = ",";
    // 选择文件的请求代码
    public static final int FILE_SELECT_CODE = 1;

    // 适配器
    public RecyclerAdapter recyclerAdapter;
    // 触摸
    public ItemTouchHelper itemTouchHelper;

    // 控件
    @BindView(R.id.recyclerView)
    public RecyclerView recyclerView;
    @BindView(R.id.backgroundView)
    public View backgroundView;
    @BindView(R.id.menuLayout)
    public LinearLayout menuLayout;
    @BindView(R.id.floatingActionButton)
    public FloatingActionButton floatingActionButton;

    // 动画
    @BindAnim(R.anim.anim_fade_in)
    public Animation fadeInAnim;
    @BindAnim(R.anim.anim_fade_out)
    public Animation fadeOutAnim;
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
        // 初始化菜单
        backgroundView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                switchMenu();
            }
        });
        dismissMenuAnim.setAnimationListener(new Animation.AnimationListener()
        {
            @Override
            public void onAnimationStart(Animation animation) {}

            @Override
            public void onAnimationEnd(Animation animation)
            {
                backgroundView.setVisibility(View.GONE);
                menuLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case FILE_SELECT_CODE:
            {
                if (resultCode == Activity.RESULT_OK)
                {
                    Uri uri = data.getData();
                    if (uri != null)
                    {
                        String path = Utils.getPath(this, uri);
                        loadDialog(path);
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed()
    {
        if (menuLayout.getVisibility() == View.VISIBLE)
        {
            switchMenu();
        }
        else
        {
            super.onBackPressed();
        }
    }

    public void onClicked(View view)
    {
        switch (view.getId())
        {
            case R.id.floatingActionButton:
                switchMenu();
                break;
            case R.id.addFloatButton:
                inputDialog();
                break;
            case R.id.addMoreFloatButton:
                batchDialog();
                break;
            case R.id.readFloatButton:
                loadDialog("");
                break;
            case R.id.saveFloatButton:
                saveDialog();
                break;
            case R.id.clearFloatButton:
                clearDialog();
                break;
        }
    }

    public void switchMenu()
    {
        if (menuLayout.getVisibility() != View.VISIBLE)
        {
            backgroundView.setVisibility(View.VISIBLE);
            menuLayout.setVisibility(View.VISIBLE);
            // 显示菜单动画
            backgroundView.startAnimation(fadeInAnim);
            menuLayout.startAnimation(showMenuAnim);
        }
        else
        {
            // FIX 先隐藏后播放动画会导致View重新显示
            // 隐藏菜单动画
            backgroundView.startAnimation(fadeOutAnim);
            menuLayout.startAnimation(dismissMenuAnim);
        }
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
                    Utils.toast(ListActivity.this, R.string.list_menu_add_empty);
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
                int num = Integer.valueOf(editText.getText().toString());
                if (num > 0)
                {
                    List<String> items = new ArrayList<>();
                    for (int i = 1; i <= num; i++) items.add(String.valueOf(i));
                    recyclerAdapter.insertItems(recyclerAdapter.getItemCount(), items);
                }
                else
                {
                    Utils.toast(ListActivity.this,
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
                String str = Utils.readFile(editText.getText().toString());
                if(!str.isEmpty())
                {
                    String[] items = str.split(String.valueOf(DELIMITER));
                    if(items.length > 0)
                    {
                        if(checkBox.isChecked()) recyclerAdapter.clearItem();
                        recyclerAdapter.insertItems(recyclerAdapter.getItemCount(), Arrays.asList(items));
                        return;
                    }
                }
                Utils.toast(ListActivity.this,R.string.list_menu_load_invalid);
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
                    Utils.toast(ListActivity.this, R.string.list_menu_load_no_file_manager);
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
                if(!new File(path).exists() || checkBox.isChecked())
                {
                    Utils.writeFile(path, Utils.join(DELIMITER, MainActivity.elements.toArray(new String[]{})));
                    return;
                }
                Utils.toast(ListActivity.this,R.string.list_menu_save_failed);
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
        int padding = Utils.dp2px(this, 16);
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
