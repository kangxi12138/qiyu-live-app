package org.qiyu.live.gift.provider.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboReference;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.MQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.client.producer.SendStatus;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.idea.qiyu.live.framework.redis.starter.key.GiftProviderCacheKeyBuilder;
import org.qiyu.live.bank.interfaces.IQiyuCurrencyAccountRpc;
import org.qiyu.live.common.interfaces.enums.CommonStatusEum;
import org.qiyu.live.common.interfaces.topic.GiftProviderTopicNames;
import org.qiyu.live.common.interfaces.utils.ListUtils;
import org.qiyu.live.gift.constants.RedPacketStatusCodeEnum;
import org.qiyu.live.gift.dto.RedPacketConfigReqDTO;
import org.qiyu.live.gift.dto.RedPacketReceiveDTO;
import org.qiyu.live.gift.provider.dao.mapper.RedPacketConfigMapper;
import org.qiyu.live.gift.provider.dao.po.RedPacketConfigPO;
import org.qiyu.live.gift.provider.service.IRedPacketConfigService;
import org.qiyu.live.gift.provider.service.bo.SendRedPacketBO;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.im.dto.ImMsgBody;
import org.qiyu.live.im.router.interfaces.constants.ImMsgBizCodeEnum;
import org.qiyu.live.im.router.interfaces.rpc.ImRouterRpc;
import org.qiyu.live.living.interfaces.dto.LivingRoomReqDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @Author kangxi
 * @Date: Created in 08:17 2023/9/5
 * @Description
 */
@Service
public class RedPacketConfigServiceImpl implements IRedPacketConfigService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RedPacketConfigServiceImpl.class);

    @Resource
    private RedPacketConfigMapper redPacketConfigMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private GiftProviderCacheKeyBuilder cacheKeyBuilder;
//    @DubboReference
    private IQiyuCurrencyAccountRpc qiyuCurrencyAccountRpc;
    @Resource
    private MQProducer mqProducer;
//    @DubboReference
    private ImRouterRpc imRouterRpc;
