# notes
笔记本
目录

基于Android的生活记事本	4
1， 概述	4
1.1研究背景	4
1.2研究意义	5
2， 系统设计	5
2.1关键技术	5
2.2系统设计	5
2.2.1系统功能结构图	5
2.2.2数据库设计	7
3， 系统实现	8
3.1 添加照片模块	9
3.2 添加拍照模块	10
3.3 添加画板模块	11
3.4 添加录音模块	15
3.5 添加手写模块	17
4  系统测试	20
4.1 添加记事本主界面	20
4.2 写字板	21
4.3 画板	26
4.4 录音	30
4.5 从相册选择相片功能	32
4.6 拍照上传功能	33
5 设计总结	34




































基于Android的生活记事本
1，概述
1.1研究背景
    当今社会人们越来越忙碌，每天有很多事情去做，因此需要记录每天的生活，也需要记录每天需要做的事情。在静下来时可以通过记事本回忆自己的生活，享受生活的静谧时光。
1.2研究意义
现在有很多的记事本软件，但是缺少一款可以通过画板，录音，书写转文字，添加图片多功能的记事本，因此需要开发一款功能齐全的记事本app，帮助更全面更好的记录生活。



2，系统设计
2.1关键技术
①通过Android的Intent机制实现拍照和添加照片以及绘画以图片的形式显示

②通过AnimationDrawable类实现录音播放逐帧动画

③通过自定义PainView类创造了自定义画板

④通过自定义LineEditText类创造了自定义的文本书写格式

⑤通过FrameLayout帧式布局将两个自定义View重叠在起 ，自定义两个View,一个是TouchView,用于在上面画图，另一个是EditText,用于将手写的字显示在其中，以实现全屏手写的功能。

⑥利用Spannablestring 实现图片插入文本。

2.2系统设计
2.2.1系统功能结构图




2.2.2数据库设计
2.2.2.1 数据名称和类型
字段	类型	是否为主键	是否为空	备注信息
Id	Integer	是	否	记录事件的id
title	text	否	否	事件的标题
context	text	否	否	事件的内容
time	varchar	否	否	事件的时间

2.2.2.2 数据的操作方法
方法	实现函数
创建数据库	Create_db()
插入数据	Insert_db(string title,string text,string time)
更新数据	Update_db(string title,string text,string time)
查询所有数据	Query_db()
根据id查询数据	Query_db(int item_ID)
根据id删除数据	Delete_db(int item_ID)
关闭数据库	Close_db()








3，系统实现
3.0 两个重要函数
①等比例缩放图片
private Bitmap resize(Bitmap bitmap,int S)：

  首先获取现有的长宽，然后等比例进行缩放处理，最后以bitmap储存。





②将图片等比例缩放到合适的大小并添加在EditText中
void InsertBitmap(Bitmap bitmap,int S,String imgPath)：

  首先调用函数等比缩放图片，用添加边框的函数将图片添加边框，然后创建SpannableString对象，取出图片所在的路径，最后在文本中插入图片即可。

3.1 添加照片模块
3.1.1 打开图库
  用intent传入参数，设置intent的type和action就可以启动打开图库。

intent = new Intent();
//设定类型为image
intent.setType("image/*");
//设置action
intent.setAction(Intent.ACTION_GET_CONTENT);
//选中相片后返回本Activity
startActivityForResult(intent, 1);
3.1.2 将图片保存在bitmap中
  调用BitmapFactory的decodeStream方法，将图片以bitmap形式储存。

bitmap = BitmapFactory.decodeStream(cr.openInputStream(uri));

3.1.3 对图片进行剪切
  调用前面的resize方法，将图片进行剪切。

   bitmap = resize(bitmap,S);

3.1.4 将图片添加到文本中
调用前面的InsertBitmap将图片插入文本

//插入图片
InsertBitmap(bitmap,480,path);


3.2 添加拍照模块
3.2.1 打开相机
使用intent,设置intent的style和action未拍照，即可启动相机

//调用系统拍照界面
intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
intent.setAction(MediaStore.ACTION_IMAGE_CAPTURE);
//区分选择相片
startActivityForResult(intent, 2);

3.2.2 获取bitmap
根据Uri获取Bitmap图片的静态方法

//这个方法是根据Uri获取Bitmap图片的静态方法
bitmap = MediaStore.Images.Media.getBitmap(cr, uri);

3.2.3 将拍照的图片保存到sdcard中
   创建文件夹，然后将图片保存到文件夹中

File dir = new File("/sdcard/notes/");
File file = new File("/sdcard/notes/",str);

3.2.4 将图片插入文本中
   调用InserBitamp方法将图片插入文本。

InsertBitmap(bitmap,480,path);




3.3 添加画板模块
3.3.1 绘画的画板实现
自定义的PaintView类中：

