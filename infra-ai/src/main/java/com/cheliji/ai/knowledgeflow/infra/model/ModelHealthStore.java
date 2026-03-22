package com.cheliji.ai.knowledgeflow.infra.model;

import com.cheliji.ai.knowledgeflow.infra.config.AIModelProperties;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * 模型健康状态
 * 用于模型熔断操作，防止一个模型出错，
 * 无降级熔断措施，导致整个服务出现雪崩
 */
@Component
@RequiredArgsConstructor
public class ModelHealthStore {

    private final AIModelProperties properties ;
    private final Map<String,ModelHealth> healthMap = new ConcurrentHashMap<>();

    /**
     * 判断模型是否为熔断状态
     */
    public boolean isOpen(String id) {
        ModelHealth modelHealth = healthMap.get(id);
        if (modelHealth == null) {
            return false ;
        }
        return modelHealth.state == State.OPEN && modelHealth.openUntil > System.currentTimeMillis();
    }

    /**
     * 判断是否允许发起调用
     */
    public boolean allowCall(String id) {
        if (id == null)
            return false ;

        long now = System.currentTimeMillis();

        final boolean[] allowed = {false};

        healthMap.compute(id,(k,v) -> {
            if (v == null) {
                v = new ModelHealth();
                return v ;
            }


            if (v.state == State.OPEN) {
                // 如果模型熔断打开并没有到达熔断解除时间
                if (v.openUntil > now) {
                    allowed[0] = false ;
                    return v ;
                }

                // 如果达到了熔断解除时间，则讲 state 变更为 HALF_OPEN
                v.state = State.HALF_OPEN ;
                v.halfOpenInFlight = true ;
                // v.openUntil = 0 ;
                allowed[0] = true ;

                return v ;

            }

            if(v.state == State.HALF_OPEN) {
                // 半开熔断已有一个请求进行中,直接返回
                if (v.halfOpenInFlight) {
                    return v ;
                }

                v.halfOpenInFlight = true ;
                allowed[0] = true ;

                return v ;
            }
            // 剩下的就是熔断关闭状态
            allowed[0] = true ;
            return v ;
        }) ;

        return allowed[0] ;

    }

    // 关闭模型熔断状态（模型调用成功都关闭熔断）
    public void markSuccess (String id) {
        if(id == null)
            return ;
        healthMap.compute(id,(k,v) -> {
            if (v == null) {
                v = new ModelHealth();
                return v ;
            }
            v.state = State.CLOSED;
            v.halfOpenInFlight = false ;
            v.openUntil = 0 ;
            v.consecutiveFailures = 0 ;

            return v ;

        }) ;
    }

    // 模型调用失败，修改模型健康状态
    public void markFail (String id) {

        if (id == null) return ;

        long now = System.currentTimeMillis();

        healthMap.compute(id ,(k,v) -> {
            if (v == null) {
                v = new ModelHealth();
                // 这里也可以不加，但严格意义来讲还是要加一下
                v.consecutiveFailures ++ ;
                return v ;
            }


            // 如果熔断状态为关闭，判断错误次数
            if (v.state == State.CLOSED) {
                if (v.consecutiveFailures + 1 < properties.getSelection().getFailureThreshold()) {
                    v.consecutiveFailures ++ ;
                } else {
                    v.state = State.OPEN ;
                    v.openUntil = now + properties.getSelection().getOpenDurationsMs() ;
                    v.consecutiveFailures = 0 ;
                }
                return v ;
            }

            if (v.state == State.HALF_OPEN) {
                v.state = State.OPEN ;
                v.openUntil = now + properties.getSelection().getOpenDurationsMs() ;
                v.consecutiveFailures = 0;
                v.halfOpenInFlight = false;
                return v ;
            }

            return v ;


        }) ;


    }



    public static class ModelHealth {

        // 调用失败次数
        private int consecutiveFailures ;

        // 熔断关闭时间
        private long openUntil ;

        // 是否为半开状态（用于保证半开状态下只有一个请求调用模型）
        private boolean halfOpenInFlight ;

        // 模型熔断状态 分为 CLOSED, OPEN, HALF_OPEN
        private State state ;

        public ModelHealth () {
            this.consecutiveFailures = 0;
            this.openUntil = 0L ;
            this.halfOpenInFlight = false ;
            this.state = State.CLOSED ;
        }


    }

    public enum State {
        CLOSED ,
        OPEN ,
        HALF_OPEN
    }


}
