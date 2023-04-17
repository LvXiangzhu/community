package com.dlut.community.Event;

import com.alibaba.fastjson2.JSONObject;
import com.dlut.community.Service.MessageService;
import com.dlut.community.pojo.Event;
import com.dlut.community.pojo.Message;
import com.dlut.community.util.CommunityConstant;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class EventConsumer implements CommunityConstant {

    Logger logger = LoggerFactory.getLogger(EventConsumer.class);

    @Autowired
    MessageService messageService;

    @KafkaListener(topics = {TOPIC_COMMENT, TOPIC_LIKE, TOPIC_FOLLOW})
    public void handleCommentMessage(ConsumerRecord record) {
        if(record == null || record.value() == null) {
            logger.error("消息的内容为空！");
            return;
        }

        Event event = JSONObject.parseObject(record.value().toString(), Event.class);
        if(event == null) {
            logger.error("消息格式错误！");
            return;
        }

        //发送站内通知
        Message message = new Message();
        message.setFromId(SYSTEM_USER_ID);
        message.setToId(event.getEntityUserId());
        message.setConversationId(event.getTopic());
        message.setCreateTime(new Date());
        Map<String, Object> content = new HashMap<>();
        content.put("userId", event.getUserId());
        content.put("entityId", event.getEntityId());
        content.put("entityType", event.getEntityType());
        Map<String, Object> map = event.getData();
        if(!map.isEmpty()) {
            Set<Map.Entry<String, Object>> entries = map.entrySet();
            for(Map.Entry<String, Object> entry : entries) {
                content.put(entry.getKey(), entry.getValue());
            }
        }

        //转换成Json格式保存起来
        message.setContent(JSONObject.toJSONString(content));
        messageService.addMessage(message);
    }
}
