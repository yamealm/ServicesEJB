package com.alodiga.services.provider.ejb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.alodiga.services.provider.commons.ejbs.TransactionEJB;
import com.alodiga.services.provider.commons.ejbs.TransactionEJBLocal;
import com.alodiga.services.provider.commons.exceptions.EmptyListException;
import com.alodiga.services.provider.commons.exceptions.GeneralException;
import com.alodiga.services.provider.commons.exceptions.MinAmountBalanceException;
import com.alodiga.services.provider.commons.exceptions.NegativeBalanceException;
import com.alodiga.services.provider.commons.exceptions.NullParameterException;
import com.alodiga.services.provider.commons.exceptions.RegisterNotFoundException;
import com.alodiga.services.provider.commons.genericEJB.AbstractSPEJB;
import com.alodiga.services.provider.commons.genericEJB.EJBRequest;
import com.alodiga.services.provider.commons.genericEJB.SPContextInterceptor;
import com.alodiga.services.provider.commons.genericEJB.SPLoggerInterceptor;
import com.alodiga.services.provider.commons.models.Category;
import com.alodiga.services.provider.commons.models.Condicion;
import com.alodiga.services.provider.commons.models.ProductHistory;
import com.alodiga.services.provider.commons.models.ProductSerie;
import com.alodiga.services.provider.commons.models.Transaction;
import com.alodiga.services.provider.commons.models.TransactionType;
import com.alodiga.services.provider.commons.utils.EjbConstants;
import com.alodiga.services.provider.commons.utils.EjbUtils;
import com.alodiga.services.provider.commons.utils.QueryConstants;

