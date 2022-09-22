package com.llw.notes;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private Button bt_add;

    private SQLiteDatabase db;
    private DatabaseOperation dop;
    private ListView lv_notes;
    private TextView tv_note_id;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //设置为全屏模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        bt_add = (Button)findViewById(R.id.bt_add);
        bt_add.setOnClickListener(new ClickEvent());

        dop = new DatabaseOperation(this, db);
        lv_notes = (ListView)findViewById(R.id.lv_notes);
//显示记事列表
        showNotesList();
//为记事列表添加监听器
        lv_notes.setOnItemClickListener(new ItemClickEvent());

        lv_notes.setOnItemLongClickListener(new ItemLongClickEvent());

    }
    //记事列表长按监听器
    class ItemLongClickEvent implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view,
                                       int position, long id) {
            tv_note_id = (TextView)view.findViewById(R.id.tv_note_id);
            int item_id = Integer.parseInt(tv_note_id.getText().toString());
            simpleList(item_id);
            return true;
        }

    }
    //简单列表对话框，用于选择操作
    public void simpleList(final int item_id){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.custom_dialog);
        alertDialogBuilder.setTitle("选择操作");
        alertDialogBuilder.setIcon(R.drawable.ic_launcher);
        alertDialogBuilder.setItems(R.array.itemOperation, new android.content.DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                switch(which){
                    //编辑
                    case 0 :
                        Intent intent = new Intent(MainActivity.this,AddActivity.class);
                        intent.putExtra("editModel", "update");
                        intent.putExtra("noteId", item_id);
                        startActivity(intent);
                        break;
                    //删除
                    case 1 :
                        dop.create_db();
                        dop.delete_db(item_id);
                        dop.close_db();
                        //刷新列表显示
                        lv_notes.invalidate();
                        showNotesList();
                        break;
                }
            }
        });
        alertDialogBuilder.create();
        alertDialogBuilder.show();
    }
    //显示记事列表
    private void showNotesList(){
        //创建或打开数据库
        dop.create_db();
        Cursor cursor = dop.query_db();
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                R.layout.note_item,
                cursor,
                new String[]{"_id","title","time"}, new int[]{R.id.tv_note_id,R.id.tv_note_title,R.id.tv_note_time});
        lv_notes.setAdapter(adapter);
        dop.close_db();

    }
    //记事列表单击监听器
    class ItemClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            tv_note_id = (TextView)view.findViewById(R.id.tv_note_id);
            int item_id = Integer.parseInt(tv_note_id.getText().toString());
            Intent intent = new Intent(MainActivity.this,AddActivity.class);
            intent.putExtra("editModel", "update");
            intent.putExtra("noteId", item_id);
            startActivity(intent);
            finish();
        }
    }

    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.bt_add :
                    Intent intent = new Intent(MainActivity.this,AddActivity.class);
                    intent.putExtra("editModel", "newAdd");
                    startActivity(intent);
                    finish();
            }
        }
    }
}