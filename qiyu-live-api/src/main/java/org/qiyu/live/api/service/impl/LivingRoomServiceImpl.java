package org.qiyu.live.api.service.impl;

import org.apache.dubbo.config.annotation.DubboReference;
import org.qiyu.live.api.error.ApiErrorEnum;
import org.qiyu.live.api.service.ILivingRoomService;

import org.qiyu.live.api.vo.LivingRoomInitVO;
import org.qiyu.live.api.vo.req.LivingRoomReqVO;
import org.qiyu.live.api.vo.req.OnlinePkReqVO;
import org.qiyu.live.api.vo.resp.LivingRoomPageRespVO;
import org.qiyu.live.api.vo.resp.LivingRoomRespVO;
import org.qiyu.live.api.vo.resp.RedPacketReceiveVO;
import org.qiyu.live.common.interfaces.dto.PageWrapper;
import org.qiyu.live.common.interfaces.utils.ConvertBeanUtils;
import org.qiyu.live.gift.dto.RedPacketConfigReqDTO;
import org.qiyu.live.gift.dto.RedPacketConfigRespDTO;
import org.qiyu.live.gift.dto.RedPacketReceiveDTO;
import org.qiyu.live.gift.interfaces.IRedPacketConfigRpc;
import org.qiyu.live.im.constants.AppIdEnum;
import org.qiyu.live.living.interfaces.constants.LivingRoomTypeEnum;
import org.qiyu.live.living.interfaces.dto.LivingPkRespDTO;
import org.qiyu.live.living.interfaces.dto.LivingRoomReqDTO;
import org.qiyu.live.living.interfaces.dto.LivingRoomRespDTO;
import org.qiyu.live.living.interfaces.rpc.ILivingRoomRpc;
import org.qiyu.live.user.dto.UserDTO;
import org.qiyu.live.user.interfaces.IUserRpc;
import org.qiyu.live.web.starter.context.QiyuRequestContext;
import org.qiyu.live.web.starter.error.BizBaseErrorEnum;
import org.qiyu.live.web.starter.error.ErrorAssert;
import org.qiyu.live.web.starter.error.QiyuBaseError;
import org.qiyu.live.web.starter.error.QiyuErrorException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Author kangxi
 * @Date: Created in 21:15 2023/7/19
 * @Description
 */
@Service
public class LivingRoomServiceImpl implements ILivingRoomService {

    @DubboReference
    private IUserRpc userRpc;
    @DubboReference
    private ILivingRoomRpc livingRoomRpc;
    @DubboReference
    private IRedPacketConfigRpc redPacketConfigRpc;

    @Override
    public LivingRoomPageRespVO list(LivingRoomReqVO livingRoomReqVO) {
        PageWrapper<LivingRoomRespDTO> resultPage = livingRoomRpc.list(ConvertBeanUtils.convert(livingRoomReqVO, LivingRoomReqDTO.class));
        LivingRoomPageRespVO livingRoomPageRespVO = new LivingRoomPageRespVO();
        livingRoomPageRespVO.setList(ConvertBeanUtils.convertList(resultPage.getList(), LivingRoomRespVO.class));
        livingRoomPageRespVO.setHasNext(resultPage.isHasNext());
        return livingRoomPageRespVO;
    }

    @Override
    public Integer startingLiving(Integer type) {
        Long userId = QiyuRequestContext.getUserId();
        UserDTO userDTO = userRpc.getByUserId(userId);
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setAnchorId(userId);
        livingRoomReqDTO.setRoomName("主播-" + QiyuRequestContext.getUserId() + "的直播间");
        livingRoomReqDTO.setCovertImg(userDTO.getAvatar());
        livingRoomReqDTO.setType(type);
        return livingRoomRpc.startLivingRoom(livingRoomReqDTO);
    }

    @Override
    public boolean onlinePk(OnlinePkReqVO onlinePkReqVO) {
        LivingRoomReqDTO reqDTO = ConvertBeanUtils.convert(onlinePkReqVO, LivingRoomReqDTO.class);
        reqDTO.setAppId(AppIdEnum.QIYU_LIVE_BIZ.getCode());
        reqDTO.setPkObjId(QiyuRequestContext.getUserId());
        LivingPkRespDTO tryOnlineStatus = livingRoomRpc.onlinePk(reqDTO);
        ErrorAssert.isTure(tryOnlineStatus.isOnlineStatus(), new QiyuErrorException(-1, tryOnlineStatus.getMsg()));
        return true;
    }

