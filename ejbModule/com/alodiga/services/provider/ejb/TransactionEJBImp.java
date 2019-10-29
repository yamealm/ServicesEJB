package com.alodiga.services.provider.ejb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.alodiga.services.provider.commons.ejbs.TransactionEJB;
import com.alodiga.services.provider.commons.ejbs.TransactionEJBLocal;
import com.alodiga.services.provider.commons.ejbs.UtilsEJBLocal;
import com.alodiga.services.provider.commons.exceptions.EmptyListException;
import com.alodiga.services.provider.commons.exceptions.GeneralException;
import com.alodiga.services.provider.commons.exceptions.NegativeBalanceException;
import com.alodiga.services.provider.commons.exceptions.NullParameterException;
import com.alodiga.services.provider.commons.exceptions.RegisterNotFoundException;
import com.alodiga.services.provider.commons.genericEJB.AbstractSPEJB;
import com.alodiga.services.provider.commons.genericEJB.EJBRequest;
import com.alodiga.services.provider.commons.genericEJB.SPContextInterceptor;
import com.alodiga.services.provider.commons.genericEJB.SPGenericEntity;
import com.alodiga.services.provider.commons.genericEJB.SPLoggerInterceptor;
import com.alodiga.services.provider.commons.models.Category;
import com.alodiga.services.provider.commons.models.Condicion;
import com.alodiga.services.provider.commons.models.Enterprise;
import com.alodiga.services.provider.commons.models.MetrologicalControl;
import com.alodiga.services.provider.commons.models.MetrologicalControlHistory;
import com.alodiga.services.provider.commons.models.Product;
import com.alodiga.services.provider.commons.models.ProductHistory;
import com.alodiga.services.provider.commons.models.ProductSerie;
import com.alodiga.services.provider.commons.models.Transaction;
import com.alodiga.services.provider.commons.models.TransactionType;
import com.alodiga.services.provider.commons.utils.EjbConstants;
import com.alodiga.services.provider.commons.utils.EjbUtils;
import com.alodiga.services.provider.commons.utils.QueryConstants;
import com.alodiga.services.provider.commons.utils.ServiceMailDispatcher;