//    @DubboReference
    private ILivingRoomRpc livingRoomRpc;

    @Override
    public RedPacketConfigPO queryByAnchorId(Long anchorId) {
        LambdaQueryWrapper<RedPacketConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedPacketConfigPO::getAnchorId, anchorId);
        queryWrapper.eq(RedPacketConfigPO::getStatus, RedPacketStatusCodeEnum.NOT_PREPARE.getCode());
        queryWrapper.orderByDesc(RedPacketConfigPO::getCreateTime);
        queryWrapper.last("limit 1");
        return redPacketConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public RedPacketConfigPO queryByConfigCode(String configCode) {
        LambdaQueryWrapper<RedPacketConfigPO> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(RedPacketConfigPO::getConfigCode, configCode);
        queryWrapper.eq(RedPacketConfigPO::getStatus, RedPacketStatusCodeEnum.IS_PREPARE.getCode());
        queryWrapper.orderByDesc(RedPacketConfigPO::getCreateTime);
        queryWrapper.last("limit 1");
        return redPacketConfigMapper.selectOne(queryWrapper);
    }

    @Override
    public boolean addOne(RedPacketConfigPO redPacketConfigPO) {
        redPacketConfigPO.setConfigCode(UUID.randomUUID().toString());
        return redPacketConfigMapper.insert(redPacketConfigPO) > 0;
    }

    @Override
    public boolean updateById(RedPacketConfigPO redPacketConfigPO) {
        return redPacketConfigMapper.updateById(redPacketConfigPO) > 0;
    }

    @Override
    public boolean prepareRedPacket(Long anchorId) {
        //防止重复生成，以及错误参数传递情况
        RedPacketConfigPO configPO = this.queryByAnchorId(anchorId);
        if (configPO == null) {
            return false;
        }
        boolean lockStatus = redisTemplate.opsForValue().setIfAbsent(cacheKeyBuilder.buildRedPacketInitLock(configPO.getConfigCode()), 1, 3, TimeUnit.SECONDS);
        if (!lockStatus) {
            return false;
        }
        Integer totalCount = configPO.getTotalCount();
        Integer totalPrice = configPO.getTotalPrice();
        String code = configPO.getConfigCode();
        List<Integer> priceList = this.createRedPacketPriceList(totalPrice, totalCount);
        String cacheKey = cacheKeyBuilder.buildRedPacketList(code);
        List<List<Integer>> splitPriceList = ListUtils.splistList(priceList, 100);
        for (List<Integer> priceItemList : splitPriceList) {
            redisTemplate.opsForList().leftPushAll(cacheKey, priceItemList.toArray());
        }
        redisTemplate.expire(cacheKey, 1, TimeUnit.DAYS);
        configPO.setStatus(RedPacketStatusCodeEnum.IS_PREPARE.getCode());
        this.updateById(configPO);
        redisTemplate.opsForValue().set(cacheKeyBuilder.buildRedPacketPrepareSuccess(code), 1, 1, TimeUnit.DAYS);
        return true;
    }

    @Override
    public RedPacketReceiveDTO receiveRedPacket(RedPacketConfigReqDTO reqDTO) {
        String code = reqDTO.getRedPacketConfigCode();
        String cacheKey = cacheKeyBuilder.buildRedPacketList(code);
        Object cacheObj = redisTemplate.opsForList().rightPop(cacheKey);
        if (cacheObj == null) {
            return null;
        }
        Integer price = (Integer) cacheObj;
        LOGGER.info("[receiveRedPacket] code is {},price is {}", code, price);
        SendRedPacketBO sendRedPacketBO = new SendRedPacketBO();
        sendRedPacketBO.setPrice(price);
        sendRedPacketBO.setReqDTO(reqDTO);
        Message message = new Message();
        message.setTopic(GiftProviderTopicNames.RECEIVE_RED_PACKET);
        message.setBody(JSON.toJSONBytes(sendRedPacketBO));
        try {
            SendResult sendResult = mqProducer.send(message);
            if (SendStatus.SEND_OK.equals(sendResult.getSendStatus())) {
                return new RedPacketReceiveDTO(price, "恭喜领取红包" + price + "旗鱼币");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return new RedPacketReceiveDTO(null, "抱歉，红包被人抢走了，再试试？");
    }

    @Override
    public void receiveRedPacketHandle(RedPacketConfigReqDTO reqDTO,Integer price) {
        String code = reqDTO.getRedPacketConfigCode();
        String totalGetPriceCacheKey = cacheKeyBuilder.buildRedPacketTotalGetPrice(code);
        String totalGetCacheKey = cacheKeyBuilder.buildRedPacketTotalGet(code);
        redisTemplate.opsForValue().increment(cacheKeyBuilder.buildUserTotalGetPriceCache(reqDTO.getUserId()), price);
        redisTemplate.opsForValue().increment(totalGetCacheKey);
        redisTemplate.expire(totalGetCacheKey, 1, TimeUnit.DAYS);
        redisTemplate.opsForValue().increment(totalGetPriceCacheKey, price);
        redisTemplate.expire(totalGetPriceCacheKey, 1, TimeUnit.DAYS);
        qiyuCurrencyAccountRpc.incr(reqDTO.getUserId(), price);
        redPacketConfigMapper.incrTotalGetPrice(code,price);
        redPacketConfigMapper.incrTotalGet(code);
    }

    @Override
    public Boolean startRedPacket(RedPacketConfigReqDTO reqDTO) {
        String code = reqDTO.getRedPacketConfigCode();
        if (!redisTemplate.hasKey(cacheKeyBuilder.buildRedPacketPrepareSuccess(code))) {
            return false;
        }
        String notifySuccessCache = cacheKeyBuilder.buildRedPacketNotify(code);
        if (redisTemplate.hasKey(notifySuccessCache)) {
            return false;
        }
        RedPacketConfigPO configPO = this.queryByConfigCode(code);
        //广播im事件
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("redPacketConfig", JSON.toJSONString(configPO));
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(reqDTO.getRoomId());
        livingRoomReqDTO.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        List<Long> userIdList = livingRoomRpc.queryUserIdByRoomId(livingRoomReqDTO);
        if (CollectionUtils.isEmpty(userIdList)) {
            return false;
        }
        this.batchSendImMsg(userIdList, ImMsgBizCodeEnum.START_RED_PACKET, jsonObject);
        configPO.setStatus(RedPacketStatusCodeEnum.HAS_SEND.getCode());
        this.updateById(configPO);
        redisTemplate.opsForValue().set(notifySuccessCache, 1, 1, TimeUnit.DAYS);
        return true;
    }

    /**
     * 批量发送im消息
     *
     * @param userIdList
     * @param imMsgBizCodeEnum
     * @param jsonObject
     */
    private void batchSendImMsg(List<Long> userIdList, ImMsgBizCodeEnum imMsgBizCodeEnum, JSONObject jsonObject) {
        List<ImMsgBody> imMsgBodies = userIdList.stream().map(userId -> {
            ImMsgBody imMsgBody = new ImMsgBody();
            imMsgBody.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
            imMsgBody.setBizCode(imMsgBizCodeEnum.getCode());
            imMsgBody.setUserId(userId);
            imMsgBody.setData(jsonObject.toJSONString());
            return imMsgBody;
        }).collect(Collectors.toList());
        imRouterRpc.batchSendMsg(imMsgBodies);
    }

    /**
     * 生成红包金额List集合数据
     *
     * @param totalPrice
     * @param totalCount
     */
    private List<Integer> createRedPacketPriceList(Integer totalPrice, Integer totalCount) {
        List<Integer> redPacketPriceList = new ArrayList<>(totalCount);
        for (int i = 0; i < totalCount; i++) {
            //如果是最后一个红包
            if (totalCount == i + 1) {
                redPacketPriceList.add(totalPrice);
                break;
            }
            int maxLimit = ((totalPrice / (totalCount - i)) * 2);
            int currentPrice = ThreadLocalRandom.current().nextInt(1, maxLimit);
            totalPrice -= currentPrice;
            redPacketPriceList.add(currentPrice);
        }
        return redPacketPriceList;
    }

}
