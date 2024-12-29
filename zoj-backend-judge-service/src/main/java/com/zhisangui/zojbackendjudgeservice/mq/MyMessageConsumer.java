package com.zhisangui.zojbackendjudgeservice.mq;

import com.rabbitmq.client.Channel;
import com.zhisangui.zojbackendcommon.constant.RabbitMQConstant;
import com.zhisangui.zojbackendjudgeservice.judge.JudgeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author zsg
 */
@Component
@Slf4j
public class MyMessageConsumer {
    @Resource
    private JudgeService judgeService;
    // 监听对应的消息，进行代码沙箱判题
    @RabbitListener(queues = {RabbitMQConstant.QUEUE_NAME}, ackMode = "MANUAL")
    public void receive(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        log.info("receive message: {}", message);
        long questionSubmitId = Long.parseLong(message);
        try {
            judgeService.doJudge(questionSubmitId);
            channel.basicAck(deliveryTag, false);
        } catch (IOException e) {
            log.info("receive error: {}", e.getMessage());
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}