package com.app.monitor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * 监控控制类
 *
 * @author wxn
 * @date 2020/7/16
 */
@RestController
public class MonitorController {
    @Autowired
    private HttpServletRequest request;

    @RequestMapping("/info")
    public ModelAndView monitor(ModelAndView ModelAndView) {
        //获取参数 param
        Map<String, String> paramMap = new HashMap<>();
        Enumeration enumeration = request.getParameterNames();
        while (enumeration.hasMoreElements()) {
            String paramName = (String) enumeration.nextElement();
            String paramValue = request.getParameter(paramName);
            paramMap.put(paramName, paramValue);
        }

        //获取 header
        Map<String, String> headerMap = new HashMap<>();
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            headerMap.put(key, value);
        }
        //获取 Cookie
        Map<String, String> cookieMap = new HashMap<>();
        Cookie[] cookies = request.getCookies();
        if(cookies != null && cookies.length > 0){
            for (Cookie cookie : cookies){
                cookieMap.put(cookie.getName(), cookie.getValue());
            }
        }

        // 内存使用率
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        List<Map<String, Object>> memoryResult = new ArrayList<Map<String, Object>>();
        Map<String, Object> info = new HashMap<String, Object>();
        info.put("OS启动时JVM初始化内存", String.format("%.2f GB", (double)memoryMXBean.getHeapMemoryUsage().getInit() /1073741824));
        info.put("JVM使用内存", String.format("%.2f GB", (double)memoryMXBean.getHeapMemoryUsage().getUsed() /1073741824));
        info.put("JVM最大有效内存", String.format("%.2f GB", (double)memoryMXBean.getHeapMemoryUsage().getMax() /1073741824));
        info.put("保证JVM可用内存", String.format("%.2f GB", (double)memoryMXBean.getHeapMemoryUsage().getCommitted() /1073741824));
        memoryResult.add(info);

        // CPU使用情况
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        List<Map<String, Object>> threadResult = new ArrayList<Map<String, Object>>();
        for(Long threadID : threadMXBean.getAllThreadIds()) {
            ThreadInfo threadInfo = threadMXBean.getThreadInfo(threadID);
            Map<String, Object> threadMap = new HashMap<String, Object>();
            threadMap.put("线程名称", threadInfo.getThreadName());
            threadMap.put("线程状态", threadInfo.getThreadState());
            threadMap.put("占用cpu时长", String.format("%s ns", threadMXBean.getThreadCpuTime(threadID)));
            threadResult.add(threadMap);
        }

        // 磁盘使用率
        String osName = System.getProperty("os.name");
        List<Map<String, Object>> driveResult = new ArrayList<Map<String, Object>>();
        Map<String, Object> drive = new HashMap<String, Object>();
        if (osName.contains("Windows")) {
            File cDrive = new File("C:");
            drive.put("总磁盘空间", String.format("%.2f GB", (double) cDrive.getTotalSpace() / 1073741824));
            drive.put("未分配磁盘空间", String.format("%.2f GB", (double) cDrive.getFreeSpace() / 1073741824));
            drive.put("可用磁盘空间", String.format("%.2f GB", (double) cDrive.getUsableSpace() / 1073741824));
        } else if (osName.contains("Linux")) {
            File root = new File("/");
            drive.put("总磁盘空间", String.format("%.2f GB", (double) root.getTotalSpace() / 1073741824));
            drive.put("未分配磁盘空间", String.format("%.2f GB", (double) root.getFreeSpace() / 1073741824));
            drive.put("可用磁盘空间", String.format("%.2f GB", (double) root.getUsableSpace() / 1073741824));
        }
        driveResult.add(drive);

        ModelAndView.addObject("param", paramMap.toString());
        ModelAndView.addObject("cookies", cookieMap.toString());
        ModelAndView.addObject("header", headerMap.toString());
        ModelAndView.addObject("memoryResult", memoryResult.toString());
        ModelAndView.addObject("threadResult", threadResult.toString());
        ModelAndView.addObject("driveResult", driveResult.toString());
        ModelAndView.setViewName("monitor");
        return ModelAndView;
    }
}
