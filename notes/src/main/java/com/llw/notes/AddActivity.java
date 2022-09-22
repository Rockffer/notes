package com.llw.notes;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ImageSpan;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

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
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddActivity extends AppCompatActivity {
    private GridView bottomMenu;
    private LineEditText et_Notes;
    private Button bt_back;
    private Button bt_save;
    private SQLiteDatabase db;
    private DatabaseOperation dop;
    Intent intent;
    String editModel = null;
    int item_Id;
    InputMethodManager imm;
    //底部按钮
    private int[] bottomItems = {
            R.drawable.tabbar_handwrite,
            R.drawable.tabbar_paint,
            R.drawable.tabbar_microphone,
            R.drawable.tabbar_photo,
            R.drawable.tabbar_camera,
            R.drawable.tabbar_appendix
    };
    //记录editText中的图片，用于单击时判断单击的是那一个图片
    private List<Map<String,String>> imgList = new ArrayList<Map<String,String>>();
    //配置底部菜单
    private void initBottomMenu(){
        ArrayList<Map<String,Object>> menus = new ArrayList<Map<String,Object>>();
        for(int i = 0;i < bottomItems.length;i++){
            Map<String,Object> item = new HashMap<String,Object>();
            item.put("image",bottomItems[i]);
            menus.add(item);
        }
        bottomMenu.setNumColumns(bottomItems.length);
        bottomMenu.setSelector(R.drawable.bottom_item);
        SimpleAdapter mAdapter = new SimpleAdapter(AddActivity.this, menus,R.layout.item_button, new String[]{"image"}, new int[]{R.id.item_image});
        bottomMenu.setAdapter(mAdapter);
    }
    private TextView tv_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        //设置为全屏模式
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        ActionBar actionBar=getSupportActionBar();
        actionBar.hide();

        et_Notes = (LineEditText)findViewById(R.id.et_note);
        et_Notes.setOnClickListener(new TextClickEvent());

        bottomMenu = (GridView)findViewById(R.id.bottomMenu);

        //配置菜单
        initBottomMenu();
        bottomMenu.setOnItemClickListener(new MenuClickEvent());
        bt_back = (Button)findViewById(R.id.bt_back);
        bt_back.setOnClickListener(new ClickEvent());
        bt_save = (Button)findViewById(R.id.bt_save);
        bt_save.setOnClickListener(new ClickEvent());

        //默认关闭软键盘,可以通过失去焦点设置
        //et_Notes.setFocusable(false);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(et_Notes.getWindowToken(),0);

        dop = new DatabaseOperation(this,db);
        intent = getIntent();
        editModel = intent.getStringExtra("editModel");
        item_Id = intent.getIntExtra("noteId", 0);
        tv_title = (TextView)findViewById(R.id.tv_title);
        loadData();

    }
    //为EidtText设置监听器
    class TextClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Spanned s = et_Notes.getText();
            ImageSpan[] imageSpans;
            imageSpans = s.getSpans(0, s.length(), ImageSpan.class);

            int selectionStart = et_Notes.getSelectionStart();
            for(ImageSpan span : imageSpans){

                int start = s.getSpanStart(span);
                int end = s.getSpanEnd(span);
                //找到图片
                if(selectionStart >= start && selectionStart < end){
                    //Bitmap bitmap = ((BitmapDrawable)span.getDrawable()).getBitmap();
                    //查找当前单击的图片是哪一个图片
                    //System.out.println(start+"-----------"+end);
                    String path = null;
                    for(int i = 0;i < imgList.size();i++){
                        Map map = imgList.get(i);
                        //找到了
                        if(map.get("location").equals(start+"-"+end)){
                            path = imgList.get(i).get("path");
                            break;
                        }
                    }
                    //接着判断当前图片是否是录音，如果为录音，则跳转到试听录音的Activity，如果不是，则跳转到查看图片的界面
                    //录音，则跳转到试听录音的Activity
                    if(path.substring(path.length()-3, path.length()).equals("amr")){
                        Intent intent = new Intent(AddActivity.this,ShowRecord.class);
                        intent.putExtra("audioPath", path);
                        startActivity(intent);
                    }
                    //图片，则跳转到查看图片的界面
                    else{
                        //有两种方法，查看图片，第一种就是直接调用系统的图库查看图片，第二种是自定义Activity
                        //调用系统图库查看图片
						/*Intent intent = new Intent(Intent.ACTION_VIEW);
						File file = new File(path);
						Uri uri = Uri.fromFile(file);
						intent.setDataAndType(uri, "image/*");*/
                        //使用自定义Activity
                        Intent intent = new Intent(AddActivity.this,ShowPicture.class);
                        intent.putExtra("imgPath", path);
                        startActivity(intent);
                    }
                }
                else
                    //如果单击的是空白出或文字，则获得焦点，即打开软键盘
                    imm.showSoftInput(et_Notes, 0);
            }
        }
    }
    //加载数据
    private void loadData(){

        //如果是新增记事模式，则将editText清空
        if(editModel.equals("newAdd")){
            et_Notes.setText("");
        }
        //如果编辑的是已存在的记事，则将数据库的保存的数据取出，并显示在EditText中
        else if(editModel.equals("update")){
            tv_title.setText("编辑记事");

            dop.create_db();
            Cursor cursor = dop.query_db(item_Id);
            cursor.moveToFirst();
            //取出数据库中相应的字段内容
            @SuppressLint("Range") String context = cursor.getString(cursor.getColumnIndex("context"));

            //定义正则表达式，用于匹配路径
            Pattern p=Pattern.compile("/([^\\.]*)\\.\\w{3}");
            Matcher m=p.matcher(context);
            int startIndex = 0;
            while(m.find()){
                //取出路径前的文字
                if(m.start() > 0){
                    et_Notes.append(context.substring(startIndex, m.start()));
                }

                SpannableString ss = new SpannableString(m.group().toString());

                //取出路径
                String path = m.group().toString();
                //取出路径的后缀
                String type = path.substring(path.length() - 3, path.length());
                Bitmap bm = null;
                Bitmap rbm = null;
                //判断附件的类型，如果是录音文件，则从资源文件中加载图片
                if(type.equals("amr")){
                    bm = BitmapFactory.decodeResource(getResources(), R.drawable.record_icon);
                    //缩放图片
                    rbm = resize(bm,200);

                }
                else{
                    //取出图片
                    bm = BitmapFactory.decodeFile(m.group());
                    //缩放图片
                    rbm = resize(bm,480);
                }

                //为图片添加边框效果
                rbm = getBitmapHuaSeBianKuang(rbm);
                System.out.println(rbm.getWidth()+"-------"+rbm.getHeight());
                ImageSpan span = new ImageSpan(this, rbm);
                ss.setSpan(span,0, m.end() - m.start(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                et_Notes.append(ss);
                startIndex = m.end();
                //用List记录该录音的位置及所在路径，用于单击事件
                Map<String,String> map = new HashMap<String,String>();
                map.put("location", m.start()+"-"+m.end());
                map.put("path", path);
                imgList.add(map);
            }
            //将最后一个图片之后的文字添加在TextView中
            et_Notes.append(context.substring(startIndex,context.length()));
            dop.close_db();
        }
    }
    //等比例缩放图片
    private Bitmap resize(Bitmap bitmap,int S){
        int imgWidth = bitmap.getWidth();
        int imgHeight = bitmap.getHeight();
        double partion = imgWidth*1.0/imgHeight;
        double sqrtLength = Math.sqrt(partion*partion + 1);
        //新的缩略图大小
        double newImgW = S*(partion / sqrtLength);
        double newImgH = S*(1 / sqrtLength);
        float scaleW = (float) (newImgW/imgWidth);
        float scaleH = (float) (newImgH/imgHeight);

        Matrix mx = new Matrix();//坐标
        //对原图片进行缩放
        mx.postScale(scaleW, scaleH);
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, imgWidth, imgHeight, mx, true);
        return bitmap;
    }

    //给图片加边框，并返回边框后的图片
    public Bitmap getBitmapHuaSeBianKuang(Bitmap bitmap) {
        float frameSize = 0.2f;
        Matrix matrix = new Matrix();

        // 用来做底图
        Bitmap bitmapbg = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        // 设置底图为画布
        Canvas canvas = new Canvas(bitmapbg);
        canvas.setDrawFilter(new PaintFlagsDrawFilter(0, Paint.ANTI_ALIAS_FLAG
                | Paint.FILTER_BITMAP_FLAG));

        float scale_x = (bitmap.getWidth() - 2 * frameSize - 2) * 1f
                / (bitmap.getWidth());
        float scale_y = (bitmap.getHeight() - 2 * frameSize - 2) * 1f
                / (bitmap.getHeight());
        matrix.reset();
        matrix.postScale(scale_x, scale_y);

        // 对相片大小处理(减去边框的大小)
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, true);

        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setStrokeWidth(1);
        paint.setStyle(Paint.Style.FILL);

        // 绘制底图边框
        canvas.drawRect(
                new Rect(0, 0, bitmapbg.getWidth(), bitmapbg.getHeight()),
                paint);
        // 绘制灰色边框
        paint.setColor(Color.BLUE);
        canvas.drawRect(
                new Rect((int) (frameSize), (int) (frameSize), bitmapbg
                        .getWidth() - (int) (frameSize), bitmapbg.getHeight()
                        - (int) (frameSize)), paint);

        canvas.drawBitmap(bitmap, frameSize + 1, frameSize + 1, paint);

        return bitmapbg;
    }
    //设置按钮监听器
    class ClickEvent implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.bt_back :
                    //当前Activity结束，则返回上一个Activity
                    AddActivity.this.finish();
                    break;

                //将记事添加到数据库中
                case R.id.bt_save :
                    //取得EditText中的内容
                    String context = et_Notes.getText().toString();
                    if(context.isEmpty()){
                        Toast.makeText(AddActivity.this, "记事为空!", Toast.LENGTH_LONG).show();
                    }
                    else{
                        //取得当前时间
                        SimpleDateFormat formatter   =   new   SimpleDateFormat   ("yyyy-MM-dd HH:mm");
                        Date curDate   =   new   Date(System.currentTimeMillis());//获取当前时间
                        String   time   =   formatter.format(curDate);
                        //截取EditText中的前一部分作为标题，用于显示在主页列表中
                        String title = (String) getTitle(context);
                        //打开数据库
                        dop.create_db();
                        //判断是更新还是新增记事
                        if(editModel.equals("newAdd")){
                            //将记事插入到数据库中
                            dop.insert_db(title,context,time);
                        }
                        //如果是编辑则更新记事即可
                        else if(editModel.equals("update")){
                            dop.update_db(title,context,time,item_Id);
                        }
                        dop.close_db();
                        Intent intent=new Intent(AddActivity.this,MainActivity.class);
                        startActivity(intent);
                        //结束当前activity
                        AddActivity.this.finish();
                    }
                    break;
            }
        }
    }
    //截取EditText中的前一部分作为标题，用于显示在主页列表中
    private String getTitle(String context){
        //定义正则表达式，用于匹配路径
        Pattern p=Pattern.compile("/([^\\.]*)\\.\\w{3}");
        Matcher m=p.matcher(context);
        StringBuffer strBuff = new StringBuffer();
        String title = "";
        int startIndex = 0;
        while(m.find()){
            //取出路径前的文字
            if(m.start() > 0){
                strBuff.append(context.substring(startIndex, m.start()));
            }
            //取出路径
            String path = m.group().toString();
            //取出路径的后缀
            String type = path.substring(path.length() - 3, path.length());
            //判断附件的类型
            if(type.equals("amr")){
                strBuff.append("[录音]");
            }
            else{
                strBuff.append("[图片]");
            }
            startIndex = m.end();
            //只取出前15个字作为标题
            if(strBuff.length() > 15){
                //统一将回车,等特殊字符换成空格
                title = strBuff.toString().replaceAll("\r|\n|\t", " ");
                return title;
            }
        }
        strBuff.append(context.substring(startIndex, context.length()));
        //统一将回车,等特殊字符换成空格
        title = strBuff.toString().replaceAll("\r|\n|\t", " ");
        return title;
    }

    //设置菜单项监听器
    class MenuClickEvent implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                                long id) {
            Intent intent;
            switch(position){
                //手写
                case 0:
                    intent = new Intent(AddActivity.this,HandWriteActivity.class);
                    startActivityForResult(intent, 5);
                    break;
                //绘图
                case 1:
                    intent = new Intent(AddActivity.this,PaintActivity.class);
                    startActivityForResult(intent, 3);
                    break;
                //语音
                case 2:
                    intent = new Intent(AddActivity.this,ActivityRecord.class);
                    startActivityForResult(intent, 4);
                    break;
                //照片
                case 3:
                    intent = new Intent(Intent.ACTION_PICK,android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,"image/*");
                    startActivityForResult(intent, 1);
                    break;
                //拍照
                case 4 :
                    //调用系统拍照界面
                    intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
                    //区分选择相片
                    startActivityForResult(intent, 2);
                //附件
                case 5 :
                    break;

            }

        }

    }

    //将图片等比例缩放到合适的大小并添加在EditText中
    void InsertBitmap(Bitmap bitmap,int S,String imgPath){

        bitmap = resize(bitmap,S);
        //添加边框效果
        bitmap = getBitmapHuaSeBianKuang(bitmap);
        //bitmap = addBigFrame(bitmap,R.drawable.line_age);
        final ImageSpan imageSpan = new ImageSpan(this,bitmap);
        SpannableString spannableString = new SpannableString(imgPath);
        spannableString.setSpan(imageSpan, 0, spannableString.length(), SpannableString.SPAN_MARK_MARK);
        //光标移到下一行
        //et_Notes.append("\n");
        Editable editable = et_Notes.getEditableText();
        int selectionIndex = et_Notes.getSelectionStart();
        spannableString.getSpans(0, spannableString.length(), ImageSpan.class);

        //将图片添加进EditText中
        editable.insert(selectionIndex, spannableString);
        //添加图片后自动空出两行
        et_Notes.append("\n");

        //用List记录该录音的位置及所在路径，用于单击事件
        Map<String,String> map = new HashMap<String,String>();
        map.put("location", selectionIndex+"-"+(selectionIndex+spannableString.length()));
        map.put("path", imgPath);
        imgList.add(map);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            //取得数据
            Uri uri = data.getData();
            ContentResolver cr = AddActivity.this.getContentResolver();
            Bitmap bitmap = null;
            Bundle extras = null;
            //如果是选择照片
            if(requestCode == 1){
                // 这里开始的第二部分，获取图片的路径：
                String[] proj = {MediaStore.Images.Media.DATA};
                Cursor cursor = managedQuery(uri, proj, null, null, null);

//按我个人理解 这个是获得用户选择的图片的索引值
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                cursor.moveToFirst();
//最后根据索引值获取图片路径
                String path = cursor.getString(column_index);
                System.out.println("pppppp:"+path);
                try {
                    //将对象存入Bitmap中
                    bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                //插入图片
                InsertBitmap(bitmap,480,path);
            }
            //如果选择的是拍照
            else if(requestCode == 2){
                System.out.println("-----fjwefowefwef");;
                try {

                    if(uri != null)
                        //这个方法是根据Uri获取Bitmap图片的静态方法
                        bitmap = MediaStore.Images.Media.getBitmap(cr, uri);
                        //这里是有些拍照后的图片是直接存放到Bundle中的所以我们可以从这里面获取Bitmap图片
                    else{
                        extras = data.getExtras();
                        bitmap = extras.getParcelable("data");
                    }
                    //将拍的照片存入指定的文件夹下
                    //获得系统当前时间，并以该时间作为文件名
                    SimpleDateFormat   formatter   =   new   SimpleDateFormat   ("yyyyMMddHHmmss");
                    Date   curDate   =   new   Date(System.currentTimeMillis());//获取当前时间
                    String   str   =   formatter.format(curDate);
                    String paintPath = "";
                    str = str + "paint.png";
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
                    FileOutputStream fos = new FileOutputStream(file);
                    // 将 bitmap 压缩成其他格式的图片数据
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
                    fos.flush();
                    fos.close();
                    String path = "/sdcard/notes/" + str;
                    //插入图片
                    System.out.println("tttttttttt:"+path);
                    InsertBitmap(bitmap,480,path);

                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            //返回的是绘图后的结果
            else if(requestCode == 3){
                extras = data.getExtras();
                String path = extras.getString("paintPath");
                //通过路径取出图片，放入bitmap中
                bitmap = BitmapFactory.decodeFile(path);
                //插入绘图文件
                InsertBitmap(bitmap,480,path);
            }
            //返回的是录音文件
            else if(requestCode == 4){
                extras = data.getExtras();
                String audioPath = extras.getString("audio");
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.record_microphone_icon);
                //插入录音图标
                InsertBitmap(bitmap,80,audioPath);
            }
            //返回的是手写文件
            else if(requestCode == 5){
                extras = data.getExtras();
                String path = extras.getString("handwritePath");
                //通过路径取出图片，放入bitmap中
                bitmap = BitmapFactory.decodeFile(path);
                //插入绘图文件
                InsertBitmap(bitmap,680,path);
            }
        }
    }
}