@Interceptors({SPLoggerInterceptor.class, SPContextInterceptor.class})
@Stateless(name = EjbConstants.TRANSACTION_EJB, mappedName = EjbConstants.TRANSACTION_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class TransactionEJBImp extends AbstractSPEJB implements TransactionEJB, TransactionEJBLocal {
	@EJB
	private UtilsEJBLocal  utilsEJB;

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
	public Integer loadQuantityByProductId(Long productId, Long categoryId)	throws GeneralException, NullParameterException {
		 if (productId == null) {
	            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "accountId"), null);
	     }
		 Long quantityTotal = null;
	     try {
	          quantityTotal = (Long) entityManager.createQuery("SELECT sum(b.quantity) FROM ProductSerie b WHERE b.product.id = " + productId + " and b.endingDate is null and b.category.id="+ categoryId ).getSingleResult();
	     } catch (NoResultException ex) {
	    	 quantityTotal = 0L;
	     } catch (Exception e) {
	           e.printStackTrace();
	           throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "ProductHistory"), null);
	     }
	     if (quantityTotal==null)
	    	 quantityTotal = 0L;
	     return quantityTotal.intValue();
	}

	@Override
	public ProductHistory saveProductHistory(EJBRequest request) throws GeneralException, NullParameterException {
		return (ProductHistory) saveEntity(request, logger, getMethodName());
	}
	
	@Override
	public MetrologicalControlHistory saveMetrologicalControlHistory(EJBRequest request) throws GeneralException, NullParameterException {
		return (MetrologicalControlHistory) saveEntity(request, logger, getMethodName());
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
	
	@Override
	public boolean validateBalanceProduct(Integer currentQuantity, float amount, boolean isAdd) throws NegativeBalanceException {
	   if (!isAdd && (currentQuantity - amount) < 0) {
	            throw new NegativeBalanceException(logger, sysError.format(EjbConstants.ERR_MIN_AMOUNT_BALANCE, this.getClass(), getMethodName(), "param"), null);
	   }
	   return true;
	}

	 public Transaction saveTransactionStock(Transaction transaction , List<ProductSerie> productSeries) throws GeneralException, NullParameterException, NegativeBalanceException,RegisterNotFoundException{
		
		  if (transaction == null) {
	            throw new NullParameterException(logger, sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "param"), null);
	      }
		  try {
			  int currentQuantity =loadQuantityByProductId(transaction.getProduct().getId(), transaction.getCategory().getId());
			  validateBalanceProduct(currentQuantity, transaction.getQuantity(),transaction.getTransactionType().getId().equals(TransactionType.ENTRY));
			  EntityTransaction trans = entityManager.getTransaction();
				try {
					trans.begin();
					Product product = transaction.getProduct();
					if (((SPGenericEntity) product).getPk() != null) {
						entityManagerWrapper.update(product);
					}
					entityManagerWrapper.save(transaction);
					
					for (ProductSerie productSerie : productSeries) {
						entityManagerWrapper.save(productSerie);
						
					}
					trans.commit();
				} catch (Exception e) {
					e.printStackTrace();
					try {
						if (trans.isActive()) {
							trans.rollback();
						}
					} catch (IllegalStateException e1) {
						throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);
					} catch (SecurityException e1) {
						throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);
					}
					throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);
				}
	        } catch (NegativeBalanceException e) {
	            throw  new NegativeBalanceException(logger, sysError.format(EjbConstants.ERR_MIN_AMOUNT_BALANCE, this.getClass(), getMethodName(), "MinAmountBalance"), null);
	        }catch (GeneralException e) {
	            throw  new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "GeneralException"), null);
	        }
		 
		 return transaction;
	 
	 }
	 
	 @Override
	 public Transaction saveEgressStock(Transaction transaction , List<ProductSerie> productSeries) throws GeneralException, NullParameterException, NegativeBalanceException,RegisterNotFoundException{
			
		if (transaction == null) {
			throw new NullParameterException(logger,
					sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "param"), null);
		}
		EntityTransaction trans = entityManager.getTransaction();
		try {
			trans.begin();
			transaction =(Transaction) entityManagerWrapper.save(transaction);
			for (ProductSerie productSerie : productSeries) {
				if (productSerie.getId() != null) {
					productSerie.setEndingTransactionId(transaction);
					entityManagerWrapper.update(productSerie);
				}else {
					entityManagerWrapper.save(productSerie);
				}

			}
			trans.commit();
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (trans.isActive()) {
					trans.rollback();
				}
			} catch (IllegalStateException e1) {
				throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);
			} catch (SecurityException e1) {
				throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);
			}
			throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);

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

	 @Override
	 public Transaction modificarStock(Transaction transaction , ProductSerie productSerie) throws GeneralException, NullParameterException, NegativeBalanceException,RegisterNotFoundException{
			
		  if (transaction == null) {
	            throw new NullParameterException(logger, sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "param"), null);
	      }
		  try {
			EntityTransaction trans = entityManager.getTransaction();
			try {
				trans.begin();
				if (((SPGenericEntity) transaction).getPk() != null) {
					entityManagerWrapper.update(transaction);
				}
				if (((SPGenericEntity) productSerie).getPk() != null) {
					entityManagerWrapper.update(productSerie);
				}
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
             
	        }catch (GeneralException e) {
	            throw  new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "GeneralException"), null);
	        }
		 
		 return transaction;
	 
	 }
	 
	 @Override
	 public Transaction deleteStock(Transaction transaction , ProductSerie productSerie) throws GeneralException, NullParameterException, NegativeBalanceException,RegisterNotFoundException{
			
		  if (transaction == null) {
	            throw new NullParameterException(logger, sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "param"), null);
	      }
		  try {
			EntityTransaction trans = entityManager.getTransaction();
			try {
				trans.begin();
				ProductSerie productSerie2 = entityManager.merge(productSerie);
				entityManager.remove(productSerie2);
				List<ProductSerie> productSeries = new ArrayList<ProductSerie>();

				StringBuilder sqlBuilder = new StringBuilder("SELECT t FROM ProductSerie t WHERE t.endingDate is NULL  and t.beginTransactionId.id =?1");
				try {
					Query query = entityManager.createQuery(sqlBuilder.toString());
					query.setParameter("1", productSerie.getBeginTransactionId().getId());
					productSeries = query.setHint("toplink.refresh", "true").getResultList();
				} catch (Exception e) {
					throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION,this.getClass(), getMethodName(), e.getMessage()), null);
				}
				if (productSeries.isEmpty()) {
					Transaction transaction2 = entityManager.merge(transaction);
					entityManager.remove(transaction2);	
				}
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
             
	        }catch (GeneralException e) {
	            throw  new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "GeneralException"), null);
	        }
		 
		 return transaction;
	 
	 }
	 
	@Override
	public List<ProductSerie> searchProductSerieByProductId(Long productId, Long categoryId)	throws GeneralException, NullParameterException, EmptyListException {
		 List<ProductSerie> productSeries = new ArrayList<ProductSerie>();
		
		    StringBuilder sqlBuilder = new StringBuilder("SELECT t FROM ProductSerie t WHERE t.endingDate is NULL  and t.product.id =?1 and t.category.id =?2" );
		    try {
		         Query query = entityManager.createQuery(sqlBuilder.toString());
		         query.setParameter("1", productId);
		         query.setParameter("2", categoryId);
		        productSeries = query.setHint("toplink.refresh", "true").getResultList();
		    } catch (Exception e) {
		        e.printStackTrace();
		        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
		    }
		    if (productSeries.isEmpty()) {
		        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
		    }
		    return productSeries;
	}
	
	@Override
	public List<ProductSerie> searchProductSerieByCategoryId(Long categoryId) throws GeneralException, NullParameterException, EmptyListException{
		 List<ProductSerie> productSeries = new ArrayList<ProductSerie>();
			
		    StringBuilder sqlBuilder = new StringBuilder("SELECT t FROM ProductSerie t WHERE t.endingDate is NULL  and t.category.id =?1" );
		    try {
		         Query query = entityManager.createQuery(sqlBuilder.toString());
		         query.setParameter("1", categoryId);
		        productSeries = query.setHint("toplink.refresh", "true").getResultList();
		    } catch (Exception e) {
		        e.printStackTrace();
		        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
		    }
		    if (productSeries.isEmpty()) {
		        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
		    }
		    return productSeries;
	}
	
	@Override
	public List<Product> listProducts(EJBRequest request)	throws GeneralException, NullParameterException, EmptyListException {
		    List<Product> products = new ArrayList<Product>();
			Map<String, Object> params = request.getParams();

			if (!params.containsKey(QueryConstants.PARAM_ENABLED)) {
				throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(),
						getMethodName(), QueryConstants.PARAM_ENABLED), null);
			}

			try {
				products = (List<Product>) getNamedQueryResult(Product.class, QueryConstants.LIST_PRODUCT,request, getMethodName(), logger, "Products");
			} catch (Exception e) {
		        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
		    }
			if (products.isEmpty())
				throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);

			return products;
	}

	@Override
	public List<TransactionType> getTransactionTypes()	throws GeneralException, NullParameterException, EmptyListException {
		EJBRequest request = new EJBRequest();
		List<TransactionType> transactionTypes = (List<TransactionType>) listEntities(TransactionType.class, request, logger, getMethodName());
	    return transactionTypes;
	}
	 
	 public MetrologicalControl saveMetrologicalControl(MetrologicalControl metrologicalControl ,MetrologicalControlHistory metrologicalControlHistory) throws GeneralException, NullParameterException ,RegisterNotFoundException{
			
		  if (metrologicalControl == null) {
	            throw new NullParameterException(logger, sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "param"), null);
	      }
		  try {
			 
			  EntityTransaction trans = entityManager.getTransaction();
				try {
					trans.begin();
					if (((SPGenericEntity) metrologicalControl).getPk() != null) {
						entityManagerWrapper.update(metrologicalControl);
						 MetrologicalControlHistory history = loadLastMetrologicalControlHistoryByMetrologicalControlId(metrologicalControl.getId());
						 if (!EjbUtils.getBeginningDate(history.getCalibrationDate()).equals(EjbUtils.getBeginningDate(metrologicalControlHistory.getCalibrationDate()))) {
							 metrologicalControlHistory.setCalibrationDateOld(history.getCalibrationDate());
						 }
						 if (!EjbUtils.getBeginningDate(history.getCalibrationDate()).equals(EjbUtils.getBeginningDate(metrologicalControlHistory.getCalibrationDate()))
							 || !EjbUtils.getBeginningDate(history.getExpirationDate()).equals(EjbUtils.getBeginningDate(metrologicalControlHistory.getExpirationDate()))) {
							 metrologicalControlHistory.setCalibrationDateOld(history.getCalibrationDate());
							 metrologicalControlHistory.setMetrologicalControl(metrologicalControl);
							 entityManagerWrapper.save(metrologicalControlHistory);							 							 
						 }else{
							 history.setCategory(metrologicalControlHistory.getCategory());
							 history.setObservation(metrologicalControlHistory.getObservation());
							 entityManagerWrapper.update(history);	
						 }
					}else {
						entityManagerWrapper.save(metrologicalControl);	
						metrologicalControlHistory.setMetrologicalControl(metrologicalControl);
						entityManagerWrapper.save(metrologicalControlHistory);
					}
					
					
					trans.commit();
				} catch (Exception e) {
					e.printStackTrace();
					try {
						if (trans.isActive()) {
							trans.rollback();
						}
					} catch (IllegalStateException e1) {
						throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);
					} catch (SecurityException e1) {
						throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);
					}
					throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), "GeneralException"), null);
				}
	        } catch (GeneralException e) {
	            throw  new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), "GeneralException"), null);
	        }
		 
		 return metrologicalControl;
	 
	 }
	 
	@Override
	public MetrologicalControlHistory loadLastMetrologicalControlHistoryByMetrologicalControlId(Long metrologicalControlId) throws GeneralException, RegisterNotFoundException, NullParameterException {
		if (metrologicalControlId == null) {
			throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(),getMethodName(), "metrologicalControlId"), null);
		}
		MetrologicalControlHistory metrologicalControlHistory = null;
		try {
			Timestamp maxDate = (Timestamp) entityManager.createQuery(
					"SELECT MAX(b.creationDate) FROM MetrologicalControlHistory b WHERE b.metrologicalControl.id = "+ metrologicalControlId)
					.getSingleResult();
			Query query = entityManager.createQuery("SELECT b FROM MetrologicalControlHistory b WHERE b.creationDate = :maxDate AND b.metrologicalControl.id = "+ metrologicalControlId);
			query.setParameter("maxDate", maxDate);
			List result = (List) query.setHint("toplink.refresh", "true").getResultList();

			if (!result.isEmpty()) {
				metrologicalControlHistory = ((MetrologicalControlHistory) result.get(0));
			}
		} catch (NoResultException ex) {
			throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION,this.getClass(), getMethodName(), "MetrologicalControlHistory"), null);
		} catch (Exception e) {
			e.printStackTrace();
			throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),getMethodName(), "MetrologicalControlHistory"), null);
		}
		return metrologicalControlHistory;
	}
	
	@Override
	public List<MetrologicalControl> searchMetrologicalControl(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException{
		 List<MetrologicalControl> metrologicalControls = new ArrayList<MetrologicalControl>();
	    Map<String, Object> params = request.getParams();
	
	    StringBuilder sqlBuilder = new StringBuilder("SELECT t FROM MetrologicalControl t WHERE t.enabled= TRUE OR t.enabled= FALSE");
//	    if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
//	        throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
//	    }
	   
	    if (params.containsKey(QueryConstants.PARAM_BRAUND_ID)) {
	        sqlBuilder.append(" AND t.braund.id=").append(params.get(QueryConstants.PARAM_BRAUND_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_MODEL_ID)) {
	        sqlBuilder.append(" AND t.model.id=").append(params.get(QueryConstants.PARAM_MODEL_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_ENTER_CALIBRATION_ID)) {
	        sqlBuilder.append(" AND t.enterCalibration.id=").append(params.get(QueryConstants.PARAM_ENTER_CALIBRATION_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_CONTROL_TYPE_ID)) {
	        sqlBuilder.append(" AND t.controlType.id=").append(params.get(QueryConstants.PARAM_CONTROL_TYPE_ID));
	    }
	    Query query = null;
	    try {
	        System.out.println("query:********"+sqlBuilder.toString());
	        query = createQuery(sqlBuilder.toString());
//	        query.setParameter("1", EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE)));
//	        query.setParameter("2", EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE)));
	        if (request.getLimit() != null && request.getLimit() > 0) {
	            query.setMaxResults(request.getLimit());
	        }
	        metrologicalControls = query.setHint("toplink.refresh", "true").getResultList();
	    } catch (Exception e) {
	        e.printStackTrace();
	        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
	    }
	    if (metrologicalControls.isEmpty()) {
	        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
	    }
	    return metrologicalControls;
	}

	@Override
	public MetrologicalControl saveMetrologicalControl(MetrologicalControl metrologicalControl)throws GeneralException, NullParameterException ,RegisterNotFoundException{
		if (metrologicalControl == null) {
	        throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "braund"), null);
	    }
	    return (MetrologicalControl) saveEntity(metrologicalControl);
	}
	
	@Override
	public void runAutomaticProcess()throws GeneralException{
		try {
			List<Product> products = getProductByCategoryId(Category.STOCK);
			List<ProductSerie> series = new ArrayList<ProductSerie>();
			List<ProductSerie> quarantines = new ArrayList<ProductSerie>();
			Enterprise enterprise = utilsEJB.loadEnterprisebyId(Enterprise.TURBINES);
			Date date = new Date(); 
			Timestamp today =  new Timestamp(new java.util.Date().getTime());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(today);
			calendar.add(Calendar.DAY_OF_YEAR, 5);
			Timestamp timestampOldDate = new Timestamp(calendar.getTimeInMillis());
			Category category = loadCategorybyId(Category.QUARANTINE);
			for (Product product : products) {
				StringBuilder sqlBuilder = new StringBuilder("SELECT p FROM ProductSerie p WHERE p.product.id="	+ product.getId() + " AND p.expirationDate<='"+timestampOldDate+"' AND p.category.id in (" + Category.STOCK+ ","+Category.METEOROLOGICAL_CONTROL+")");
				Query query = null;
				try {
					System.out.println("query:********" + sqlBuilder.toString());
					query = createQuery(sqlBuilder.toString());
					List<ProductSerie>  productSeries = query.setHint("toplink.refresh", "true").getResultList();
					for (ProductSerie productSerie2 : productSeries) {
						if (productSerie2.getExpirationDate() != null) {
							if (EjbUtils.getBeginningDate(productSerie2.getExpirationDate()).equals(EjbUtils.getBeginningDate(date))	|| (EjbUtils.getBeginningDate(productSerie2.getExpirationDate()).before(EjbUtils.getBeginningDate(date)))) {
								// falta sacar de stock o control metrologico e
								// ingresar a cuarentena
								Transaction transaction2 = loadTransactionById(
										productSerie2.getBeginTransactionId().getId());
								Transaction transaction =(Transaction) transaction2.clone();
								transaction.setId(null);
								transaction.setCreationDate(new Timestamp(new Date().getTime()));
								transaction.setObservation("Entra a cuarentena por fecha de expiracion vencida");
								productSerie2.setEndingDate(new Timestamp(new Date().getTime()));
								productSerie2.setObservation("Entra a cuarentena por fecha de expiracion vencida");
								List<ProductSerie> seriesSave = new ArrayList<ProductSerie>();
								// sacar del stock
								seriesSave.add(productSerie2);
								saveEgressStock(transaction, seriesSave);

								// ingresar a cuarentena
								transaction.setCategory(category);
								ProductSerie productSerie= (ProductSerie) productSerie2.clone();
								
								productSerie.setId(null);
								productSerie.setCategory(category);
								productSerie.setEndingDate(null);
								productSerie.setCreationDate(new Timestamp(new Date().getTime()));
								productSerie.setObservation(null);
								productSerie.setQuarantineReason("Entra a cuarentena por fecha de expiracion vencida");
								seriesSave = new ArrayList<ProductSerie>();
								seriesSave.add(productSerie);
								saveTransactionStock(transaction, seriesSave);
								quarantines.add(productSerie);
							} else {
								series.add(productSerie2);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION,this.getClass(), getMethodName(), e.getMessage()), null);
				}
			}
			if (!quarantines.isEmpty()) {
				ServiceMailDispatcher.sendQuarantineDataMail(enterprise, quarantines, "Cuarentena");
				// sendNotificationMailQuarine(productSeries);
			}
			if (!series.isEmpty()) {
				ServiceMailDispatcher.sendPendingDataMail(enterprise, series, "Productos a vencer");
				// sendNotificationMailStock(productSeries);
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), e.getMessage()), null);
		}
	}
	
	@Override
	public void runAutomaticProcessMetrologicalControl()throws GeneralException{
		try {
			List<MetrologicalControl> controls = searchMetrologicalControl();
			List<MetrologicalControlHistory> histories = new ArrayList<MetrologicalControlHistory>();
			List<MetrologicalControlHistory> quarantines = new ArrayList<MetrologicalControlHistory>();
			Enterprise enterprise = utilsEJB.loadEnterprisebyId(Enterprise.TURBINES);
			Date date = new Date(); 
			Timestamp today =  new Timestamp(new java.util.Date().getTime());
			Calendar calendar = Calendar.getInstance();
			calendar.setTime(today);
			calendar.add(Calendar.DAY_OF_YEAR, 5);
			Timestamp timestampOldDate = new Timestamp(calendar.getTimeInMillis());
			Category category = loadCategorybyId(Category.QUARANTINE);
			for (MetrologicalControl control : controls) {
				StringBuilder sqlBuilder = new StringBuilder("SELECT m FROM MetrologicalControlHistory m WHERE m.metrologicalControl.id="+ control.getId() + "  m.expirationDate <='"+timestampOldDate+"' AND m.category.id =" +Category.METEOROLOGICAL_CONTROL);
				Query query = null;
				try {
					System.out.println("query:********" + sqlBuilder.toString());
					query = createQuery(sqlBuilder.toString());
					List<MetrologicalControlHistory>  histories2 = query.setHint("toplink.refresh", "true").getResultList();
					for (MetrologicalControlHistory history : histories2) {
						if (history.getExpirationDate() != null) {
							if (EjbUtils.getBeginningDate(history.getExpirationDate()).equals(EjbUtils.getBeginningDate(date))|| (EjbUtils.getBeginningDate(history.getExpirationDate()).before(EjbUtils.getBeginningDate(date)))) {
								// ingresar control metrologico a quarentena
								history.setObservation("Entro a cuarentena Fecha de Calibracion Vencida");
								history.setCategory(category);
								EJBRequest request = new EJBRequest();
								request.setParam(history);
								saveMetrologicalControlHistory(request);
								quarantines.add(history);
	
							} else {
								histories.add(history);
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
					throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION,this.getClass(), getMethodName(), e.getMessage()), null);
				}
			}
			if (!quarantines.isEmpty()) {
				ServiceMailDispatcher.sendQuarantineDataMailControl(enterprise, quarantines, "Cuarentena");
			}
			if (!histories.isEmpty()) {
				ServiceMailDispatcher.sendPendingDataMailControl(enterprise, histories, "Productos a vencer");
			}
		} catch (Exception e) {
			
			e.printStackTrace();
			throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), e.getMessage()), null);
		}
	}
	
	public List<Product> getProductByCategoryId(Long categoryId) throws GeneralException{
		List<Product> products = new ArrayList<Product>();
		Timestamp today = new Timestamp(new java.util.Date().getTime());
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(today);
		calendar.add(Calendar.DAY_OF_YEAR, 5);
		Timestamp timestampOldDate = new Timestamp(calendar.getTimeInMillis());
		StringBuilder sqlBuilder = new StringBuilder("SELECT DISTINCT s.product FROM ProductSerie s WHERE s.endingDate is null AND s.expirationDate<='"+timestampOldDate+"' AND s.category.id in (" + Category.STOCK+ ","+Category.METEOROLOGICAL_CONTROL+")");
	    Query query = null;
	    try {
	        query = createQuery(sqlBuilder.toString());
	        products = query.setHint("toplink.refresh", "true").getResultList();
	    } catch (Exception e) {
	    	 e.printStackTrace();
	        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
	    }
	    return products;
	}

	
	@Override
	public List<MetrologicalControl> searchMetrologicalControl() throws GeneralException,  EmptyListException{
		 List<MetrologicalControl> metrologicalControls = new ArrayList<MetrologicalControl>();
			
		    StringBuilder sqlBuilder = new StringBuilder("SELECT m FROM MetrologicalControl m " );
		    try {
		         Query query = entityManager.createQuery(sqlBuilder.toString());
		         metrologicalControls = query.setHint("toplink.refresh", "true").getResultList();
		    } catch (Exception e) {
		        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
		    }
		    if (metrologicalControls.isEmpty()) {
		        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
		    }
		    return metrologicalControls;
	}

	@Override
	public TransactionType loadTransactionTypebyId(Long id)throws NullParameterException, RegisterNotFoundException, GeneralException {
		 if (id == null) {
	            throw new NullParameterException(" parameter id cannot be null in loadTransactionTypebyId.");
	      }
		 TransactionType transactionType = null;
	      try {
	          Query query = createQuery("SELECT tt FROM TransactionType tt WHERE tt.id = ?1");
	          query.setParameter("1", id);
	          transactionType = (TransactionType) query.setHint("toplink.refresh", "true").getSingleResult();
	      } catch (NoResultException ex) {
	            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_REGISTER_NOT_FOUND_EXCEPTION, TransactionType.class.getSimpleName(), "loadTransactionTypebyId", TransactionType.class.getSimpleName(), null), ex);
	      } catch (Exception ex) {
	            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), ex);
	      }
	      return transactionType;
	}
	
	@Override
	public MetrologicalControl loadMetrologicalControlByInstrument(String instrumentId)	throws RegisterNotFoundException, NullParameterException, GeneralException {
		MetrologicalControl metrologicalControl = new MetrologicalControl();
		if (instrumentId == null) {
			throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "instrumentId"),	null);
		}

		try {
			Query query = createQuery("SELECT c FROM MetrologicalControl c WHERE c.instrument = ?1");
			query.setParameter("1", instrumentId);
			metrologicalControl = (MetrologicalControl) query.getSingleResult();
		} catch (Exception ex) {
			throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(),	getMethodName(), ex.getMessage()), null);
		}
		if (metrologicalControl == null) {
			throw new RegisterNotFoundException(logger,	sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
		}
		return metrologicalControl;

	}
	
	
	@Override
	public List<MetrologicalControlHistory> getMetrologicalControlDefeated(int dayEnding) throws GeneralException, NullParameterException, EmptyListException{
		 List<MetrologicalControlHistory> controlHistories = new ArrayList<MetrologicalControlHistory>();
		 List<MetrologicalControl> controls = new ArrayList<MetrologicalControl>();
		 Timestamp today =  new Timestamp(new java.util.Date().getTime());
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(today);
		 calendar.add(Calendar.DAY_OF_MONTH, dayEnding);
		 Timestamp timestampOldDate = new Timestamp(calendar.getTimeInMillis());
		 EJBRequest request = new EJBRequest();
         Map<String, Object> params = new HashMap<String, Object>();
         request.setParams(params);
         request.setParam(true);

		 controls = searchMetrologicalControl(request);
		 for (MetrologicalControl control : controls) {
			try {
				MetrologicalControlHistory history = loadLastMetrologicalControlHistoryByMetrologicalControlId(control.getId());
				 if(history.getExpirationDate().getTime()< timestampOldDate.getTime()) {
					 controlHistories.add(history) ;
				 }
			} catch (RegisterNotFoundException e) {
			}
		}
	    if (controlHistories.isEmpty()) {
	        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
	    }
	    return controlHistories;
	}

	
}
