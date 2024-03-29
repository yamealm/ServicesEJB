package com.alodiga.services.provider.ejb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.alodiga.services.provider.commons.ejbs.UtilsEJB;
import com.alodiga.services.provider.commons.ejbs.UtilsEJBLocal;
import com.alodiga.services.provider.commons.exceptions.EmptyListException;
import com.alodiga.services.provider.commons.exceptions.GeneralException;
import com.alodiga.services.provider.commons.exceptions.NullParameterException;
import com.alodiga.services.provider.commons.exceptions.RegisterNotFoundException;
import com.alodiga.services.provider.commons.genericEJB.AbstractSPEJB;
import com.alodiga.services.provider.commons.genericEJB.EJBRequest;
import com.alodiga.services.provider.commons.genericEJB.SPContextInterceptor;
import com.alodiga.services.provider.commons.genericEJB.SPLoggerInterceptor;
import com.alodiga.services.provider.commons.models.Braund;
import com.alodiga.services.provider.commons.models.ControlType;
import com.alodiga.services.provider.commons.models.EnterCalibration;
import com.alodiga.services.provider.commons.models.Model;
import com.alodiga.services.provider.commons.models.ProductSerie;
import com.alodiga.services.provider.commons.models.Country;
import com.alodiga.services.provider.commons.models.CountryHasProvider;
import com.alodiga.services.provider.commons.models.CountryTranslation;
import com.alodiga.services.provider.commons.models.Currency;
import com.alodiga.services.provider.commons.models.Enterprise;
import com.alodiga.services.provider.commons.models.EnterpriseHasEmail;
import com.alodiga.services.provider.commons.models.Language;
import com.alodiga.services.provider.commons.models.Provider;
import com.alodiga.services.provider.commons.models.QuarantineStatus;
import com.alodiga.services.provider.commons.models.TinType;
import com.alodiga.services.provider.commons.models.Transaction;
import com.alodiga.services.provider.commons.utils.EjbConstants;
import com.alodiga.services.provider.commons.utils.Mail;
import com.alodiga.services.provider.commons.utils.QueryConstants;
import com.alodiga.services.provider.commons.utils.SendMail;

