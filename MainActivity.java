package com.example.lihuixu.hello;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewDebug;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.String;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.core.CvType;
import org.opencv.core.Core;
import org.opencv.core.Rect;
import org.opencv.imgproc.Imgproc;
import org.opencv.android.Utils;

public class MainActivity extends AppCompatActivity {
    private Button btn;
    private Bitmap bitmapSource;
    private static Boolean isFirst = true;
    private String tag = "hello";
    //private Map<String, Bitmap> templateMap = new HashMap<>();
    private ArrayList<Bitmap> templateMap = new ArrayList<Bitmap>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn = (Button)findViewById(R.id.btn);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                matchCards();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(tag, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_3_0, this, mLoaderCallback);
        } else {
            Log.d(tag, "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            // TODO Auto-generated method stub
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    Log.i(tag, "成功加载");
                    break;
                default:
                    super.onManagerConnected(status);
                    Log.i(tag, "加载失败");
                    break;
            }
        }
    };

    public void matchCards() {
        ArrayList<Card> pai = new ArrayList<Card>();
        Mat source = new Mat();
        bitmapSource = BitmapFactory.decodeResource(getResources(), R.drawable.pic8);
        bitmapSource = Scale(bitmapSource);
        bitmapSource = Intercept(bitmapSource, 20, 520, 1100, 128 );


        templateInit();
        Log.d("match", "matchCards: init");
        String matchResult = "I have ";

        //Iterator<Map.Entry<String, Bitmap>> it = templateMap.entrySet().iterator();
        int seque = 1;
        int lastseque = 0;
       // for (Map.Entry<String, Bitmap> entry:templateMap.entrySet()) {
       // while(it.hasNext()){
        int len = templateMap.size();
        for(int n=0 ; n < len ; ){
            Utils.bitmapToMat(bitmapSource, source);
            Mat template = new Mat();
            //Bitmap bitmapTemplate = entry.getValue();
            Bitmap bitmapTemplate = templateMap.get(n);
            Utils.bitmapToMat(bitmapTemplate, template);
            Mat result = Mat.zeros(source.rows() - template.rows() + 1, source.cols() - template.cols() + 1, CvType.CV_32FC1);

            Imgproc.matchTemplate(source, template, result, Imgproc.TM_SQDIFF);
            //Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1);
            Core.MinMaxLocResult mlr = Core.minMaxLoc(result);
            org.opencv.core.Point matchLoc = mlr.minLoc;
            double minVal = mlr.minVal;
            Log.i(tag, "minVal=" + minVal);
            if (minVal <= 20000000) {
                bitmapSource = ChangeColor(bitmapSource, matchLoc);
                //matchResult += n + minVal + "\r\n";
                //TextView textView = (TextView)findViewById(R.id.text);
                //textView.setText(matchResult);
                if(seque != lastseque){
                    Card card = new Card(seque, matchLoc);
                    card.count +=1;
                    card.loc = matchLoc;
                    pai.add(card);
                    lastseque = seque;
                }
                else{
                    int length = pai.size();
                    int index = length -1;
                    Card card = new Card(pai.get(index));
                    card.count += 1;
                    pai.set(index, card);
                }
                seque -=1;
                n -=1;
            }

            if(n == len - 1){
                ImageView imageView1 = (ImageView)findViewById(R.id.ima);
                imageView1.setImageBitmap(bitmapSource);
            }
            seque +=1;
            n += 1;
        }


        int length = pai.size();
        int count1 = 0;
        int count2 = 0;
        int count3 = 0;
        int count4 = 0;
        for(int i=0;i<length;i++){
            if(pai.get(i).id <=7)
                count1 += 1;
            else if(pai.get(i).id<=16)
                count2 +=1;
            else if(pai.get(i).id<=25)
                count3 +=1;
            else
                count4 +=1;
        }

        if(count1 != 0){
            int amount1[] = new int[count1];
            for(int i = 0;i<count1;i++){
                amount1[i] = pai.get(i).count;
            }
            int pri[] = el_getpriority(amount1);
            for(int i = 0;i<count1;i++){
                Card card = new Card(pai.get(i));
                card.priority = pri[i];
                pai.set(i, card);
            }
        }

        if(count2 !=0){
            int per = count1;
            ArrayList<Integer> gloup = new ArrayList<Integer>();
            int num = 1;
            for(int i = 1;i<count2;i++){
                if(pai.get(per+i).id == pai.get(per+i-1).id){
                    if(i == count2 - 1)
                        gloup.add(num);
                    else
                        num += 1;
                }
                else{
                    gloup.add(num);
                    num = 1;
                }
            }
            int size = gloup.size();
            int per1 = 0;
            for(int i = 0;i<size;i++){
                int cou = gloup.get(i);
                int amount[] = new int[cou];
                for(int j = 0;j<cou;j++){
                    amount[j] = pai.get(per + per1 + j).count;
                }
                int pri[] = getpriority(amount);
                for(int j = 0;j<cou;j++){
                    Card card = new Card(pai.get(per + per1 + j));
                    card.priority = pri[j];
                    pai.set(per + per1 + j, card);
                }
                per1 +=cou;
            }
        }

        if(count3 != 0){
            int per = count1 + count2;
            ArrayList<Integer> gloup = new ArrayList<Integer>();
            int num = 1;
            for(int i = 1;i<count3;i++){
                if(pai.get(per+i).id == pai.get(per+i-1).id){
                    if(i == count3 - 1)
                        gloup.add(num);
                    else
                        num += 1;
                }
                else{
                    gloup.add(num);
                    num = 1;
                }
            }
            int size = gloup.size();
            int per1 = 0;
            for(int i = 0;i<size;i++){
                int cou = gloup.get(i);
                int amount[] = new int[cou];
                for(int j = 0;j<cou;j++){
                    amount[j] = pai.get(per + per1 + j).count;
                }
                int pri[] = getpriority(amount);
                for(int j = 0;j<cou;j++){
                    Card card = new Card(pai.get(per + per1 + j));
                    card.priority = pri[j];
                    pai.set(per + per1 + j, card);
                }
                per1 +=cou;
            }

        }

        if(count4 != 0){
            int per = count1 + count2 + count3;
            ArrayList<Integer> gloup = new ArrayList<Integer>();
            int num = 1;
            for(int i = 1;i<count4;i++){
                if(pai.get(per+i).id == pai.get(per+i-1).id){
                    if(i == count4 - 1)
                        gloup.add(num);
                    else
                        num += 1;
                }
                else{
                    gloup.add(num);
                    num = 1;
                }
            }
            int size = gloup.size();
            int per1 = 0;
            for(int i = 0;i<size;i++){
                int cou = gloup.get(i);
                int amount[] = new int[cou];
                for(int j = 0;j<cou;j++){
                    amount[j] = pai.get(per + per1 + j).count;
                }
                int pri[] = getpriority(amount);
                for(int j = 0;j<cou;j++){
                    Card card = new Card(pai.get(per + per1 + j));
                    card.priority = pri[j];
                    pai.set(per + per1 + j, card);
                }
                per1 +=cou;
            }
        }

        for(int i=0;i<length;i++){
            matchResult +=  pai.get(i).id + " ";
            TextView textView = (TextView)findViewById(R.id.text);
            textView.setText(matchResult);

        }

    }

    public void templateInit() {
        List<String> name = Arrays.asList( "bai", "bei", "dong","nan", "xi", "fa", "zhong",
                "yiwan","erwan","sanwan","siwan","wuwan","liuwan","qiwan","bawan","jiuwan",
                "yitiao","ertiao","santiao","sitiao","wutiao","liutiao","qitiao","batiao","jiutiao",
                "yibing", "erbing", "sanbing","sibing","wubing", "liubing","qibing","babing",  "jiubing"
               );
        Field[] fields = R.drawable.class.getDeclaredFields();
        for (Field field:fields) {
            int resId = getResources().getIdentifier(field.getName(), "drawable", getClass().getPackage().getName());
            if (!name.contains(field.getName())) continue;
            Bitmap bitmapTemplate = BitmapFactory.decodeResource(getResources(), resId);
            Bitmap scaled = Scale(bitmapTemplate);
           // templateMap.put(field.getName(), scaled);
            templateMap.add(scaled);
        }
    }

    //图像伸缩
    public Bitmap Scale(Bitmap bitmap) {
        int rawHeight = bitmap.getHeight();
        int rawWidth = bitmap.getWidth();
        Matrix matrix = new Matrix();
        matrix.setScale(0.2f, 0.2f);
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, rawWidth, rawHeight, matrix, true);
        return newBitmap;
    }

   //图像截取
    public Bitmap Intercept(Bitmap bitmap, int x, int y, int width, int height){
        Bitmap newBitmap = Bitmap.createBitmap(bitmap, x, y, width, height);
        return newBitmap;
    }
    //改变颜色
    public Bitmap ChangeColor(Bitmap bitmap, org.opencv.core.Point loc ){
        int x = (int)loc.x;
        int y = (int)loc.y;
        for (int i = y; i < y+60; i++) {
            for (int j = x; j < x+50; j++) {
                bitmap.setPixel(j, i, Color.BLACK);
            }
        }
        return bitmap;
    }
    //中发白等计算优先级
    public static int[] el_getpriority(int count[]){
        int length = count.length;
        int pri[] = new int[length];
        for(int i = 0;i<length;i++){
            if(count[1] == 1){
                pri[1]  = 1;
            }
            else if(count[i] == 2 ){
                pri[i] = 3;
            }
            else if(count[i] == 3 ||count[i] == 4){
                pri[1] = 5;
            }
        }
        return pri;
    }
