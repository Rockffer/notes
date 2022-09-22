package com.llw.notes;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.SimpleAdapter;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class HandWriteActivity extends AppCompatActivity {
    private LinearLayout paintLayout;
    private GridView paint_bottomMenu;
    private LineEditText et_handwrite;
    private TouchView touchView;
    private ArrayList<CharSequence> deleteChar = new ArrayList<CharSequence>();
    //菜单资源
    private int[]  paintItems = {
            R.drawable.paint_pencil,
            R.drawable.paint_icon_color,
            R.drawable.paint_icon_back,
            R.drawable.paint_icon_forward,
            R.drawable.paint_icon_delete
    };
    private Button btn_save;
    private Button btn_back;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hand_write);
        //设置为全屏模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();
        paint_bottomMenu = (GridView)findViewById(R.id.paintBottomMenu);
        paint_bottomMenu.setOnItemClickListener(new MenuClickEvent());
        et_handwrite = (LineEditText)findViewById(R.id.et_handwrite);
        InitPaintMenu();
        touchView = (TouchView)findViewById(R.id.touch_view);
        touchView.setHandler(handler);

        btn_save = (Button)findViewById(R.id.bt_save);
        btn_back = (Button)findViewById(R.id.bt_back);
        btn_save.setOnClickListener(new ClickEvent());
        btn_back.setOnClickListener(new ClickEvent());
    }

    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if(v == btn_save){

                //得到调用该Activity的Intent对象
                Intent intent = getIntent();
                Bundle b = new Bundle();
                String path = saveBitmap();
                b.putString("handwritePath", path);
                intent.putExtras(b);
                setResult(RESULT_OK, intent);
                HandWriteActivity.this.finish();

            }
            else if(v == btn_back){
                HandWriteActivity.this.finish();
            }
        }

    }
    //保存手写文件
    public String saveBitmap(){
        //获得系统当前时间，并以该时间作为文件名
        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyyMMddHHmmss");
        Date curDate   =   new   Date(System.currentTimeMillis());//获取当前时间
        String   str   =   formatter.format(curDate);
        String paintPath = "";
        str = str + "write.png";
        File dir = new File("/sdcard/notes/");
        File file = new File("/sdcard/notes/",str);
        if (!dir.exists()) {
            dir.mkdir();
        }
        else{
            if(file.exists()){
                file.delete();
            }
        }

        //将view转换成图片
        et_handwrite.setDrawingCacheEnabled(true);
        et_handwrite.setDrawingCacheBackgroundColor(Color.WHITE);
        Bitmap cutHandwriteBitmap = Bitmap.createBitmap(et_handwrite.getDrawingCache());
        et_handwrite.setDrawingCacheEnabled(false);
        try {
            //保存绘图文件路径
            paintPath = "/sdcard/notes/" + str;
            FileOutputStream out = new FileOutputStream(file);
            cutHandwriteBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return paintPath;
    }
    //配置绘图菜单
    public void InitPaintMenu(){
        ArrayList<Map<String,Object>> menus = new ArrayList<Map<String,Object>>();
        for(int i = 0;i < paintItems.length;i++){
            Map<String,Object> item = new HashMap<String,Object>();
            item.put("image",paintItems[i]);
            menus.add(item);
        }
        paint_bottomMenu.setNumColumns(paintItems.length);
        paint_bottomMenu.setSelector(R.drawable.bottom_item);
        SimpleAdapter mAdapter = new SimpleAdapter(HandWriteActivity.this, menus,R.layout.item_button, new String[]{"image"}, new int[]{R.id.item_image});
        paint_bottomMenu.setAdapter(mAdapter);
    }

    //处理界面
    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Bundle bundle = new Bundle();
            bundle = msg.getData();
            Bitmap myBitmap = bundle.getParcelable("bitmap");
            InsertToEditText(myBitmap);
        }
    };

    //将手写字插入到EditText中
    private void InsertToEditText(Bitmap mBitmap){

        int imgWidth = mBitmap.getWidth();
        int imgHeight = mBitmap.getHeight();
        //缩放比例
        float scaleW = (float) (160f/imgWidth);
        float scaleH = (float) (200f/imgHeight);

        Matrix mx = new Matrix();
        //对原图片进行缩放
        mx.postScale(scaleW, scaleH);

        mBitmap = Bitmap.createBitmap(mBitmap, 0, 0, imgWidth, imgHeight, mx, true);
        //将手写的字插入到edittext中
        SpannableString ss = new SpannableString("1");
        ImageSpan span = new ImageSpan(mBitmap, ImageSpan.ALIGN_BOTTOM);
        ss.setSpan(span, 0, 1, Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        et_handwrite.append(ss);
    }
    //设置菜单项监听器
    class MenuClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            switch(position){
                //画笔大小
                case 0:
                    showPaintSizeDialog(view);
                    break;
                //颜色
                case 1:
                    showPaintColorDialog(view);
                    break;
                //删除
                case 2:
                    Editable editable = et_handwrite.getText();
                    //找到最后一个手写字,并删除最后一个手写字
                    int selectionEnd = et_handwrite.getSelectionEnd();
                    System.out.println("end = "+ selectionEnd);
                    if(selectionEnd < 1){
                        et_handwrite.setText("");
                    }
                    else if(selectionEnd == 1){
                        et_handwrite.setText("");
                        CharSequence deleteCharSeq = editable.subSequence(0,1);
                        deleteChar.add(deleteCharSeq);
                    }
                    else{
                        System.out.println("delete");
                        CharSequence charSeq = editable.subSequence(0, selectionEnd - 1);
                        CharSequence deleteCharSeq = editable.subSequence(selectionEnd - 1,selectionEnd);
                        et_handwrite.setText(charSeq);
                        et_handwrite.setSelection(selectionEnd - 1);
                        //将删除的字存储到列表中，以便恢复使用
                        deleteChar.add(deleteCharSeq);
                    }
                    break;
                //恢复
                case 3:
                    //取出删除列表中的元素
                    int length = deleteChar.size();
                    if(length > 0){
                        et_handwrite.append(deleteChar.get(deleteChar.size()-1));
                        deleteChar.remove(deleteChar.size()-1);
                    }

                    break;
                //清空屏幕
                case 4 :

                    if(et_handwrite.getSelectionEnd() > 0){
                        AlertDialog.Builder builder = new AlertDialog.Builder(HandWriteActivity.this);
                        builder.setTitle("您确定要清空所有吗？");
                        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                et_handwrite.setText("");
                                dialog.cancel();
                            }
                        });
                        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                        Dialog dialog = builder.create();
                        dialog.show();
                    }
                    break;
                default :
                    break;
            }
        }
    }
    private int select_handwrite_color_index = 0;
    private int select_handwrite_size_index = 0;
    //弹出画笔大小选项对话框
    public void showPaintSizeDialog(View parent){



        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.custom_dialog);
        alertDialogBuilder.setTitle("选择画笔大小：");

        alertDialogBuilder.setSingleChoiceItems(R.array.paintsize, select_handwrite_size_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select_handwrite_size_index = which;
                touchView.selectHandWritetSize(which);
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }
    //弹出画笔颜色选项对话框
    public void showPaintColorDialog(View parent){



        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this,R.style.custom_dialog);
        alertDialogBuilder.setTitle("选择画笔颜色：");

        alertDialogBuilder.setSingleChoiceItems(R.array.paintcolor, select_handwrite_color_index, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                select_handwrite_color_index = which;
                touchView.selectHandWriteColor(which);
                dialog.dismiss();
            }
        });

        alertDialogBuilder.setNegativeButton("取消", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialogBuilder.create().show();
    }
}