package com.alodiga.services.provider.ejb;

import com.alodiga.services.provider.commons.ejbs.AutomaticProcessControlTimerEJB;
import com.alodiga.services.provider.commons.ejbs.AutomaticProcessControlTimerEJBLocal;
import com.alodiga.services.provider.commons.ejbs.AutomaticProcessTimerEJB;
import com.alodiga.services.provider.commons.ejbs.AutomaticProcessTimerEJBLocal;
import com.alodiga.services.provider.commons.ejbs.TransactionEJBLocal;
import com.alodiga.services.provider.commons.genericEJB.AbstractSPEJB;
import com.alodiga.services.provider.commons.genericEJB.SPContextInterceptor;
import com.alodiga.services.provider.commons.genericEJB.SPLoggerInterceptor;
import com.alodiga.services.provider.commons.utils.EjbConstants;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import org.apache.log4j.Logger;

@Interceptors({SPLoggerInterceptor.class, SPContextInterceptor.class})
@Stateless(name = EjbConstants.AUTOMATIC_PROCESS_CONTROL_TIMER_EJB, mappedName = EjbConstants.AUTOMATIC_PROCESS_CONTROL_TIMER_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class AutomaticProcessControlTimerEJBImp extends AbstractSPEJB implements AutomaticProcessControlTimerEJB, AutomaticProcessControlTimerEJBLocal {

	 private static final Logger logger = Logger.getLogger(AutomaticProcessControlTimerEJBImp.class);
	    @EJB
	    private TransactionEJBLocal transactionEJBLocal;
	    @Resource
	    private SessionContext ctx;
	    Calendar initialExpiration;
	    private Long timeoutInterval = 0L;

	    private void cancelTimers() {
	        try {
	            if (ctx.getTimerService() != null) {
	                Collection<Timer> timers = ctx.getTimerService().getTimers();
	                if (timers != null) {
	                    for (Timer timer : timers) {
	                        timer.cancel();
	                    }
	                }
	            }
	        } catch (Exception e) {
	            //
	        }
	    }

	    private void createTimer() {
	        ctx.getTimerService().createTimer(initialExpiration.getTime(), timeoutInterval, EjbConstants.AUTOMATIC_PROCESS_TIMER_EJB);
	    }

	    @Timeout
	    public void execute(Timer timer) {
	        try {
	            logger.info("[AutomaticProcessControlTimerEJB] Ejecutando");
	            System.out.println("[AutomaticProcessControlTimerEJB] Ejecutando");
	            executeUpdate();
	            stop();
	            start();
	        } catch (Exception e) {
	            logger.error("Error", e);
	        }
	    }

	    private void executeUpdate() throws Exception {

	        try {
	            transactionEJBLocal.runAutomaticProcessMetrologicalControl();
	            logger.info("[AutomaticProcessControlTimerEJB] Ejecutado la actualización");
	            System.out.println("[AutomaticProcessControlTimerEJB] Ejecutado la actualización");
	        } catch (Exception ex) {
	            ex.printStackTrace();
	        }
	    }

	    public void forceExecution() throws Exception {
	        logger.info("Ejecutó forceExecution!!!!!!!!");
	        System.out.println("Ejecutó forceExecution!!!!!!!!");
	    }

	    public void forceTimeout() throws Exception {
	        logger.info("[AutomaticProcessControlTimerEJB] Forzando timeout para dentro de 1 minuto");
	        System.out.println("[AutomaticProcessControlTimerEJB] Forzando timeout para dentro de 1 minuto");
	        cancelTimers();
	        setTimeoutInterval();
	        initialExpiration = Calendar.getInstance();
	        initialExpiration.add(Calendar.MINUTE, 1);
	        createTimer();
	    }

	    public Date getNextExecutionDate() {
	        if (ctx.getTimerService() != null) {
	            Collection<Timer> timers = ctx.getTimerService().getTimers();
	            if (timers != null) {
	                for (Timer timer : timers) {
	                    return timer.getNextTimeout();
	                }
	            }
	        }

	        return null;
	    }

	    public void restart() throws Exception {
	        stop();
	        start();
	        logger.info("[AutomaticProcessControlTimerEJB] Reiniciado");
	        System.out.println("[AutomaticProcessControlTimerEJB] Reiniciado");
	    }

	    private void setTimeoutInterval() throws Exception {

	        initialExpiration = Calendar.getInstance();
	        initialExpiration.set(Calendar.HOUR, 1);
	        initialExpiration.set(Calendar.MINUTE, 40);
	        initialExpiration.set(Calendar.SECOND, 0);
	        initialExpiration.set(Calendar.MILLISECOND, 0);
	        initialExpiration.set(Calendar.AM_PM, Calendar.PM);
	        Long secondsInDay = 86400L;
	        secondsInDay = secondsInDay * 1;//Cada un dia
//	        initialExpiration.add(Calendar.DAY_OF_MONTH, 1);//El timer comienza un día despues que se inicializa.
	        timeoutInterval = secondsInDay * 1000L;//Milisegundos
	    }

	    @SuppressWarnings("unchecked")
	    public void start() throws Exception {
	        setTimeoutInterval();
	        createTimer();
	        logger.info("[AutomaticProcessControlTimerEJB] Iniciado");
	        System.out.println("AutomaticProcessControlTimerEJB] Iniciado");
	    }

	    @SuppressWarnings("unchecked")
	    public void stop() throws Exception {
	        cancelTimers();
	        logger.info("[AutomaticProcessControlTimerEJB] Detenido");
	        System.out.println("[AutomaticProcessControlTimerEJB] Detenido");
	    }

	    public Long getTimeoutInterval() {
	        return timeoutInterval;
	    }
	
}