    @Override
    public boolean closeLiving(Integer roomId) {
        LivingRoomReqDTO livingRoomReqDTO = new LivingRoomReqDTO();
        livingRoomReqDTO.setRoomId(roomId);
        livingRoomReqDTO.setAnchorId(QiyuRequestContext.getUserId());
        return livingRoomRpc.closeLiving(livingRoomReqDTO);
    }

    @Override
    public LivingRoomInitVO anchorConfig(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
        ErrorAssert.isNotNull(respDTO, ApiErrorEnum.LIVING_ROOM_END);
        Map<Long, UserDTO> userDTOMap = userRpc.batchQueryUserInfo(Arrays.asList(respDTO.getAnchorId(), userId).stream().distinct().collect(Collectors.toList()));
        UserDTO anchor = userDTOMap.get(respDTO.getAnchorId());
        UserDTO watcher = userDTOMap.get(userId);
        LivingRoomInitVO respVO = new LivingRoomInitVO();
        respVO.setAnchorNickName(anchor.getNickName());
        respVO.setWatcherNickName(watcher.getNickName());
        respVO.setUserId(userId);
        //给定一个默认的头像
        respVO.setAvatar(StringUtils.isEmpty(anchor.getAvatar()) ? "https://s1.ax1x.com/2022/12/18/zb6q6f.png" : anchor.getAvatar());
        respVO.setWatcherAvatar(watcher.getAvatar());
        respVO.setDefaultBgImg("https://picst.sunbangyan.cn/2023/08/29/waxzj0.png");
        if (respDTO == null || respDTO.getAnchorId() == null || userId == null) {
            //这种就是属于直播间已经不存在的情况了
            respVO.setRoomId(-1);
            return respVO;
        }
        boolean isAnchor = respDTO.getAnchorId().equals(userId);
        respVO.setAnchorImg(anchor.getAvatar());
        respVO.setRoomId(respDTO.getId());
        respVO.setAnchorId(respDTO.getAnchorId());
        respVO.setAnchor(isAnchor);
        if (isAnchor) {
            RedPacketConfigRespDTO redPacketConfigRespDTO = redPacketConfigRpc.queryByAnchorId(userId);
            if (redPacketConfigRespDTO != null) {
                respVO.setRedPacketConfigCode(redPacketConfigRespDTO.getConfigCode());
            }
        }
        return respVO;
    }

    @Override
    public Boolean prepareRedPacket(Long userId, Integer roomId) {
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByRoomId(roomId);
        ErrorAssert.isNotNull(respDTO, BizBaseErrorEnum.PARAM_ERROR);
        ErrorAssert.isNotNull(respDTO.getAnchorId().equals(userId), BizBaseErrorEnum.PARAM_ERROR);
        try {
            redPacketConfigRpc.prepareRedPacket(userId);
        }catch (Exception e) {
        }
        return true;
    }

    @Override
    public Boolean startRedPacket(Long userId, String code) {
        RedPacketConfigReqDTO reqDTO = new RedPacketConfigReqDTO();
        reqDTO.setUserId(userId);
        reqDTO.setRedPacketConfigCode(code);
        LivingRoomRespDTO respDTO = livingRoomRpc.queryByAnchorId(userId);
        ErrorAssert.isNotNull(respDTO,BizBaseErrorEnum.PARAM_ERROR);
        reqDTO.setRoomId(respDTO.getId());
        return redPacketConfigRpc.startRedPacket(reqDTO);
    }

    @Override
    public RedPacketReceiveVO getRedPacket(Long userId, String code) {
        RedPacketConfigReqDTO reqDTO = new RedPacketConfigReqDTO();
        reqDTO.setUserId(userId);
        reqDTO.setRedPacketConfigCode(code);
        RedPacketReceiveDTO receiveDTO = redPacketConfigRpc.receiveRedPacket(reqDTO);
        RedPacketReceiveVO respVO = new RedPacketReceiveVO();
        if (receiveDTO == null) {
            respVO.setMsg("红包领取活动已结束");
        } else {
            respVO.setPrice(receiveDTO.getPrice());
            respVO.setMsg(receiveDTO.getNotifyMsg());
        }
        return respVO;
    }
}
