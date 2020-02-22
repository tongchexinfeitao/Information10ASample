package com.bw.information10asample;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import java.util.logging.LogRecord;

import javax.net.ssl.HttpsURLConnection;

/**
 * NetUtil  联网工具类
 * <p>
 * 作用：
 * 1、发送请求           (通过接口地址请求)
 * 2、接受响应      (接受到的是流  inputstream)
 * 3、将流转换成字符串，这个字符串就是 json      inputstream -> String
 * <p>
 * 需要封装的方法：
 * 1、ioToString()    把流转成字符串
 * 2、ioToBitmap()    把流转换成 bitmap      imageview只是一个控件能够展示bitmap！ bitmap才是图片
 */
public class NetUtil {

    //用来将结果切换成主线程
    Handler handler = new Handler();

    /**
     * 将流转成字符串
     * <p>
     * len不等于 -1 ，说明读到了数据
     * len等于 -1，说明没数据了，已经读完了
     */
    private String ioToString(InputStream inputStream) throws IOException {
        //三件套走起
        byte[] bytes = new byte[1024];
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        int len = -1;

        //只有读出的长度不等于-1，才说明有数据可写 ,边读边写
        while ((len = inputStream.read(bytes)) != -1) {
            byteArrayOutputStream.write(bytes, 0, len);//写
        }

        //将 byteArrayOutputStream 中的数据转换成  bytes数组
        byte[] bytes1 = byteArrayOutputStream.toByteArray();

        //将bytes1 转换成 String
        String json = new String(bytes1);
        return json;
    }


    /**
     * 将流转成 bitmap
     */
    private Bitmap ioToBitmap(InputStream inputStream) {
        return BitmapFactory.decodeStream(inputStream);
    }

    /**
     * 是否有网
     */
    public boolean hasNet(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetworkInfo != null && activeNetworkInfo.isAvailable()) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 请求图片
     *
     * @param photoUrl
     */
    public void doGetPhoto(final String photoUrl, final ImageView imageView) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                InputStream inputStream =null;
                try {
                    URL url = new URL(photoUrl);
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    httpURLConnection.setRequestMethod("GET");
                    httpURLConnection.setConnectTimeout(5000);
                    httpURLConnection.setReadTimeout(5000);
                    httpURLConnection.connect();
                    if (httpURLConnection.getResponseCode() == 200) {
                         inputStream = httpURLConnection.getInputStream();
                        //流转bitmap
                        final Bitmap bitmap = ioToBitmap(inputStream);
                        //切换到主线程
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //只给给imageView赋值
                                imageView.setImageBitmap(bitmap);
                            }
                        });
                    } else {
                        //失败
                        Log.e("TAG", "请求图片失败");
                    }


                } catch (Exception e) {
                    e.printStackTrace();
                    //失败
                    Log.e("TAG", "请求图片失败");
                }finally {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }

    /**
     * 做 GET 请求
     * <p>
     * 整体思路：
     * 1、在子线程中
     * 2、得到 HttpURLConnection 对象
     * 3、连接前先设置好  请求方式、链接超时、读取超时
     * 4、开启连接
     * 5、获取状态码
     * 6、如果状态码为200就是成功，否则就是失败
     * 7、成功之后获取到 流
     * 8、将流转成 String
     * <p>
     * 参数的作用：
     * String httpUrl 是接口地址
     * MyCallback myCallback  是用来通知外界 请求成功/失败 的
     */
    public void doGet(final String httpUrl, final MyCallback myCallback) {
        //联网是耗时的过程，需要在子线程中进行
        new Thread(new Runnable() {
            @Override
            public void run() {
                //这里就是子线程，可以进行联网操作
                InputStream inputStream = null;
                try {
                    //创建URL对象
                    URL url = new URL(httpUrl);
                    //我们需要 HttpURLConnection
                    HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
                    //设置请求方式
                    httpURLConnection.setRequestMethod("GET");
                    //设置连接超时
                    httpURLConnection.setConnectTimeout(5000);
                    //设置读取超时
                    httpURLConnection.setReadTimeout(5000);
                    //真正的开启连接
                    httpURLConnection.connect();
                    //获取状态码，200就是成功
                    if (httpURLConnection.getResponseCode() == 200) {
                        //获取输入流
                        inputStream = httpURLConnection.getInputStream();
                        //将流转成json
                        final String json = ioToString(inputStream);

                        //切到主线程
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //打印成功日志
                                Log.e("TAG", "请求成功" + json);

                                //通知外界成功
                                myCallback.onDoGetSuccess(json);
                            }
                        });

                    } else {
                        //切到主线程
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                //把失败日志打出来
                                Log.e("TAG", "请求失败");

                                //通知外界失败
                                myCallback.onDoGetError();
                            }
                        });

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    //切到主线程
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            //把失败日志打出来
                            Log.e("TAG", "请求失败");

                            //通知外界失败
                            myCallback.onDoGetError();
                        }
                    });
                } finally {
                    //关流
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }).start();
    }


    /**
     * 成功失败的监听
     */
    public interface MyCallback {
        //通知外界成功
        void onDoGetSuccess(String json);

        //通知外界失败
        void onDoGetError();
    }

}