package com.zhisangui.zojbackendquestionservice.mq;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * @author zsg
 */
@Component
public class MyMessageProducer {
    @Resource
    private RabbitTemplate rabbitTemplate;

    /**
     * 发送消息
     * @param exchange 交换机
     * @param routingKey 路由键
     * @param message 发送的消息
     */
    public void sendMessage(String exchange, String routingKey, String message) {
        rabbitTemplate.convertAndSend(exchange, routingKey, message);
    }
}
