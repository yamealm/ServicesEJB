package com.alodiga.services.provider.ejb;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import org.apache.log4j.Logger;
import com.alodiga.services.provider.commons.ejbs.AuditoryEJB;
import com.alodiga.services.provider.commons.ejbs.AuditoryEJBLocal;
import com.alodiga.services.provider.commons.exceptions.EmptyListException;
import com.alodiga.services.provider.commons.exceptions.GeneralException;
import com.alodiga.services.provider.commons.exceptions.NullParameterException;
import com.alodiga.services.provider.commons.exceptions.RegisterNotFoundException;
import com.alodiga.services.provider.commons.genericEJB.AbstractSPEJB;
import com.alodiga.services.provider.commons.genericEJB.EJBRequest;
import com.alodiga.services.provider.commons.genericEJB.SPContextInterceptor;
import com.alodiga.services.provider.commons.genericEJB.SPLoggerInterceptor;
import com.alodiga.services.provider.commons.models.Audit;
import com.alodiga.services.provider.commons.models.AuditAction;
import com.alodiga.services.provider.commons.models.Braund;
import com.alodiga.services.provider.commons.models.Customer;
import com.alodiga.services.provider.commons.models.Event;
import com.alodiga.services.provider.commons.models.MetrologicalControl;
import com.alodiga.services.provider.commons.models.Model;
import com.alodiga.services.provider.commons.models.Product;
import com.alodiga.services.provider.commons.models.Profile;
import com.alodiga.services.provider.commons.models.Provider;
import com.alodiga.services.provider.commons.models.User;
import com.alodiga.services.provider.commons.utils.EjbConstants;
import com.alodiga.services.provider.commons.utils.EjbUtils;
import com.alodiga.services.provider.commons.utils.GeneralUtils;
import com.alodiga.services.provider.commons.utils.QueryConstants;

import java.util.ArrayList;
import java.util.Date;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

