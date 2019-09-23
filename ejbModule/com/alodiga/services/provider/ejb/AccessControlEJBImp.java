package com.alodiga.services.provider.ejb;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.alodiga.services.provider.commons.ejbs.AccessControlEJB;
import com.alodiga.services.provider.commons.ejbs.AccessControlEJBLocal;
import com.alodiga.services.provider.commons.ejbs.UserEJBLocal;
import com.alodiga.services.provider.commons.exceptions.DisabledUserException;
import com.alodiga.services.provider.commons.exceptions.EmptyListException;
import com.alodiga.services.provider.commons.exceptions.GeneralException;
import com.alodiga.services.provider.commons.exceptions.NullParameterException;
import com.alodiga.services.provider.commons.exceptions.RegisterNotFoundException;
import com.alodiga.services.provider.commons.genericEJB.AbstractSPEJB;
import com.alodiga.services.provider.commons.genericEJB.EJBRequest;
import com.alodiga.services.provider.commons.genericEJB.SPContextInterceptor;
import com.alodiga.services.provider.commons.genericEJB.SPLoggerInterceptor;
import com.alodiga.services.provider.commons.models.Permission;
import com.alodiga.services.provider.commons.models.PermissionHasProfile;
import com.alodiga.services.provider.commons.models.ProductSerie;
import com.alodiga.services.provider.commons.models.Profile;
import com.alodiga.services.provider.commons.models.ProfileData;
import com.alodiga.services.provider.commons.models.TransactionType;
import com.alodiga.services.provider.commons.models.User;
import com.alodiga.services.provider.commons.utils.EjbConstants;
import com.alodiga.services.provider.commons.utils.EjbUtils;
import com.alodiga.services.provider.commons.utils.QueryConstants;

@Interceptors({SPLoggerInterceptor.class, SPContextInterceptor.class})
@Stateless(name = EjbConstants.ACCESS_CONTROL_EJB, mappedName = EjbConstants.ACCESS_CONTROL_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class AccessControlEJBImp extends AbstractSPEJB implements AccessControlEJB, AccessControlEJBLocal {

    private static final Logger logger = Logger.getLogger(AccessControlEJBImp.class);
    @EJB
    private UserEJBLocal userEJB;
    private static AccessControlEJB accessEjb = null;

    public void deletePermissionHasProfile(Long profileId) throws NullParameterException, GeneralException {
        if (profileId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "profileId"), null);
        }
        Map<String, Long> params = new HashMap<String, Long>();
        params.put(QueryConstants.PARAM_PROFILE_ID, profileId);
        try {
            executeNameQuery(PermissionHasProfile.class, QueryConstants.DELETE_PERMISSION_HAS_PROFILE, params, getMethodName(), logger, "Profile", null, null);
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
    }

//    private Integer getActualValue(PreferenceField preferenceField) {
//        List<PreferenceValue> preferenceValues = preferenceField.getPreferenceValue();
//        for (PreferenceValue preferenceValue : preferenceValues) {
//            if (preferenceValue.getEndingDate() == null) {
//                return Integer.parseInt(preferenceValue.getValue());
//            }
//        }
//        return 0;
//    }

    public List<Profile> getParentsByProfile(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Profile> profiles = null;
        Map<String, Object> params = request.getParams();
        if (!params.containsKey(QueryConstants.PARAM_PROFILE_ID)) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_PROFILE_ID), null);
        }
        Query query = null;
        try {
            query = createQuery("SELECT php.parent FROM ProfileHasProfile php WHERE php.child.id = ?1 AND php.endingDate IS NULL");
            query.setParameter("1", params.get(QueryConstants.PARAM_PROFILE_ID));
            if (request.getLimit() != null && request.getLimit() > 0) {
                query.setMaxResults(request.getLimit());
            }
            profiles = query.setHint("toplink.refresh", "true").getResultList();

        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (profiles.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return profiles;
    }

    public List<Permission> getPermissions(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Permission> permissions = (List<Permission>) listEntities(Permission.class, request, logger, getMethodName());
        return permissions;
    }

    public List<Profile> getProfiles(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Profile> profiles = (List<Profile>) listEntities(Profile.class, request, logger, getMethodName());
        return profiles;
    }

    public Permission loadPermission(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Permission permission = (Permission) loadEntity(Permission.class, request, logger, getMethodName());

        return permission;
    }

    public Profile loadProfile(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Profile profile = (Profile) loadEntity(Profile.class, request, logger, getMethodName());

        return profile;
    }

    public void logginFailed(EJBRequest request) throws NullParameterException, GeneralException, RegisterNotFoundException {
        Object o = request.getParam();
        if (o == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_CUSTOMER_ID + " - " + QueryConstants.PARAM_DISTRIBUTOR_ID + " - " + QueryConstants.PARAM_USER_ID), null);
        }

    }

    public void logginSuccessful(EJBRequest request) throws NullParameterException, GeneralException, RegisterNotFoundException {
    }

    public Permission savePermission(EJBRequest request) throws NullParameterException, GeneralException {
        return (Permission) saveEntity(request, logger, getMethodName());
    }

    public Profile saveProfile(EJBRequest request) throws NullParameterException, GeneralException {
        return (Profile) saveEntity(request, logger, getMethodName());
    }

    public ProfileData saveProfileData(EJBRequest request) throws NullParameterException, GeneralException {
        return (ProfileData) saveEntity(request, logger, getMethodName());

    }

    public boolean validateLoginPreferences(EJBRequest request) throws NullParameterException, GeneralException, RegisterNotFoundException {
        Object o = request.getParam();
        if (o == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_CUSTOMER_ID + " - " + QueryConstants.PARAM_DISTRIBUTOR_ID + " - " + QueryConstants.PARAM_USER_ID), null);
        }
        return false;
    }

    public User validateUser(String login, String password) throws RegisterNotFoundException, NullParameterException, GeneralException, DisabledUserException {
        User user = null;


        if (login == null || login.equals("")) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_LOGIN), null);
        }
        if (password == null || password.equals("")) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_PASSWORD), null);
        }

        try {
            user = userEJB.loadUserByLogin(login);
            if (user != null && !user.getPassword().equals(password)) {
                user = null;
                throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), null);
            }

        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), ex);
        } catch (RegisterNotFoundException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), ex);
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "user"), ex);
        }
        if (!user.getEnabled()) {
            throw new DisabledUserException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName(), "distributor enabled"), null);
        }

        return user;
    }


    public List<User> getUsersWithParams(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<User> users = null;
        Map<String, Object> params = request.getParams();
	    StringBuilder sqlBuilder = new StringBuilder("SELECT s FROM User s WHERE s.enabled = 1");
	    if (params.containsKey(QueryConstants.PARAM_LOGIN)) {
	        sqlBuilder.append(" AND s.login=").append(params.get(QueryConstants.PARAM_LOGIN));
	    }

	    Query query = null;
	    try {
	        System.out.println("query:********"+sqlBuilder.toString());
	        query = createQuery(sqlBuilder.toString());
	        if (request.getLimit() != null && request.getLimit() > 0) {
	            query.setMaxResults(request.getLimit());
	        }
	        users = query.setHint("toplink.refresh", "true").getResultList();
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
	    }
	    if (users.isEmpty()) {
	        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
	    }
        
        return users;
    }
}
