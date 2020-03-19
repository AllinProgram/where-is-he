package com.ufan0.whereishe.controller;

import com.ufan0.whereishe.util.IpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.Base64Utils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;

@Controller
public class IndexController {

    private final String MARK = "where-is-he";

    private static Logger logger = LoggerFactory.getLogger(IndexController.class);

    @Autowired
    private IpUtil ipUtil;

    @Autowired
    private SimpMessageSendingOperations simpMessageSendingOperations;

    /**
     * @return 对页面进行伪装，防止猎奇心理用户发现
     */
    @ResponseBody
    @GetMapping({""})
    public String index() {
        return "对不起，页面发生错误。";
    }

    /**
     * @param encode  访问路径
     * @param request 获取IP地址必须
     * @return 重定向到伪装地址
     * 处理GET请求，判断请求类型 1-访问主页面、2-访问伪装地址、3-无效访问
     * 2、3类型都将重定向到伪装地址
     */
    @GetMapping("/mask/{encode}")
    public Object getUserInfo(@PathVariable String encode, HttpServletRequest request) throws InterruptedException {

        // 功能页面入口
        if (encode.equals("where-is-he")) {
            return "index";
        }
        // 非正确访问的话，站点主要功能当然不能暴露出来啦！跳转到其他网址，伪装一下
        else {
            String[] vars = null;
            // 解析传来的encode参数
            if (encode != null && !encode.equals("")) {
                // 拿到访问者IP
                String ip = ipUtil.getIpAddr(request);

                // 进行BASE64解码
                // String encodeReverse = String.valueOf(new StringBuilder(encode).reverse());
                String decode = new String(Base64Utils.decodeFromUrlSafeString(encode), StandardCharsets.UTF_8);

                // 字符串切割得到vars[0]为标记MARK，vars[1]为用户伪装url，vars[2]为时间戳
                vars = decode.split("#");

                // 利用MARK标记和时间戳进行地址有效校验，decode[0] == MARK且时差3分钟内才是有效链接
                if (vars[0].equals(MARK) && System.currentTimeMillis() - Long.parseLong(vars[2]) < 1000 * 60 * 3) {
                    Thread.sleep(1000);
                    // 传递访问者ip至监控页面
                    simpMessageSendingOperations.convertAndSend(
                            "/topic/user/" + new StringBuilder(encode).reverse(), ip
                    );
                }
            }
            assert vars != null;
            // 当用户伪装地址不为空时返回用户伪装地址，否则跳转到百度
            return new ModelAndView(new RedirectView(
                    (vars[1] == null || vars[1].equals(""))
                            ? "https://baidu.com/"
                            : vars[1])
            );
        }
    }

    /**
     * @param url 用户传来的自定义伪装地址
     * @return 返回反转后的encode作为用户查看ip信息的接口地址
     */
    @ResponseBody
    @PostMapping("/mask/where-is-he")
    public String sentInfo(String url) {

        // 伪装地址日志
        logger.info(url);

        // 当前时间戳
        String now = String.valueOf(System.currentTimeMillis());

        // 生成自定义地址
        // BASE64（"mark(判断合法地址)" + "#" + "用户定义的伪装url" + "#" + "时间戳(校验连接是否有效)"）
        return Base64Utils.encodeToUrlSafeString((MARK + "#" + url + "#" + now).getBytes());
    }
}
