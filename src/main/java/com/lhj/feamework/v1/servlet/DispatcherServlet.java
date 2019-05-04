package com.lhj.feamework.v1.servlet;

import com.lhj.feamework.v1.annotation.LhjAutowired;
import com.lhj.feamework.v1.annotation.LhjController;
import com.lhj.feamework.v1.annotation.LhjRequestMapping;
import com.lhj.feamework.v1.annotation.LhjService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.*;

/**
 * @Description:
 * @Author: lhj
 * @Time: 2019/5/4 14:12
 * @Version: 1.0
 */
public class DispatcherServlet extends HttpServlet {

    private Properties contextConfig = new Properties();
    private List<String> classNames = new ArrayList<String>();
    private Map<String,Object> ioc = new HashMap<String,Object>();
    private Map<String,Method> handlMapping = new HashMap<String,Method>();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            doDispatch(req,resp);
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        String url = req.getRequestURI();
        String contextPath = req.getContextPath();
        url = url.replaceAll(contextPath,"").replaceAll("/","/");

        if(!this.handlMapping.containsKey(url)){
            resp.getWriter().write("404 Not Found!");
            return;
        }

        Method method = this.handlMapping.get(url);
        Map<String,String[]> params = req.getParameterMap();

        String beanName = toLowerFirstCase(method.getDeclaringClass().getSimpleName());
        //method.invoke(ioc.get(beanName),new Object[]{req,resp,params.get("name")[0]});
        method.invoke(ioc.get(beanName),new Object[]{req,resp});
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        //1.加载配置
        doLoadConfig(config.getInitParameter("contextConfigLocation"));
        //2.解析并扫描相关类
        doScanner(contextConfig.getProperty("scanPackage"));
        //3.实例化相关类，并且报载道IOC容器中
        doInstance();
        //4.依赖注入
        doAutowired();
        //5.初始化HandlerMapping
        initHandlerMapping();
        System.out.println("lhj spring framework is init.");
    }

    private void initHandlerMapping() {
        if(ioc.isEmpty()){return;}

        for(Map.Entry<String,Object> entry : ioc.entrySet()){
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(LhjController.class)){continue;}

            String baseUrl = "";
            //获取controller的url配置
            if(clazz.isAnnotationPresent(LhjRequestMapping.class)){
                LhjRequestMapping requestMapping = clazz.getAnnotation(LhjRequestMapping.class);
                baseUrl = requestMapping.value();
            }
            //获取所有的public method的url配置
            Method[] methods = clazz.getMethods();
            for(Method method : methods){
                //没有RequestMapping直接忽略
                if(!method.isAnnotationPresent(LhjRequestMapping.class)){continue;}

                LhjRequestMapping requestMapping = method.getAnnotation(LhjRequestMapping.class);
                String url = ( baseUrl + requestMapping.value())
                        .replaceAll("/","/");
                handlMapping.put(url,method);
                System.out.println("Mapped" + url + "," + method);
            }
        }
    }

    private void doAutowired() {
        if(ioc.isEmpty()){return;}
        for(Map.Entry<String,Object> entry : ioc.entrySet()){
            Field[] fields = entry.getValue().getClass().getDeclaredFields();
            for(Field field : fields){
                if(!field.isAnnotationPresent(LhjAutowired.class)){continue;}
                LhjAutowired autowired = field.getAnnotation(LhjAutowired.class);
                String beanName = autowired.value().trim();
                if("".equals(beanName)){
                    beanName = field.getType().getName();
                }
                field.setAccessible(true);

                try {
                    field.set(entry.getValue(),ioc.get(beanName));
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                    continue;
                }
            }
        }
    }

    private void doInstance() {
        if(classNames.isEmpty()){return;}
        try {
            for(String className : classNames){
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(LhjController.class)){
                    Object instance = clazz.newInstance();
                    String bewanName = toLowerFirstCase(clazz.getSimpleName());
                    ioc.put(bewanName,instance);
                }else if(clazz.isAnnotationPresent(LhjService.class)){
                    //1.默认的雷鸣首字母小写
                    String beanName = toLowerFirstCase(clazz.getSimpleName());
                    LhjService service = clazz.getAnnotation(LhjService.class);
                    if(!"".equals(service.value())){
                        beanName = service.value();
                    }
                    Object instance = clazz.newInstance();
                    ioc.put(beanName,instance);
                    for (Class<?> i : clazz.getInterfaces()){
                        if(ioc.containsKey(i.getName())){
                            throw new Exception("The beanName is exists!");
                        }
                        ioc.put(i.getName(),instance);
                    }
                }else {
                    continue;
                }

            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toLowerFirstCase(String simpleName) {
        char[] chars = simpleName.toCharArray();
        chars[0] += 32;
        return String.valueOf(chars);
    }

    private void doScanner(String scanPackage) {
        //找到所有.class文件
        URL url = this.getClass().getClassLoader()
                .getResource("/" + scanPackage.replaceAll("\\.","/"));
        File classPath = new File(url.getFile());
        for(File file:classPath.listFiles()){
            if(file.isDirectory()){
                doScanner(scanPackage + "." + file.getName());
            }else {
                if (!file.getName().endsWith(".class")){continue;}
                String className = (scanPackage + "." + file.getName()).replace(".class","");
                classNames.add(className);
            }
        }
    }

    private void doLoadConfig(String contextConfigLocation) {
        InputStream fis = null;
        try {
            fis = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            contextConfig.load(fis);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(null != fis ){fis.close();}
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