//万条饼等计算优先级
    public static int[] getpriority(int count[]){
        int length = count.length;
        int pri[] = new int[length];
        switch(length){
            case 1:
                if(count[0] == 1)
                    pri[0] = 1;
                else if(count[0] == 2)
                    pri[0] = 3;
                else if(count[0] ==3)
                    pri[0] = 5;
                break;

            case 2:
                pri = count_two(count);
                break;

            case 3:
                pri = count_three(count);
                break;

            case 4:
                pri = count_four(count);
                break;
            case 5:
                for(int i =0;i<5;i++){
                    if(count[i] == 1)
                        pri[i] = 2;
                    else if(count[i] == 2)
                        pri[i] = 3;
                    else if(count[i] == 3)
                        pri[i] = 5;
                }
                break;
            case 6:
                for(int i =0;i<6;i++){
                    if(count[i] == 1)
                        pri[i] = 2;
                    else if(count[i] == 2)
                        pri[i] = 3;
                    else if(count[i] == 3)
                        pri[i] = 5;
                }
                break;
            case 7:
                for(int i =0;i<7;i++){
                    if(count[i] == 1)
                        pri[i] = 2;
                    else if(count[i] == 2)
                        pri[i] = 3;
                    else if(count[i] == 3)
                        pri[i] = 5;
                }
                break;
            case 8:
                for(int i =0;i<8;i++){
                    if(count[i] == 1)
                        pri[i] = 2;
                    else if(count[i] == 2)
                        pri[i] = 3;
                    else if(count[i] == 3)
                        pri[i] = 5;
                }
                break;
            case 9:
                for(int i =0;i<9;i++){
                    if(count[i] == 1)
                        pri[i] = 2;
                    else if(count[i] == 2)
                        pri[i] = 3;
                    else if(count[i] == 3)
                        pri[i] = 5;
                }
                break;

        }
        return pri;
    }

    public static int[] count_two(int count[]){
        int pri[] = new int[2];
        String str = "";
        for(int i=0;i<2;i++){
            str += String.valueOf(count[i]);
        }
        switch(str){
            case "11":
                pri[0] = 2;
                pri[2] = 2;
                break;
            case "12":
                pri[0] = 2;
                pri[1] = 3;
                break;
            case "21":
                pri[0] = 3;
                pri[1] = 2;
                break;
            case "22":
                pri[0] = 3;
                pri[1] = 3;
                break;
            case "13":
                pri[0] = 2;
                pri[1] = 5;
                break;
            case "31":
                pri[0] = 5;
                pri[1] = 2;
                break;
            case "23":
                pri[0] = 3;
                pri[1] = 5;
                break;
            case "32":
                pri[0] = 5;
                pri[1] = 3;
                break;
            case "33":
                pri[0] = 5;
                pri[1] = 5;
                break;
        }
        return pri;
    }
    public static int[] count_three(int count[]) {
        int pri[] = new int[3];
        String str = "";
        for (int i = 0; i < 3; i++) {
            str += String.valueOf(count[i]);
        }
        switch(str){
            case "111":
                pri[0] = 4;
                pri[1] = 4;
                pri[2] = 4;
                break;
            case "211":
                pri[0] = 2;
                pri[1] = 4;
                pri[2] = 4;
                break;
            case "121":
                pri[0] = 4;
                pri[1] = 2;
                pri[2] = 4;
                break;
            case "112":
                pri[0] = 4;
                pri[1] = 4;
                pri[2] = 2;
                break;
            case "311":
                pri[0] = 5;
                pri[1] = 4;
                pri[2] = 4;
                break;
            case "131":
                pri[0] = 4;
                pri[1] = 5;
                pri[2] = 4;
                break;
            case "113":
                pri[0] = 4;
                pri[1] = 4;
                pri[2] = 5;
                break;
            case "221":
                pri[0] = 3;
                pri[1] = 3;
                pri[2] = 4;
                break;
            case "212":
                pri[0] = 3;
                pri[1] = 4;
                pri[2] = 3;
                break;
            case "122":
                pri[0] = 4;
                pri[1] = 3;
                pri[2] = 3;
                break;
            case "222":
                pri[0] = 5;
                pri[1] = 5;
                pri[2] = 5;
                break;
            case "321":
                pri[0] = 5;
                pri[1] = 4;
                pri[2] = 2;
                break;
            case "312":
                pri[0] = 5;
                pri[1] = 2;
                pri[2] = 4;
                break;
            case "123":
                pri[0] = 2;
                pri[1] = 4;
                pri[2] = 5;
                break;
            case "132":
                pri[0] = 2;
                pri[1] = 5;
                pri[2] = 4;
                break;
            case "213":
                pri[0] = 4;
                pri[1] = 2;
                pri[2] = 5;
                break;
            case "231":
                pri[0] = 4;
                pri[1] = 5;
                pri[2] = 2;
                break;
            case "322":
                pri[0] = 2;
                pri[1] = 5;
                pri[2] = 5;
                break;
            case "232":
                pri[0] = 5;
                pri[1] = 2;
                pri[2] = 5;
                break;
            case "223":
                pri[0] = 5;
                pri[1] = 5;
                pri[2] = 2;
                break;
            case "323":
                pri[0] = 5;
                pri[1] = 4;
                pri[2] = 5;
                break;
            case "332":
                pri[0] = 5;
                pri[1] = 5;
                pri[2] = 4;
                break;
            case "233":
                pri[0] = 4;
                pri[1] = 5;
                pri[2] = 5;
                break;
            case "133":
                pri[0] = 2;
                pri[1] = 5;
                pri[2] = 5;
                break;
            case "313":
                pri[0] = 5;
                pri[1] = 2;
                pri[2] = 5;
                break;
            case "331":
                pri[0] = 5;
                pri[1] = 5;
                pri[2] = 2;
                break;
            case "333":
                pri[0] = 5;
                pri[1] = 5;
                pri[2] = 5;
                break;
        }
        return pri;
    }

    public static int[] count_four(int count[]){
        int pri[] = new int[4];
        String str = "";
        for (int i = 0; i < 4; i++) {
            str += String.valueOf(count[i]);
        }
        switch(str){
            case "1111":
                pri[0] = 2;
                pri[1] = 4;
                pri[2] = 4;
                pri[3] = 4;
                break;
            case "1112":
                pri[0] = 4;
                pri[1] = 4;
                pri[2] = 4;
                pri[3] = 3;
                break;
            case "1121":
                pri[0] = 4;
                pri[1] = 4;
                pri[2] = 3;
                pri[3] = 4;
                break;
            case "1211":
                pri[0] = 4;
                pri[1] = 3;
                pri[2] = 4;
                pri[3] = 4;
                break;
            case "2111":
                pri[0] = 3;
                pri[1] = 4;
                pri[2] = 4;
                pri[3] = 4;
                break;
            case "1113":
                pri[0] = 4;
                pri[1] = 4;
                pri[2] = 4;
                pri[3] = 5;
                break;
            case "1131":
                pri[0] = 4;
                pri[1] = 4;
                pri[2] = 5;
                pri[3] = 2;
                break;
            case "1311":
                pri[0] = 2;
                pri[1] = 5;
                pri[2] = 4;
                pri[3] = 4;
                break;
            case "3111":
                pri[0] = 5;
                pri[1] = 4;
                pri[2] = 4;
                pri[3] = 4;
                break;
            case "1123":
                pri[0] = 4;
                pri[1] = 4;
                pri[2] = 2;
                pri[3] = 5;
                break;
            case "1132":
                pri[0] = 2;
                pri[1] = 2;
                pri[2] = 5;
                pri[3] = 3;
                break;
            case "1213":
                pri[0] = 4;
                pri[1] = 3;
                pri[2] = 4;
                pri[3] = 5;
                break;
            case "1231":
                pri[0] = 2;
                pri[1] = 3;
                pri[2] = 5;
                pri[3] = 2;
                break;
            case "2113":
                pri[0] = 2;
                pri[1] = 4;
                pri[2] = 4;
                pri[3] = 5;
                break;
            case "2131":
                pri[0] = 3;
                pri[1] = 4;
                pri[2] = 5;
                pri[3] = 2;
                break;
            case "2311":
                pri[0] = 3;
                pri[1] = 5;
                pri[2] = 2;
                pri[3] = 2;
                break;
            case "3112":
                pri[0] = 5;
                pri[1] = 4;
                pri[2] = 4;
                pri[3] = 2;
                break;
            case "3121":
                pri[0] = 5;
                pri[1] = 4;
                pri[2] = 2;
                pri[3] = 4;
                break;
            case "3211":
                pri[0] = 5;
                pri[1] = 2;
                pri[2] = 4;
                pri[3] = 4;
                break;
            case "1312":
                pri[0] = 2;
                pri[1] = 5;
                pri[2] = 4;
                pri[3] = 3;
                break;
            case "1321":
                pri[0] = 2;
                pri[1] = 5;
                pri[2] = 3;
                pri[3] = 4;
                break;
            case "1223":
                pri[0] = 2;
                pri[1] = 3;
                pri[2] = 3;
                pri[3] = 5;
                break;
            case "3221":
                pri[0] = 5;
                pri[1] = 3;
                pri[2] = 3;
                pri[3] = 2;
                break;
            case "1232":
                pri[0] = 2;
                pri[1] = 3;
                pri[2] = 5;
                pri[3] = 3;
                break;
            case "3212":
                pri[0] = 5;
                pri[1] = 3;
                pri[2] = 2;
                pri[3] = 3;
                break;
            case "1322":
                pri[0] = 2;
                pri[1] = 5;
                pri[2] = 3;
                pri[3] = 3;
                break;
            case "3122":
                pri[0] = 5;
                pri[1] = 2;
                pri[2] = 3;
                pri[3] = 3;
                break;
            default:
                for(int i =0;i<4;i++){
                    if(count[i] == 1)
                        pri[i] = 2;
                    else if(count[i] == 2)
                        pri[i] = 3;
                    else if(count[i] == 3)
                        pri[i] = 5;
                }
                break;
        }
        return pri;
    }


}


