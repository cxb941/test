package proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/*
 * 代理类，用于给jdk代理类Proxy进行委托，该类需要实现一个接口InvocationHandler，该接口只有一个方法invoke
 */
class ProxyClass implements InvocationHandler {
    /*
     * 参数说明：
     * proxy:代理对象，该对象用于查询代理对象的其他信息，更具体作用可以参考这篇博客：
     *        http://blog.csdn.net/bu2_int/article/details/60150319；
     * method：真实对象所对应的方法
     * args:执行上面method所需要的参数
     * 有需要该方法可以选择返回值
     */
    //真实对象，invoke方法中需要用到
    Object realClass = null;
    public ProxyClass(Object realClass){
        this.realClass = realClass;
    }
    /** 
     * 绑定委托对象并返回一个代理类 
     * @param target 
     * @return 
     */  
    public Object bind() {  
        //取得代理对象  
        return Proxy.newProxyInstance(realClass.getClass().getClassLoader(),  
        		realClass.getClass().getInterfaces(), this);   //要绑定接口(这是一个缺陷，cglib弥补了这一缺陷)  
    } 
    public Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
        //在invoke方法体内部执行织入的代码
        System.out.println("这个是RealClass method1执行前要执行的代码");
        method.invoke(realClass,args);
        System.out.println("这个是RealClass method1执行后要执行的代码");
        return null;
    }
    
}