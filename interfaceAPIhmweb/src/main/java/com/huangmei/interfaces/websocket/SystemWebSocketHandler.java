package com.huangmei.interfaces.websocket;

import com.huangmei.commonhm.model.User;
import com.huangmei.commonhm.util.CommonError;
import com.huangmei.commonhm.util.JsonResultY;
import com.huangmei.commonhm.util.PidValue;
import com.huangmei.interfaces.websocket.Mapping.ActionRouter;
import com.huangmei.interfaces.websocket.Mapping.LoginResource;
import com.huangmei.interfaces.websocket.Mapping.RouterHelper;
import net.sf.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

import java.lang.reflect.Method;


@Component
public class SystemWebSocketHandler implements WebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(SystemWebSocketHandler.class);

    @Autowired
    private ActionRouter actionRouter;

    @Autowired
    private SessionManager sessionManager;

    @Autowired
    private MessageManager messageManager;

    public SystemWebSocketHandler() {
        super();
    }

    // 连接关闭
    public void afterConnectionClosed(WebSocketSession session,
                                      CloseStatus closeStatus) throws Exception {
        log.debug("webSocket接关闭[sessionId={}] 关闭原因[{}]",
                session.getId(),
                closeStatus.toString());
        actionRouter.dealDisconnection(session);
        sessionManager.connectionClosed(session);
    }


    // 连接上
    public void afterConnectionEstablished(WebSocketSession session)
            throws Exception {
        log.debug("新建立webSocket[sessionId={}]", session.getId());
        sessionManager.addSession(session);
        // 处理离线消息

    }

    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {

        String clientMessage = (String) message.getPayload();

        JsonResultY jsonResultY = null;
        int pid = -1;
        try {
            // 请求参数解析
            JSONObject jsonObject = JSONObject.fromObject(clientMessage);
            JSONObject data = JSONObject.fromObject(jsonObject.get("data"));

            pid = jsonObject.getInt("pid");
            if (pid != PidValue.HEARTBEAT.getPid()) {
                log.info("收到客户端[sessionId={}]消息:{}", session.getId(), message.getPayload());
            }
            Method m = RouterHelper.from(pid);

            //用户登录检查，如果是需要登录才能访问的资源，但是客户端未登录，则不允许继续访问业务逻辑
            checkLogin(m, session);

            //通过了登录检查，继续执行逻辑
            Object result = m.invoke(actionRouter, session, data);
            if (result != null) {
                if (result instanceof JsonResultY) {
                    jsonResultY = (JsonResultY) result;
                } else {
                    throw new RuntimeException(String.format("%s方法返回值不支持！请修改！", m.getName()));
                }
            }
        } catch (Exception e) {
            log.info("收到客户端[sessionId={}]消息:{}", session.getId(), message.getPayload());
            log.error(e.getMessage(), e);
            CommonError error = parse(e);
            if (error != null) {
                jsonResultY = new JsonResultY.Builder()
                        .setPid(pid)
                        .setError(error)
                        .build();
            } else {
                jsonResultY = new JsonResultY.Builder()
                        .setPid(pid)
                        .setCode(CommonError.SYS_ERR.getCode())
                        .setData(String.format("系统异常--%s)",
                                e.getCause() == null ? e.getMessage() : e.getCause().getMessage()))
                        .build();
            }
        }

        // 响应客户端
        if (jsonResultY != null) {
            messageManager.send(session, jsonResultY);
        }
    }

    /**
     * 用户登录检查
     * 思路：判断method有没有LoginResource注解
     * 如果没有LoginResource注解，则放行，执行具体的方法
     * 如果有LoginResource注解，则判断webSocketSession的id有没有对应的user
     * 如果有对应的user，则证明已登录。如果没有user，则返回未登录的信息
     */
    private void checkLogin(Method method, WebSocketSession session) {
        if (method.isAnnotationPresent(LoginResource.class)) {
            User user = sessionManager.getUser(session.getId());
            if (user == null) {
                throw CommonError.USER_UnLogin.newException();
            }
        }
    }

    /**
     * 解析异常是否为自定义的异常
     */
    private CommonError parse(Throwable e) {
        CommonError error = CommonError.exceptions.get(
                e.getClass().getSimpleName());
        if (error == null && e.getCause() != null) {
            error = CommonError.exceptions.get(
                    e.getCause().getClass().getSimpleName());
        }
        return error;
    }

    public void handleTransportError(WebSocketSession session,
                                     Throwable exception) throws Exception {
        if (session.isOpen()) {
            session.close();
        }
        log.error("webSocket错误断开[sessionId={}]", session.getId());
        log.error(exception.getMessage(), exception);
        sessionManager.connectionClosed(session);
    }

    public boolean supportsPartialMessages() {
        return false;
    }

}