3.3.1.1初始画笔

Public void setPainStyle()//下面详解


3.3.1.2 初始画布

Public void initCanvas()//下面详解


3.3.1.3 重写onTouchEvent
   
重写onTouchEvent需要重写其中的三种方法，分别为手势开始的ACTION_DOWN,手势滑动过程中的ACTION_MOVE,手势结束的ACTION_DOWN







3.3.2 画笔样式的选择 

   样式的选择实际上是选择画笔的颜色，如果是画笔就设置画笔未画笔选择的颜色，如果是选择的橡皮擦就选择和画布一样的白色。



3.3.3 画笔大小的调整

根据用户选择画笔大小的位置设置画笔的大小，然后将选择的大小赋值给currentSize,调用setPainStyle更新画笔的样式。


//选择画笔大小
public void selectPaintSize(int which){
    int size =Integer.parseInt(this.getResources().getStringArray
(R.array.paintsize)[which]);
    currentSize = size;
    setPaintStyle();
}
3.3.4 画笔颜色的调整

获取用户选择对于数组中的颜色，将选择的颜色赋值给currentColor，然后调用setPainStyle()更新画笔样式。

//设置画笔颜色
public void selectPaintColor(int which){
    currentColor = paintColor[which];
    setPaintStyle();
}

3.3.5撤销功能
public void undo()：
  首先将画布清空，然后将最后一笔画前的路径画出，将最后一笔画存入deletePath路径中，savaPath中移除最后一笔画，然后将保存的路径在画布上画出。
3.3.6恢复功能
public void redo()
首先判断删除路径deletePath是否为空，若不为空，从deletePath中取出最后一笔画，存入savePath中，然后在画布上重新画出savePath的路径，刷新即可。





3.3.7 清空功能
public void removeAllPaint()
 将画布清空后刷新，然后将路径清空就实现了清空功能。


3.3.8 将绘制的绘画以图片的形式保存

创建文件file，将图片用compress方法压缩存入文件中，然后返回文件的路径。具体代码如下：

/*
 * 保存所绘图形
 * 返回绘图文件的存储路径
 * */
public String saveBitmap(){
str = str + "paint.png";
File dir = new File("/sdcard/notes/");
File file = new File("/sdcard/notes/",str);
//保存绘图文件路径
FileOutputStream out = new FileOutputStream(file);
mBitmap.compress(Bitmap.CompressFormat.PNG, 100, out);//压缩
out.flush();
out.close();
paintPath = "/sdcard/notes/" + str;
}

3.3.9 将图片插入文本
用extras获取画布图片的路径，然后通过路径取出图片，将图片插入文本中：

extras = data.getExtras();
String path = extras.getString("paintPath");
//通过路径取出图片，放入bitmap中
bitmap = BitmapFactory.decodeFile(path);
//插入绘图文件
InsertBitmap(bitmap,480,path);

3.4 添加录音模块
3.4.1 逐帧动画
3.4.1.1 录音左边和右边波形图片
①动画列表文件----麦克左边的波形：record_wave_left.xml
②动画列表文件----麦克左边的波形：record_wave_left.xml
3.4.1.2 利用AnimationDrawable实现动画

获取左右波形的位置，将波形的XML文件加入波形组件控件，通过AnimationDrawable的getDrawable()实现逐帧动画，ad_left.start()，ad_right().start()可以使逐帧动画开始，ad_left.stop(),ad_right().stop()可以使逐帧动画结束。


3.4.2创建录音文件
New File创建录音文件夹

File dir = new File("/sdcard/notes/");
File file = new File("/sdcard/notes/",str);
FilePath = dir.getPath() +"/"+ str;

3.4.3录音并将录音保存到录音文件
创建录音的MediaRcorder()组件，然后分别设置录音的资源，设置录音的输出格式，设置输出的路径，设置音频的编码器，然后用mRecorder.start()开始录音，mRecorder.stop()，mRecorder.release()，使录音停止，最后录音就保存到录音文件了。如果觉得录音不好，可以重复录音，重写创建录音文件。



3.4.4将录音以图片形式插入文本

用extra获取录音的路径，将录音转化为bitmap，然后一图片的形式插入文本。


3.5 添加手写模块

3.5.1调整画笔大小

根据用户选择的位置，选择对于画笔大小的数组，然后设置当前画笔大小select_handwrite_size_index = which为当前尺寸。

alertDialogBuilder.setSingleChoiceItems(R.array.paintsize, select_handwrite_size_index, new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
        select_handwrite_size_index = which;
        touchView.selectHandWritetSize(which);
        dialog.dismiss();
    }
});


3.5.2 画笔颜色

根据用户选择的位置，选择对于画笔大小的数组，然后设置当前画笔颜色select_handwrite_color_index = which为当前颜色。