@Interceptors({SPLoggerInterceptor.class, SPContextInterceptor.class})
@Stateless(name = EjbConstants.AUDITORY_EJB, mappedName = EjbConstants.AUDITORY_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class AuditoryEJBImp extends AbstractSPEJB implements AuditoryEJB, AuditoryEJBLocal {

    private static final Logger logger = Logger.getLogger(AuditoryEJBImp.class);

    public List<Audit> getLastAudits(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        List<Audit> audits = null;
        String nameQuery = null;
        nameQuery = QueryConstants.LOAD_LAST_AUDITS;
        audits = (List<Audit>) getNamedQueryResult(Audit.class, nameQuery, request, getMethodName(), logger, "audits");
        return audits;

    }

    public List<Audit> getAudits(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        return (List<Audit>) listEntities(Audit.class, request, logger, getMethodName());
    }

    public Audit loadAudit(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {
        return (Audit) loadEntity(Audit.class, request, logger, getMethodName());
    }

    public Audit saveAudit(EJBRequest request) throws GeneralException, NullParameterException {
        return (Audit) saveEntity(request, logger, getMethodName());
    }

    public List<Audit> searchAudit(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException {
        List<Audit> audits = new ArrayList<Audit>();
        Map<String, Object> params = request.getParams();
        String sql = "SELECT a FROM Audit a WHERE a.creationDate BETWEEN :date1 AND :date2";

        if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        if (params.containsKey(QueryConstants.PARAM_RESPONSIBLE_ID)) {
            sql += " AND a.user.id =" + params.get(QueryConstants.PARAM_USER_ID);
        }
        if (params.containsKey(QueryConstants.PARAM_RESPONSIBLE_TYPE)) {
            sql += " AND a.responsibleType LIKE '%" + params.get(QueryConstants.PARAM_RESPONSIBLE_TYPE) + "%'";
        }
        if (params.containsKey(QueryConstants.PARAM_TABLE_NAME)) {
            sql += " AND a.tableName LIKE '%" + params.get(QueryConstants.PARAM_TABLE_NAME) + "%'";
        }
        if (params.containsKey(QueryConstants.PARAM_REMOTE_IP)) {
            sql += " AND a.remoteIp LIKE '%" + params.get(QueryConstants.PARAM_REMOTE_IP) + "%'";
        }
        if (params.containsKey(QueryConstants.PARAM_EVENT_ID)) {
            sql += " AND a.event.id =" + params.get(QueryConstants.PARAM_EVENT_ID);
        }
        Query query = null;
        try {
            query = createQuery(sql);
            query.setParameter("date1", EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE)));
            query.setParameter("date2", EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE)));

            if (request.getLimit() != null && request.getLimit() > 0) {
                query.setMaxResults(request.getLimit());
            }
            audits = query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;

    }
    public AuditAction deleteAuditAction(Long actionId) throws GeneralException, NullParameterException {
        return null;
    }

    public List<AuditAction> getAuditActions(Date beginningDate, Date endingDate) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        List<AuditAction> audits = new ArrayList<AuditAction>();
        if (beginningDate == null || beginningDate == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        Query query = null;
        try {
            query = createQuery("SELECT a FROM AuditAction a WHERE a.date BETWEEN :date1 AND :date2");
            query.setParameter("date1", GeneralUtils.getBeginningDate(beginningDate));
            query.setParameter("date2", GeneralUtils.getEndingDate(endingDate));
            audits = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;
    }


     public List<AuditAction> searchAuditAction(String login, String userName, Long permissionId, Date beginningDate, Date endingDate) throws GeneralException, NullParameterException, EmptyListException {
        List<AuditAction> audits = new ArrayList<AuditAction>();

        if (beginningDate == null || beginningDate == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT a FROM AuditAction a WHERE a.date BETWEEN :date1 AND :date2");

        if (login != null) {
            sqlBuilder.append(" AND a.user.login=").append(login);
        }
        if (userName != null && !userName.equals("")) {
            sqlBuilder.append(" AND a.user.firstName=").append(userName);
        }
        if (permissionId != null) {
            sqlBuilder.append(" AND a.permission.id=").append(permissionId);
        }
        Query query = null;
        try {
            query = createQuery(sqlBuilder.toString());
            query.setParameter("date1", GeneralUtils.getBeginningDate(beginningDate));
            query.setParameter("date2", GeneralUtils.getEndingDate(endingDate));
            audits = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;
    }

    public AuditAction saveAuditAction(AuditAction action) throws GeneralException, NullParameterException {
        return (AuditAction) saveEntity(action);
    }
    
    @Override
    public Event loadEvent(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
    	Event event = (Event) loadEntity(Event.class, request, logger, getMethodName());
        return event;
    }

    public List<AuditAction> getAuditActionsByUserId(Long userId, Date beginningDate, Date endingDate) throws GeneralException, RegisterNotFoundException, NullParameterException, EmptyListException {
        List<AuditAction> audits = new ArrayList<AuditAction>();

        if (userId == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "userId"), null);
        } else if (beginningDate == null || beginningDate == null) {
            throw new NullParameterException( sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
        }
        Query query = null;
        try {
            query = createQuery("SELECT a FROM AuditAction a WHERE a.date BETWEEN :date1 AND :date2 AND a.user.id=?1");
            query.setParameter("1", userId);
            query.setParameter("date1", GeneralUtils.getBeginningDate(beginningDate));
            query.setParameter("date2", GeneralUtils.getEndingDate(endingDate));
            audits = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (audits.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return audits;
    }
    
    @Override
    public String getNaturalFieldProfile(EJBRequest request1, EJBRequest request2) throws NullParameterException, GeneralException
    {
        Profile rolOld = (Profile) request1.getParam();
        Profile rolNew = (Profile) request2.getParam();
        String result = "";

        if (rolOld == null || rolNew == null) {
            logger.error("Parameters must not be null in method getNaturalFieldRol.");
            throw new NullParameterException("Parameters  must not be null.");
        }
        try {
            Profile rol = new Profile();
            result = rol.getNaturalField(rolOld, rolNew);
        } catch (Exception e) {
            throw new GeneralException("General Error in method getNaturalFieldRol.");
        }

        if (result == null || result.isEmpty()) {
            logger.error("Empty Result Exception in method getNaturalFieldRol.");
            throw new GeneralException("Empty Result in method getNaturalFieldRol.");
        }
        return result;
    }

    @Override
    public String getNaturalFieldUser(EJBRequest request1, EJBRequest request2) throws NullParameterException, GeneralException
    {
        User userOld = (User) request1.getParam();
        User userNew = (User) request2.getParam();
        String result = "";

        if (userOld == null || userNew == null) {
            logger.error("Parameters must not be null in method getNaturalFieldUser.");
            throw new NullParameterException("Parameters  must not be null.");
        }
        try {
            User user = new User();
            result = user.getNaturalField(userOld, userNew);
        } catch (Exception e) {
            throw new GeneralException("General Error in method getNaturalFieldUser.");
        }

        if (result == null || result.isEmpty()) {
            logger.error("Empty Result Exception in method getNaturalFieldUser.");
            throw new GeneralException("Empty Result in method getNaturalFieldUser.");
        }
        return result;
    }
    
    public String getNaturalFieldMetrologicalControl(EJBRequest request1, EJBRequest request2) throws NullParameterException, GeneralException{
    	MetrologicalControl controlOld = (MetrologicalControl) request1.getParam();
    	MetrologicalControl controlNew = (MetrologicalControl) request2.getParam();
        String result = "";

        if (controlOld == null || controlNew == null) {
            logger.error("Parameters must not be null in method getNaturalFieldRol.");
            throw new NullParameterException("Parameters  must not be null.");
        }
        try {
        	MetrologicalControl rol = new MetrologicalControl();
            result = rol.getNaturalField(controlOld, controlNew);
        } catch (Exception e) {
            throw new GeneralException("General Error in method getNaturalFieldRol.");
        }

        if (result == null || result.isEmpty()) {
            logger.error("Empty Result Exception in method getNaturalFieldMetrologicalControl.");
            throw new GeneralException("Empty Result in method getNaturalFieldMetrologicalControl.");
        }
        return result;
    }
    
    public String getNaturalFieldCustomer(EJBRequest request1, EJBRequest request2) throws NullParameterException, GeneralException{
    	Customer controlOld = (Customer) request1.getParam();
    	Customer controlNew = (Customer) request2.getParam();
        String result = "";

        if (controlOld == null || controlNew == null) {
            logger.error("Parameters must not be null in method getNaturalFieldRol.");
            throw new NullParameterException("Parameters  must not be null.");
        }
        try {
        	Customer rol = new Customer();
            result = rol.getNaturalField(controlOld, controlNew);
        } catch (Exception e) {
            throw new GeneralException("General Error in method getNaturalFieldRol.");
        }

        if (result == null || result.isEmpty()) {
            logger.error("Empty Result Exception in method getNaturalFieldMetrologicalControl.");
            throw new GeneralException("Empty Result in method getNaturalFieldMetrologicalControl.");
        }
        return result;
    }

	@Override
	public String getNaturalFieldBraund(EJBRequest request1, EJBRequest request2)throws NullParameterException, GeneralException {
		Braund controlOld = (Braund) request1.getParam();
		Braund controlNew = (Braund) request2.getParam();
        String result = "";

        if (controlOld == null || controlNew == null) {
            logger.error("Parameters must not be null in method getNaturalFieldRol.");
            throw new NullParameterException("Parameters  must not be null.");
        }
        try {
        	Braund rol = new Braund();
            result = rol.getNaturalField(controlOld, controlNew);
        } catch (Exception e) {
            throw new GeneralException("General Error in method getNaturalFieldRol.");
        }

        if (result == null || result.isEmpty()) {
            logger.error("Empty Result Exception in method getNaturalFieldMetrologicalControl.");
            throw new GeneralException("Empty Result in method getNaturalFieldMetrologicalControl.");
        }
        return result;
	}

	@Override
	public String getNaturalFieldModel(EJBRequest request1, EJBRequest request2) throws NullParameterException, GeneralException{
	Model controlOld = (Model) request1.getParam();
	Model controlNew = (Model) request2.getParam();
    String result = "";

    if (controlOld == null || controlNew == null) {
        logger.error("Parameters must not be null in method getNaturalFieldRol.");
        throw new NullParameterException("Parameters  must not be null.");
    }
    try {
    	Model rol = new Model();
        result = rol.getNaturalField(controlOld, controlNew);
    } catch (Exception e) {
        throw new GeneralException("General Error in method getNaturalFieldRol.");
    }

    if (result == null || result.isEmpty()) {
        logger.error("Empty Result Exception in method getNaturalFieldMetrologicalControl.");
        throw new GeneralException("Empty Result in method getNaturalFieldMetrologicalControl.");
    }
    return result;
	}

	@Override
	public String getNaturalFieldProduct(EJBRequest request1, EJBRequest request2) throws NullParameterException, GeneralException{
	Product controlOld = (Product) request1.getParam();
	Product controlNew = (Product) request2.getParam();
    String result = "";

    if (controlOld == null || controlNew == null) {
        logger.error("Parameters must not be null in method getNaturalFieldRol.");
        throw new NullParameterException("Parameters  must not be null.");
    }
    try {
    	Product product = new Product();
        result = product.getNaturalField(controlOld, controlNew);
    } catch (Exception e) {
        throw new GeneralException("General Error in method getNaturalFieldRol.");
    }

    if (result == null || result.isEmpty()) {
        logger.error("Empty Result Exception in method getNaturalFieldMetrologicalControl.");
        throw new GeneralException("Empty Result in method getNaturalFieldMetrologicalControl.");
    }
    return result;
	}

	@Override
	public String getNaturalFieldProvider(EJBRequest request1, EJBRequest request2) throws NullParameterException, GeneralException{
	Provider controlOld = (Provider) request1.getParam();
	Provider controlNew = (Provider) request2.getParam();
    String result = "";

    if (controlOld == null || controlNew == null) {
        logger.error("Parameters must not be null in method getNaturalFieldRol.");
        throw new NullParameterException("Parameters  must not be null.");
    }
    try {
    	Provider provider = new Provider();
        result = provider.getNaturalField(controlOld, controlNew);
    } catch (Exception e) {
        throw new GeneralException("General Error in method getNaturalFieldRol.");
    }

    if (result == null || result.isEmpty()) {
        logger.error("Empty Result Exception in method getNaturalFieldMetrologicalControl.");
        throw new GeneralException("Empty Result in method getNaturalFieldMetrologicalControl.");
    }
    return result;
	}
    
}
