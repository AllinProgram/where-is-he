package com.ufan0.recordip.controller;

import com.ufan0.recordip.service.MailService;
import com.ufan0.recordip.util.IpUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;

@Controller
public class IndexController {

    @Autowired
    private IpUtil ipUtil;

    @Autowired
    private MailService mailService;

    @GetMapping("ip")
    public String ipIndex() {
        return "index";
    }

    @ResponseBody
    @GetMapping("{emailEncode}")
    public String getIp(@PathVariable String emailEncode, HttpServletRequest request) {

        // 拿到访问者IP
        String ip = ipUtil.getIpAddr(request);

        // 对地址进行解码，decode[0]为邮箱，decode[1]为时间戳
        String[] decode = new String(Base64Utils.decodeFromUrlSafeString(emailEncode)).split("#");

        // 利用时间戳进行地址有效校验，时差三分钟内才是有效链接
        if (System.currentTimeMillis() - Long.parseLong(decode[1]) < 1000 * 60 * 3)
            mailService.sendTextMail(decode[0], "IP地址获取成功，请查收！", "IP地址：" + ip);
        return "要天天开心噢！";
    }

    @PostMapping("/ip")
    public String sentEmail(String email, Model model) {

        // 当前时间戳
        String now = String.valueOf(System.currentTimeMillis());

        // 对邮箱进行加密-BASE64（"邮箱" + "#" + "时间戳"）
        String encode = Base64Utils.encodeToUrlSafeString((email + "#" + now).getBytes());

        // 生成的地址发往前端
        model.addAttribute("url", "https://overflow.fun/" + encode);

        return "index";
    }
}