alertDialogBuilder.setSingleChoiceItems(R.array.paintcolor, select_handwrite_color_index, new DialogInterface.OnClickListener() {
    @Override
    public void onClick(DialogInterface dialog, int which) {
        select_handwrite_color_index = which;
        touchView.selectHandWriteColor(which);
        dialog.dismiss();
    }
});


3.5.3 删除功能
首先寻找当前位置，然后获取位置前一个字，设置字为空，就实现了删除功能。

.
Editable editable = et_handwrite.getText();

//找到最后一个手写字,并删除最后一个手写字

int selectionEnd = et_handwrite.getSelectionEnd();

et_handwrite.setText("");


3.5.4 恢复功能
取出删除栈中的元素，然后在手写的字中加入删除的字，就实现了删除功能。

//取出删除列表中的元素
	  	int length = deleteChar.size();
	  	if(length > 0){
	  		et_handwrite.append(deleteChar.get(deleteChar.size()-1));
	  		deleteChar.remove(deleteChar.size()-1);
	  	}

3.5.5 清空功能
将手写字et_handwrite设置为空，就实现了清空功能。

et_handwrite.setText("");

3.5.6 全屏手写的功能
3.5.6.1 FrameLayout布局
自定义两个View,一个是TouchView,用于在上面画图，另一个是EditText,用于将手写的字显示在其中，并且，要将两个自定义View通过FrameLayout帧式布局重叠在起，以实现全屏手写的功能。


3.5.6.2 切割画布中的字并返回
获取书写字的长宽，然后压缩成bitmap，转化为图片插入文本。


3.5.6.3 将手写字插入到EditText中

4  系统测试
4.1 添加记事本主界面



4.2 写字板
4.2.1写字转文字
在写字板写字后自动转化为文字：

        




4.2.2 调整画笔大小及颜色
点击菜单栏第一个选择画笔大小，点击菜单栏第二个选择画笔颜色：

  
 
4.2.3 撤销功能
点击撤销后：
 
4.2.4 恢复功能
点击恢复后：
  
4.2.5 清空功能
点击清空：

 





4.2.6 保存为图片
点击保存按钮将文字转为图片插入文本：






4.3 画板
4.3.1 画板样式
点击菜单栏第一个选择画笔样式:





4.3.2 画笔大小
点击菜单栏第二个选择画笔大小:






4.3.3 画笔颜色
点击菜单栏第三个选择画笔颜色:

 





4.3.4 撤销功能
点击撤销后最后一笔画撤回：

 点击后：






4.3.5 恢复功能
点击恢复后：恢复最后一笔画


4.3.6 清空功能
点击清空后：清空所有的路径和笔画

4.3.6 保存为图片
将绘画的画笔转化为图片：












4.4 录音
4.4.1 点击开始录音按钮
录音开始录制并计时：




4.4.2 点击暂停录音按钮
录音暂停录制：






4.4.3 将录音以图片形式保存
录音以图片形式插入文本：











4.5 从相册选择相片功能

 












4.6 拍照上传功能
点击拍照按钮：



点击确认后即上传


















5 设计总结
    在设计这个记事本中开始遇到最大的难题是不知道怎么将图片插入文本中，即使图片插入文本中又怎么将其在点入item项将其加载出来，图片插入文本后在数据库中保存的类型又是什么，是String还是char还是什么其他类型，这些问题一直困扰着我。我在询问他人后，得知可以用富文本或者spannableString,开始尝试用富文本，但是查了很多网上方法感觉很复杂，最后选择了spannableString,可以将图片插入自定义的文本中，接着可以将图片的地址以String类型保存早文本中，最后用正则表达式判断路径的类型，将其加载出来，所以文本以String类型保存。

    设计中遇到第二大问题是不知道怎么自定义画板，并且怎么将自己画的转化为图片。首先，自定义PainActivity作为自定义画板，然后自定义View,重写onTouchEvent方法。接着创建文件保存在指定的SD卡文件夹，将文件路径返回，通过Intent发送到AddActivity,最后将路径取出，放入bitmap中。

    设计中还有一个难点时怎么实现手写功能，怎么定时更新将手写的文字转成另一个activity中的文字，并且怎么将两个activity布局在一个页面显示。自定义两个View,一个是TouchView,用于在上面画图，另一个是EditText,用于将手写的字显示在其中，并且，要将两个自定义View通过FrameLayout帧式布局重叠在起，以实现全屏手写的功能。 在TouchView中实现写字，并截取画布中的字以Bitmap保存。设置定时器，利用handle更新界面。

    在这次设计中，我通过一步步探索设计出了一个多功能记事本，很有成就感，这次设计也加强了我对Android的热爱，希望以后可以设计更多的Android App。