@Interceptors({SPLoggerInterceptor.class, SPContextInterceptor.class})
@Stateless(name = EjbConstants.TRANSACTION_EJB, mappedName = EjbConstants.TRANSACTION_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class TransactionEJBImp extends AbstractSPEJB implements TransactionEJB, TransactionEJBLocal {

    private static final Logger logger = Logger.getLogger(TransactionEJBImp.class);

	@Override
	public List<Transaction> getTransactionByCondition(EJBRequest request)throws NullParameterException, EmptyListException, GeneralException {
		List<Transaction> transactions = new ArrayList<Transaction>();
        Map orderField = new HashMap();
        orderField.put("id", QueryConstants.ORDER_DESC);
        Boolean isFilter = (Boolean) request.getParam();
        if (isFilter == null || isFilter.equals("null")) {
            isFilter = false;
        }
        createSearchQuery(Transaction.class, request, orderField, logger, getMethodName(), "customers", isFilter);
        transactions = (List<Transaction>) createSearchQuery(Transaction.class, request, orderField, logger, getMethodName(), "customers", isFilter);
        return transactions;
	}

	@Override
	public List<Condicion> getConditions() throws GeneralException, NullParameterException, EmptyListException {
		EJBRequest request = new EJBRequest();
		List<Condicion> conditions = (List<Condicion>) listEntities(Condicion.class, request, logger, getMethodName());
	    return conditions;
	}

	@Override
	public List<Category> getCategories() throws GeneralException, NullParameterException, EmptyListException {
		EJBRequest request = new EJBRequest();
		List<Category> categories = (List<Category>) listEntities(Category.class, request, logger, getMethodName());
	    return categories;
	}

	@Override
	public Transaction loadTransaction(EJBRequest request)throws GeneralException, RegisterNotFoundException, NullParameterException {
		return (Transaction) loadEntity(Transaction.class, request, logger, getMethodName());
	}

	@Override
	public Transaction saveTransaction(EJBRequest request)throws GeneralException, NullParameterException {
	    return (Transaction) saveEntity(request, logger, getMethodName());
	}

	@Override
	public List<Transaction> searchTransaction(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException{
		 List<Transaction> transactions = new ArrayList<Transaction>();
	    Map<String, Object> params = request.getParams();
	
	    StringBuilder sqlBuilder = new StringBuilder("SELECT t FROM Transaction t WHERE t.creationDate BETWEEN ?1 AND ?2");
	    if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
	        throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
	    }
	    if (params.containsKey(QueryConstants.PARAM_CUSTOMER_ID)) {
	        sqlBuilder.append(" AND t.customer.id=").append(params.get(QueryConstants.PARAM_CUSTOMER_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_PRODUCT_ID)) {
	        sqlBuilder.append(" AND t.product.id=").append(params.get(QueryConstants.PARAM_PRODUCT_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_CATEGORY_ID)) {
	        sqlBuilder.append(" AND t.category.id=").append(params.get(QueryConstants.PARAM_CATEGORY_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_CONDITION_ID)) {
	        sqlBuilder.append(" AND t.condition.id=").append(params.get(QueryConstants.PARAM_CONDITION_ID));
	    }
	    Query query = null;
	    try {
	        System.out.println("query:********"+sqlBuilder.toString());
	        query = createQuery(sqlBuilder.toString());
	        query.setParameter("1", EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE)));
	        query.setParameter("2", EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE)));
	        if (request.getLimit() != null && request.getLimit() > 0) {
	            query.setMaxResults(request.getLimit());
	        }
	        transactions = query.setHint("toplink.refresh", "true").getResultList();
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
	    }
	    if (transactions.isEmpty()) {
	        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
	    }
	    return transactions;
	}


	@Override
	public Float getCurrentBalanceByProduct(Long productId) throws NullParameterException, GeneralException {
        if (productId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "productId"), null);
        }
        Float currentBalance = 0F;

        try {
            StringBuilder sqlDateBuilder = new StringBuilder("SELECT MAX(b.creationDate) FROM ProductHistory b WHERE b.product.id = ?1");
            Query queryDate = entityManager.createQuery(sqlDateBuilder.toString());
            queryDate.setParameter("1", productId);
            Timestamp maxDate = (Timestamp) queryDate.getSingleResult();

            StringBuilder sqlBuilder = new StringBuilder("SELECT b.currentQuantity FROM ProductHistory b WHERE b.creationDate = ?1 AND b.product.id = ?2");
            Query query = entityManager.createQuery(sqlBuilder.toString());
            query.setParameter("1", maxDate);
            query.setParameter("2", productId);
            List result = (List) query.setHint("toplink.refresh", "true").getResultList();

            currentBalance = result != null && result.size() > 0 ? ((Float) result.get(0)) : 0f;
        } catch (NoResultException ex) {
            //
        } catch (Exception e) {
            e.printStackTrace();
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "BalanceHistory"), null);
        }
        return currentBalance;
	}

	@Override
	public Condicion loadConditionbyId(Long id)	throws NullParameterException, RegisterNotFoundException, GeneralException {
		  if (id == null) {
	            throw new NullParameterException(" parameter id cannot be null in loadConditionbyId.");
	      }
		  Condicion condition = null;
	      try {
	          Query query = createQuery("SELECT tt FROM Condition tt WHERE tt.id = ?1");
	          query.setParameter("1", id);
	          condition = (Condicion) query.setHint("toplink.refresh", "true").getSingleResult();
	      } catch (NoResultException ex) {
	            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Condicion.class.getSimpleName(), "loadConditionbyId", Condicion.class.getSimpleName(), null), ex);
	      } catch (Exception ex) {
	            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
	      }
	      return condition;
	}

	@Override
	public Category loadCategorybyId(Long id)throws NullParameterException, RegisterNotFoundException, GeneralException {
		  if (id == null) {
	            throw new NullParameterException(" parameter id cannot be null in loadConditionbyId.");
	      }
		  Category category = null;
	      try {
	          Query query = createQuery("SELECT tt FROM Category tt WHERE tt.id = ?1");
	          query.setParameter("1", id);
	          category = (Category) query.setHint("toplink.refresh", "true").getSingleResult();
	      } catch (NoResultException ex) {
	            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Category.class.getSimpleName(), "loadCategorybyId", Category.class.getSimpleName(), null), ex);
	      } catch (Exception ex) {
	            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
	      }
	      return category;
	}

	@Override
	public Transaction loadTransactionById(Long id)	throws GeneralException, RegisterNotFoundException, NullParameterException {
        if (id == null) {
            throw new NullParameterException(" parameter id cannot be null in loadTransactionById.");
        }
        Transaction transaction = null;
        try {
            Query query = createQuery("SELECT t FROM Transaction t WHERE t.id = ?1");
            query.setParameter("1", id);
            transaction = (Transaction) query.setHint("toplink.refresh", "true").getSingleResult();
        } catch (NoResultException ex) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, Transaction.class.getSimpleName(), "loadTransactionById", Transaction.class.getSimpleName(), null), ex);
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
        }
        return transaction;
	}

	@Override
	public ProductHistory loadLastProductHistoryByProductId(Long productId)	throws GeneralException, RegisterNotFoundException, NullParameterException {
		 if (productId == null) {
	            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "accountId"), null);
	     }
		 ProductHistory productHistory = null;
	     try {
	          Timestamp maxDate = (Timestamp) entityManager.createQuery("SELECT MAX(b.creationDate) FROM ProductHistory b WHERE b.product.id = " + productId).getSingleResult();
	          Query query = entityManager.createQuery("SELECT b FROM ProductHistory b WHERE b.creationDate = :maxDate AND b.product.id = " + productId);
	          query.setParameter("maxDate", maxDate);
              List result = (List) query.setHint("toplink.refresh", "true").getResultList();

	          if (!result.isEmpty()) {
	        	  productHistory = ((ProductHistory) result.get(0));
	          }
	     } catch (NoResultException ex) {
	           throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, this.getClass(), getMethodName(), "ProductHistory"), null);
	     } catch (Exception e) {
	           e.printStackTrace();
	           throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "ProductHistory"), null);
	     }
	     return productHistory;
	}

	@Override
	public ProductHistory saveProductHistory(EJBRequest request) throws GeneralException, NullParameterException {
		return (ProductHistory) saveEntity(request, logger, getMethodName());
	}
	
	private ProductSerie saveProductSerie(EJBRequest request) throws GeneralException, NullParameterException {
		return (ProductSerie) saveEntity(request, logger, getMethodName());
	}

	@Override
	public boolean validateBalance(ProductHistory currentProductHistory, float amount, boolean isAdd) throws NegativeBalanceException {
	   if (!isAdd && (currentProductHistory.getCurrentQuantity() - amount) < 0) {
	            throw new NegativeBalanceException(logger, sysError.format(EjbConstants.ERR_MIN_AMOUNT_BALANCE, this.getClass(), getMethodName(), "param"), null);
	   }
	   return true;
	}

	 public Transaction saveTransactionStock(Transaction transaction , List<ProductSerie> productSeries) throws GeneralException, NullParameterException, NegativeBalanceException,RegisterNotFoundException{
		
		  if (transaction == null) {
	            throw new NullParameterException(logger, sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "param"), null);
	      }
		  try {
			  ProductHistory currentProductHistory =loadLastProductHistoryByProductId(transaction.getProduct().getId());
			  validateBalance(currentProductHistory, transaction.getQuantity(),transaction.getTransactionType().getId().equals(TransactionType.ADD));
			  ProductHistory productHistory = new ProductHistory();
		      List<ProductHistory> histories = new ArrayList<ProductHistory>();
		      productHistory = createBalanceHistory(transaction, transaction.getQuantity(), transaction.getTransactionType().getId().equals(TransactionType.ADD));
		      productHistory.setTransaction(transaction);
              histories.add(productHistory);
              transaction.setProductHistories(histories);
              EJBRequest request = new EJBRequest();
              request.setParam(transaction);
              transaction = saveTransaction(request);
              for (ProductSerie productSerie : productSeries) {
            	  productSerie.setBeginTransactionId(transaction);
            	  EJBRequest requestSerie = new EJBRequest();
            	  requestSerie.setParam(productSerie);
            	  saveProductSerie(requestSerie);
              }
              
	        }  catch (RegisterNotFoundException ex) {
		           throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, this.getClass(), getMethodName(), "ProductHistory"), null);
		    }catch (NegativeBalanceException e) {
	            throw  new NegativeBalanceException(logger, sysError.format(EjbConstants.ERR_MIN_AMOUNT_BALANCE, this.getClass(), getMethodName(), "MinAmountBalance"), null);
	        }catch (GeneralException e) {
	            throw  new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "GeneralException"), null);
	        }
		 
		 return transaction;
	 
	 }
	 
	 private ProductHistory createBalanceHistory(Transaction transaction, int transferAmount, boolean isAdd) throws GeneralException, NullParameterException, NegativeBalanceException, RegisterNotFoundException {
		    ProductHistory currentProductHistory = loadLastProductHistoryByProductId(transaction.getProduct().getId());
	        validateBalance(currentProductHistory, transferAmount, isAdd);
	        int currentQuantity = currentProductHistory!=null?currentProductHistory.getCurrentQuantity():0;
	        ProductHistory productHistory = new ProductHistory();
	        productHistory.setProduct(transaction.getProduct());
	        productHistory.setCreationDate(new Timestamp(new Date().getTime()));
	        productHistory.setOldQuantity(currentQuantity);
	        productHistory.setOldAmount(currentProductHistory!=null?currentProductHistory.getCurrentAmount():0f);
	        productHistory.setCurrentAmount(transaction.getAmount());
	        int newCurrentQuantity = 0;
	        if(!isAdd)
	        	newCurrentQuantity = currentQuantity - transferAmount; //RESTO DEL MONTO ACTUAL (EL QUE REALIZA LA TRANSFERENCIA)
	        else
	        	newCurrentQuantity = currentQuantity + transferAmount;//SUMO AL MONTO ACTUAL (EL DESTINO)
	        
	        if (newCurrentQuantity < 0) {
	            throw new NegativeBalanceException("Current amount can not be negative");
	        }
	        productHistory.setCurrentQuantity(newCurrentQuantity);
	        return productHistory;
	    }
	 

}
