package com.zhisangui.zojbackendquestionservice;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.zhisangui.zojbackendcommon.constant.RabbitMQConstant;
import com.zhisangui.zojbackendquestionservice.mq.MyMessageProducer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

@SpringBootTest
class ZojBackendQuestionServiceApplicationTests {

    @Resource
    private MyMessageProducer producer;
    @Test
    void contextLoads() {
        producer.sendMessage(RabbitMQConstant.EXCHANGE_NAME, RabbitMQConstant.ROUTING_KEY, "消息aaaaa");
    }

}
