package com.ufan0.recordip.service;

public interface MailService {

    /**
     * 发送纯文本邮件
     * @param toAddr 发送给谁
     * @param title 标题
     * @param content 内容
     */
    public void sendTextMail(String toAddr, String title, String content);
}
