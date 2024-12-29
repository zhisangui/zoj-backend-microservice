package com.zhisangui.zojbackendquestionservice.mq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zhisangui.zojbackendcommon.constant.RabbitMQConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;


/**
 * @author zsg
 */
@Component
@Slf4j
public class MqInit implements ApplicationRunner {
    @Value("${spring.rabbitmq.host:localhost}")
    private String host;
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("开始启动消息队列");
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost(host);
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.exchangeDeclare(RabbitMQConstant.EXCHANGE_NAME, "direct");

            // 创建队列
            channel.queueDeclare(RabbitMQConstant.QUEUE_NAME, true, false, false, null);
            channel.queueBind(RabbitMQConstant.QUEUE_NAME, RabbitMQConstant.EXCHANGE_NAME, RabbitMQConstant.ROUTING_KEY);
            log.info("消息队列启动成功");
        } catch (Exception e) {
            log.error("消息队列启动失败", e);
            throw new RuntimeException(e);
        }
    }
}