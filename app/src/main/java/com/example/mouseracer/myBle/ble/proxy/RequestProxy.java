package com.example.mouseracer.myBle.ble.proxy;

import com.example.mouseracer.myBle.ble.L;
import com.example.mouseracer.myBle.ble.request.ConnectRequest;
import com.example.mouseracer.myBle.ble.request.MtuRequest;
import com.example.mouseracer.myBle.ble.request.NotifyRequest;
import com.example.mouseracer.myBle.ble.request.ReadRequest;
import com.example.mouseracer.myBle.ble.request.ReadRssiRequest;
import com.example.mouseracer.myBle.ble.request.Rproxy;
import com.example.mouseracer.myBle.ble.request.ScanRequest;
import com.example.mouseracer.myBle.ble.request.WriteRequest;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;


/**
 *
 * Created by LiuLei on 2017/9/1.
 */

public class RequestProxy implements InvocationHandler {
    private static final String TAG = "RequestProxy";

    private Object tar;

    private static RequestProxy instance = new RequestProxy();


    public static RequestProxy getInstance(){
        return instance;
    }

    //Bind the delegate object and return the proxy class
    public Object bindProxy(Object tar){
        this.tar = tar;
        //绑定委托对象，并返回代理类
        L.e(TAG, "bindProxy: "+"Binding agent successfully");
        Rproxy.getInstance().init(ScanRequest.class,
                ConnectRequest.class,
                NotifyRequest.class,
                ReadRequest.class,
                ReadRssiRequest.class,
                WriteRequest.class,
                MtuRequest.class
        );
        return Proxy.newProxyInstance(
                tar.getClass().getClassLoader(),
                tar.getClass().getInterfaces(),
                this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        return method.invoke(tar,args);
    }
}