@Interceptors({SPLoggerInterceptor.class, SPContextInterceptor.class})
@Stateless(name = EjbConstants.UTILS_EJB, mappedName = EjbConstants.UTILS_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class UtilsEJBImp extends AbstractSPEJB implements UtilsEJB, UtilsEJBLocal {

    private static final Logger logger = Logger.getLogger(UtilsEJBImp.class);


    public List<Country> getCountries(EJBRequest request) throws EmptyListException, GeneralException, NullParameterException {
        List<Country> countries = (List<Country>) listEntities(Country.class, request, logger, getMethodName());

        return countries;
    }

     public List<Country> getCountries() throws EmptyListException, GeneralException, NullParameterException {
        List<Country> countries = null;
        Query query = null;
        try {
            query = createQuery("SELECT c FROM Country c ORDER BY c.name");
            countries = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (countries.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return countries;
    }

    public List<Enterprise> getEnterprises() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<Enterprise> enterprises = (List<Enterprise>) listEntities(Enterprise.class, request, logger, getMethodName());

        return enterprises;
    }


    public List<Language> getLanguages() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<Language> languages = (List<Language>) listEntities(Language.class, request, logger, getMethodName());
        return languages;
    }

    public Country loadCountry(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Country country = (Country) loadEntity(Country.class, request, logger, getMethodName());
        return country;
    }

    public Enterprise loadEnterprise(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Enterprise enterprise = (Enterprise) loadEntity(Enterprise.class, request, logger, getMethodName());
        return enterprise;
    }

    public Language loadLanguage(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Language language = (Language) loadEntity(Language.class, request, logger, getMethodName());
        return language;
    }

    public List<TinType> getTinTypes() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<TinType> tinTypes = (List<TinType>) listEntities(TinType.class, request, logger, getMethodName());

        return tinTypes;
    }

    public List<TinType> getTinTypesByEnterprise(Long enterpriseId) throws EmptyListException, GeneralException, NullParameterException {
        List<TinType> tinTypes = null;
        if (enterpriseId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), QueryConstants.PARAM_ENTERPRISE_ID), null);
        }

        Query query = null;
        try {
            query = createQuery("SELECT eht.tinType FROM EnterpriseHasTinType eht WHERE eht.enterprise.id = ?1");
            query.setParameter("1", enterpriseId);
            tinTypes = query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
        if (tinTypes.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return tinTypes;
    }

    public Enterprise saveEnterprise(EJBRequest request) throws NullParameterException, GeneralException {
        return (Enterprise) saveEntity(request, logger, getMethodName());
    }

    public void sendMail(Mail mail) throws GeneralException, NullParameterException {
        SendMail SendMail = new SendMail();
        try {
            SendMail.sendMail(mail);
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), null);
        }
    }

   
     
     public Enterprise loadEnterprisebyId(Long enterpriseId) throws GeneralException {
        List<Enterprise> list = new ArrayList();
       
        try {
            list = entityManager.createQuery("SELECT c FROM Enterprise c WHERE c.id='" + enterpriseId + "'").getResultList();
        } catch (Exception e) {


            logger.error("Exception in method loadEnterprise: Exception text: ", e);
            throw new GeneralException("Exception in method loadEnterprise: Exception text: " + e.getMessage(), e.getStackTrace());
        }
        if (list.isEmpty()) {
            logger.error("Not Enterprise found in method loadEnterprise");
            //throw new EnterpriseNotFoundException("Not Enterprise found in method loadEnterprise");
        }

          return list.get(0);
    }


       
    public Float getTotalAmauntbyTransacction(Long accountId, Timestamp date1, Date date2) throws GeneralException, NullParameterException{
	  Float totalType = null;

        java.sql.Date date = new java.sql.Date(date1.getTime());
        java.sql.Date date3 = new java.sql.Date(date2.getTime());

	   if (accountId == null ) {
           throw new NullParameterException("Parameter AccountId  cant be null ");
       }
	   if (date == null ) {
           throw new NullParameterException("Parameter date cant be null in method getTotalAmauntbyTransacction");
       }
	   if (date3 == null ) {
           throw new NullParameterException("Parameter date2 cant be null in method getTotalAmauntbyTransacction");
       }
 
       try {
           StringBuilder sql = new StringBuilder();
           sql.append("SELECT sum(totalAmount) FROM transaction WHERE accountId= ");
           sql.append(accountId);
           sql.append(" and creationDate BETWEEN '");
           sql.append(date);
           sql.append("' and '");
           sql.append(date3);
           sql.append("'");

           System.out.println(sql.toString());




          
           Query q = entityManager.createNativeQuery(sql.toString());
           if ((q.getSingleResult())!=null){
                totalType =  Float.valueOf( ((Double)((List) q.getSingleResult()).get(0)).toString());
           }
           
 	   
       } catch (Exception e) {
           throw new GeneralException("Exception in method getTotalInvoicebyTypebyAni "+ e.getMessage(), e.getStackTrace());
       }
       return totalType;
   }



   public Float getTotalTaxTransacction(Long accountId, Timestamp date1, Date date2) throws GeneralException, NullParameterException{
       Float totalType = null;
       Date date = new Date(date1.getTime());

	   if (accountId == null ) {
           throw new NullParameterException("Parameter AccountID  cant be null ");
       }
	   if (date == null ) {
           throw new NullParameterException("Parameter date cant be null in method getTotalAmauntbyTransacction");
       }
	   if (date2 == null ) {
           throw new NullParameterException("Parameter date2 cant be null in method getTotalAmauntbyTransacction");
       }

       try {
           StringBuilder sql = new StringBuilder();
           sql.append("SELECT sum(totalTax) FROM transaction WHERE accountId= ");
           sql.append(accountId);
           sql.append(" and creationDate BETWEEN '");
           sql.append(date);
           sql.append("' and '");
           sql.append(date2);
           sql.append("'");

           System.out.println(sql.toString());

           Query q = entityManager.createNativeQuery(sql.toString());
           if ((q.getSingleResult())!=null){
               totalType =  Float.valueOf( ((Double)((List) q.getSingleResult()).get(0)).toString());
              // totalType = (Float)(((Double) q.getSingleResult()).floatValue());

           }

                  	   //totalType = (Float) ((Vector) q.getSingleResult()).get(0);
       } catch (Exception e) {
           throw new GeneralException("Exception in method getTotalInvoicebyTypebyAni "+ e.getMessage(), e.getStackTrace());
       }
       return totalType;
   }



      public Float getTotalTransacctionNum(Long accountId, Timestamp date1, Date date2) throws GeneralException, NullParameterException{
	 Float totalType = null;
         java.sql.Date date = new java.sql.Date(date1.getTime());

	   if (accountId == null ) {
           throw new NullParameterException("Parameter AccountId  cant be null ");
       }
	   if (date == null ) {
           throw new NullParameterException("Parameter date cant be null in method getTotalAmauntbyTransacction");
       }
	   if (date2 == null ) {
           throw new NullParameterException("Parameter date2 cant be null in method getTotalAmauntbyTransacction");
       }

       try {
           StringBuilder sql = new StringBuilder();
           sql.append("SELECT count(*) FROM transaction WHERE accountId= ");
           sql.append(accountId);
           sql.append(" and creationDate BETWEEN '");
           sql.append(date);
           sql.append("' and '");
           sql.append(date2);
           sql.append("'");

           System.out.println(sql.toString());

           Query q = entityManager.createNativeQuery(sql.toString());
           if ((q.getSingleResult())!=null){
               totalType =  Float.valueOf( ((Double)((List) q.getSingleResult()).get(0)).toString());
               //totalType = (Float)(((Double) q.getSingleResult()).floatValue());

           }

       } catch (Exception e) {
           throw new GeneralException("Exception in method getTotalInvoicebyTypebyAni "+ e.getMessage(), e.getStackTrace());
       }
       return totalType;
   }

    public Country loadCountryByName(String name) throws RegisterNotFoundException, NullParameterException, GeneralException {
        List<Country> list = new ArrayList<Country>();
        Country country = new Country();

        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT ct.country FROM CountryTranslation ct ");
            sqlBuilder.append("WHERE ct.alias LIKE '").append(name).append("'")//Problema con el caso FRANCE y GUYANA FRANCESA
                    .append(" OR ct.country.alternativeName1 LIKE '").append(name).append("'").append(" OR ct.country.alternativeName2 LIKE '%").append(name).append("'").append(" OR ct.country.alternativeName3 LIKE '%").append(name).append("'");
            //country = (Country) createQuery(sqlBuilder.toString()).setHint("toplink.refresh", "true").getSingleResult();
             list = createQuery(sqlBuilder.toString()).setHint("toplink.refresh", "true").getResultList();
            if (list.isEmpty()) {
                System.out.println("name: " + name);
                throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByName", Country.class.getSimpleName(), null), null);
            }else{
                country = list.get(0);
            }
        } catch (RegisterNotFoundException ex) {
            System.out.println("name: " + name);
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByName", Country.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            System.out.println("name: " + name);
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }

        return country;
    }

    public Country searchCountry(String name) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Country country = new Country();

        try {
            if (name == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "name"), null);
            }
            StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT ct.country FROM CountryTranslation ct ");
            sqlBuilder.append("WHERE ct.country.name LIKE '").append(name).append("'").append(" OR ct.alias LIKE '").append(name).append("'")//Problema con el caso FRANCE y GUYANA FRANCESA
                    .append(" OR ct.country.alternativeName1 LIKE '%").append(name).append("%'").append(" OR ct.country.alternativeName2 LIKE '%").append(name).append("'").append(" OR ct.country.alternativeName3 LIKE '%").append(name).append("'").append(" OR ct.country.shortName LIKE '").append(name).append("'");
            country = (Country) createQuery(sqlBuilder.toString()).setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            System.out.println("name: " + name);
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByName", Country.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            System.out.println("name: " + name);
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }

        return country;
    }

    public Country saveCountry(Country country) throws NullParameterException, GeneralException {
        if (country == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "country"), null);
        }
        return (Country) saveEntity(country);
    }

    public void deleteEnterpriseHasTinType(Long enterpriseId) throws NullParameterException, GeneralException {
        if (enterpriseId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "enterpriseId"), null);
        }

        try {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            Query query = createQuery("DELETE FROM EnterpriseHasTinType ehhtt WHERE ehhtt.enterprise.id=?1");
            query.setParameter("1", enterpriseId);
            query.executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
        }
    }

    public Country loadCountryByShortName(String referenceCode) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Country country = new Country();
        try {
            if (referenceCode == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "referenceCode"), null);
            }

            Query query = createQuery("SELECT c FROM Country c WHERE c.shortName = ?1");
            query.setParameter("1", referenceCode);
            country = (Country) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            System.out.println("shortName: " + referenceCode);
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Country.class.getSimpleName(), "loadCountryByShortName", Country.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            System.out.println("shortName: " + referenceCode);
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return country;
    }

    public int getCyclesbyPreferenValue(Long preferenceFieldId) throws EmptyListException, NullParameterException, GeneralException {
        int cycles = 0;
        List list = new ArrayList();
        if (preferenceFieldId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "preferenceFieldId"), null);
        }
        try {
            StringBuilder sqlBuilder = new StringBuilder("Select p.value From PreferenceValue p WHERE p.endingDate IS NULL AND  p.preferenceField.id = ?1 ORDER BY p.id DESC");
            Query query = entityManager.createQuery(sqlBuilder.toString());
            query.setParameter("1", preferenceFieldId);
            list = (List) query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), null);
        }
        if (!list.isEmpty()) {
            cycles = Integer.parseInt((String) list.get(0));
        }
        return cycles;
    }

    public List<Currency> getCurrencies() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<Currency> currencies = (List<Currency>) listEntities(Currency.class, request, logger, getMethodName());

        return currencies;
    }

    public Currency loadCurrency(EJBRequest request) throws RegisterNotFoundException, NullParameterException, GeneralException {
        Currency currency = (Currency) loadEntity(Currency.class, request, logger, getMethodName());
        return currency;
    }

    public CountryHasProvider saveCountryHasProvider(CountryHasProvider countryHasProvider) throws NullParameterException, GeneralException {
        if (countryHasProvider == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "countryHasProvider"), null);
        }
        return (CountryHasProvider) saveEntity(countryHasProvider);
    }

    public List<CountryTranslation> getCountryTranslationByCountryId(Long countryId) throws EmptyListException, NullParameterException, GeneralException {
        List<CountryTranslation> countryTranslations = new ArrayList<CountryTranslation>();
        try {
            if (countryId == null) {
                throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "countryId"), null);
            }
            Query query = createQuery("SELECT ct FROM CountryTranslation ct WHERE ct.country.id =?1");
            query.setParameter("1", countryId);
            countryTranslations = (List<CountryTranslation>) query.setHint("toplink.refresh", "true").getResultList();
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        if (countryTranslations.isEmpty()) {
            throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return countryTranslations;
    }

    public CountryTranslation saveCountryTranslation(CountryTranslation countryTranslation) throws NullParameterException, GeneralException {
        if (countryTranslation == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "countryTranslation"), null);
        }
        return (CountryTranslation) saveEntity(countryTranslation);
    }
    
    public List<Provider> getProvider() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<Provider> providers = (List<Provider>) listEntities(Provider.class, request, logger, getMethodName());
        return providers;
    }
    
    public List<Braund> getBraunds() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<Braund> braunds = (List<Braund>) listEntities(Braund.class, request, logger, getMethodName());
        return braunds;
    }
    
    public List<Model> getModelsByBraund(Long braundId) throws EmptyListException, GeneralException, NullParameterException {
        List<Model> models = null;
        try {
        if (braundId==null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "braundId"), null);
        }
        Query query = createQuery("SELECT m FROM Model m WHERE m.braund.id =?1");
        query.setParameter("1", braundId);
        models = (List<Model>) query.setHint("toplink.refresh", "true").getResultList();
	    } catch (Exception ex) {
	        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
	    }
	    if (models.isEmpty()) {
	        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
	    }
        return models;
    }

    public List<ControlType> getControlTypes() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<ControlType> controlTypes = (List<ControlType>) listEntities(ControlType.class, request, logger, getMethodName());
        return controlTypes;
    }
    
    public List<EnterCalibration> getEnterCalibrations() throws EmptyListException, GeneralException, NullParameterException {
        EJBRequest request = new EJBRequest();
        List<EnterCalibration> enterCalibrations = (List<EnterCalibration>) listEntities(EnterCalibration.class, request, logger, getMethodName());
        return enterCalibrations;
    }

	@Override
	public Braund saveBraund(Braund braund) throws NullParameterException, GeneralException {
		 if (braund == null) {
	            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "braund"), null);
	        }
	        return (Braund) saveEntity(braund);
	}

	@Override
	public Model saveModel(Model model) throws NullParameterException, GeneralException {
		if (model == null) {
			throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "model"), null);
		}
		return (Model) saveEntity(model);
	}

	@Override
	public List<Model> getModels() throws EmptyListException, GeneralException, NullParameterException {
		EJBRequest request = new EJBRequest();
		List<Model> models = (List<Model>) listEntities(Model.class, request, logger, getMethodName());
		return models;
	}
	
	@Override
	public EnterCalibration saveEnterCalibration(EnterCalibration enterCalibration) throws NullParameterException, GeneralException{
		if (enterCalibration == null) {
			throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "enterCalibration"), null);
		}
		return (EnterCalibration) saveEntity(enterCalibration);
	}

	@Override
	public List<QuarantineStatus> getQuaratineStatus()throws EmptyListException, GeneralException, NullParameterException {
		 EJBRequest request = new EJBRequest();
		  List<QuarantineStatus> quarantineStatus = (List<QuarantineStatus>) listEntities(QuarantineStatus.class, request, logger, getMethodName());
		  return quarantineStatus;
	}

	@Override
	public void deleteEmail(EnterpriseHasEmail email) throws GeneralException {
			EntityTransaction trans = entityManager.getTransaction();
			try {
				trans.begin();
				EnterpriseHasEmail email2 = entityManager.merge(email);
				entityManager.remove(email2);
				trans.commit();
           } catch (Exception e) {
               e.printStackTrace();
               try {
                   if (trans.isActive()) {
                 	  trans.rollback();
                   }
               } catch (IllegalStateException e1) {
             	  throw  new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "GeneralException"), null);
               } catch (SecurityException e1) {
             	  throw  new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "GeneralException"), null);
               }
               throw  new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "GeneralException"), null);
           }
	}

	@Override
	public EnterpriseHasEmail saveEnterpriseHasEmail(EnterpriseHasEmail email) throws NullParameterException, GeneralException {
		if (email == null) {
			throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "model"), null);
		}
		return (EnterpriseHasEmail) saveEntity(email);
	}

	@Override
	public List<EnterpriseHasEmail> getEnterpriseHasEmails() throws EmptyListException, GeneralException, NullParameterException {
		EJBRequest request = new EJBRequest();
		List<EnterpriseHasEmail> emails = (List<EnterpriseHasEmail>) listEntities(EnterpriseHasEmail.class, request, logger, getMethodName());
		return emails;
	}
 }
