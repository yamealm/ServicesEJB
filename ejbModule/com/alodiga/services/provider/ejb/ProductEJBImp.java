package com.alodiga.services.provider.ejb;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.interceptor.Interceptors;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import com.alodiga.services.provider.commons.ejbs.ProductEJB;
import com.alodiga.services.provider.commons.ejbs.ProductEJBLocal;
import com.alodiga.services.provider.commons.exceptions.EmptyListException;
import com.alodiga.services.provider.commons.exceptions.GeneralException;
import com.alodiga.services.provider.commons.exceptions.NullParameterException;
import com.alodiga.services.provider.commons.exceptions.RegisterNotFoundException;
import com.alodiga.services.provider.commons.genericEJB.AbstractSPEJB;
import com.alodiga.services.provider.commons.genericEJB.EJBRequest;
import com.alodiga.services.provider.commons.genericEJB.SPContextInterceptor;
import com.alodiga.services.provider.commons.genericEJB.SPLoggerInterceptor;
import com.alodiga.services.provider.commons.models.Category;
import com.alodiga.services.provider.commons.models.MetrologicalControlHistory;
import com.alodiga.services.provider.commons.models.Product;
import com.alodiga.services.provider.commons.models.ProductSerie;
import com.alodiga.services.provider.commons.models.Provider;
import com.alodiga.services.provider.commons.models.TransactionType;
import com.alodiga.services.provider.commons.utils.EjbConstants;
import com.alodiga.services.provider.commons.utils.EjbUtils;
import com.alodiga.services.provider.commons.utils.QueryConstants;

@Interceptors({SPLoggerInterceptor.class, SPContextInterceptor.class})
@Stateless(name = EjbConstants.PRODUCT_EJB, mappedName = EjbConstants.PRODUCT_EJB)
@TransactionManagement(TransactionManagementType.BEAN)
public class ProductEJBImp extends AbstractSPEJB implements ProductEJB, ProductEJBLocal {

    private static final Logger logger = Logger.getLogger(ProductEJBImp.class);

    public Category deleteCategory(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }

    public Product deleteProduct(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }


    public Provider deleteProvider(EJBRequest request) throws GeneralException, NullParameterException {
        return null;
    }


    public Product enableProduct(EJBRequest request) throws GeneralException, NullParameterException, RegisterNotFoundException {
        return (Product) saveEntity(request, logger, getMethodName());
    }

    public List<Product> filterProducts(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {
        if (request == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "request"), null);
        }

        Map<String, Object> params = request.getParams();
        if (params == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "params"), null);
        }
        Boolean isFilter = true;
        Map orderField = new HashMap();
        orderField.put(Product.NAME, QueryConstants.ORDER_DESC);
        return (List<Product>) createSearchQuery(Product.class, request, orderField, logger, getMethodName(), "customers", isFilter);
    }

    
    public List<Category> getCategories(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {

        return (List<Category>) listEntities(Category.class, request, logger, getMethodName());
    }


    public List<Product> getProducts(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {

        return (List<Product>) listEntities(Product.class, request, logger, getMethodName());
    }

    public List<Provider> getProviders(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {

        return (List<Provider>) listEntities(Provider.class, request, logger, getMethodName());
    }

    public Category loadCategory(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {

        return (Category) loadEntity(Category.class, request, logger, getMethodName());
    }

   
    public Product loadProduct(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {
        return (Product) loadEntity(Product.class, request, logger, getMethodName());
    }

    public Product loadProductById(Long productId) throws GeneralException, RegisterNotFoundException, NullParameterException {
        if (productId == null) {
            throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "productId"), null);
        }
        Product product = new Product();
        try {
            Query query = createQuery("SELECT p FROM Product p WHERE p.id = ?1");
            query.setParameter("1", productId);
            product = (Product) query.getSingleResult();
        } catch (Exception ex) {
            throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), ex.getMessage()), null);
        }
        if (product == null) {
            throw new RegisterNotFoundException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
        }
        return product;
    }

    public Provider loadProvider(EJBRequest request) throws GeneralException, RegisterNotFoundException, NullParameterException {

        return (Provider) loadEntity(Provider.class, request, logger, getMethodName());
    }

    public Category saveCategory(EJBRequest request) throws GeneralException, NullParameterException {
        return (Category) saveEntity(request, logger, getMethodName());
    }

    
    public Product saveProduct(EJBRequest request) throws GeneralException, NullParameterException {

        return (Product) saveEntity(request, logger, getMethodName());
    }

    public Provider saveProvider(EJBRequest request) throws GeneralException, NullParameterException {

        return (Provider) saveEntity(request, logger, getMethodName());
    }
    
    
	@Override
	public List<ProductSerie> searchProductSerie(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException{
		 List<ProductSerie> productSeries = new ArrayList<ProductSerie>();
	    Map<String, Object> params = request.getParams();
	
	    StringBuilder sqlBuilder = new StringBuilder("SELECT p FROM ProductSerie p WHERE p.creationDate BETWEEN ?1 AND ?2");
	    if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
	        throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
	    }
	
	    if (params.containsKey(QueryConstants.PARAM_PROVIDER_ID)) {
	        sqlBuilder.append(" AND p.provider.id=").append(params.get(QueryConstants.PARAM_PROVIDER_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_PRODUCT_ID)) {
	        sqlBuilder.append(" AND p.product.id=").append(params.get(QueryConstants.PARAM_PRODUCT_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_CUSTOMER_ID)) {
	        sqlBuilder.append(" AND p.customer.id=").append(params.get(QueryConstants.PARAM_CUSTOMER_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_CONDITION_ID)) {
	        sqlBuilder.append(" AND p.condition.id=").append(params.get(QueryConstants.PARAM_CONDITION_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_WORK_ORDER)) {
	        sqlBuilder.append(" AND p.orderWord=").append(params.get(QueryConstants.PARAM_WORK_ORDER));
	    }
	    if (params.containsKey(QueryConstants.PARAM_CATEGORY_ID)) {
	    	  sqlBuilder.append(" AND p.category.id=").append(params.get(QueryConstants.PARAM_CATEGORY_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_TRANSACTION_TYPE_ID)) { //pendiente
	    	Long transactionType = (Long) params.get(QueryConstants.PARAM_TRANSACTION_TYPE_ID);
			if (transactionType.equals(TransactionType.ADD))
				sqlBuilder.append(" AND p.endingTransactionId.id is null");
			else if (transactionType.equals(TransactionType.REMOVE))
				sqlBuilder.append(" AND p.endingTransactionId.id is not null");
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
	public List<ProductSerie> getProductDefeated() throws GeneralException, NullParameterException, EmptyListException{
		 List<ProductSerie> productSeries = new ArrayList<ProductSerie>();
		 Timestamp today =  new Timestamp(new java.util.Date().getTime());
	    StringBuilder sqlBuilder = new StringBuilder("SELECT p FROM ProductSerie p WHERE p.expirationDate <=CURRENT_DATE");
	    Query query = null;
	    try {
	        System.out.println("query:********"+sqlBuilder.toString());
	        query = createQuery(sqlBuilder.toString());
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
	public List<ProductSerie> getProductDefeated(int dayEnding) throws GeneralException, NullParameterException, EmptyListException{
		 List<ProductSerie> productSeries = new ArrayList<ProductSerie>();
		 Timestamp today =  new Timestamp(new java.util.Date().getTime());
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(today);
		 calendar.add(Calendar.DAY_OF_MONTH, dayEnding);
		 Timestamp timestampOldDate = new Timestamp(calendar.getTimeInMillis());
	    StringBuilder sqlBuilder = new StringBuilder("SELECT p FROM ProductSerie p WHERE p.expirationDate <= '"+ timestampOldDate+"'");
	    Query query = null;
	    try {
	        System.out.println("query:********"+sqlBuilder.toString());
	        query = createQuery(sqlBuilder.toString());
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
	public List<ProductSerie> getProductDefeatedCure(int dayEnding) throws GeneralException, NullParameterException, EmptyListException{
		 List<ProductSerie> productSeries = new ArrayList<ProductSerie>();
		 Timestamp today =  new Timestamp(new java.util.Date().getTime());
		 Calendar calendar = Calendar.getInstance();
		 calendar.setTime(today);
		 calendar.add(Calendar.DAY_OF_MONTH, dayEnding);
		 Timestamp timestampOldDate = new Timestamp(calendar.getTimeInMillis());
	    StringBuilder sqlBuilder = new StringBuilder("SELECT p FROM ProductSerie p WHERE p.cure >= '"+ timestampOldDate+"'");
	    Query query = null;
	    try {
	        System.out.println("query:********"+sqlBuilder.toString());
	        query = createQuery(sqlBuilder.toString());
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
	public List<MetrologicalControlHistory> searchMetrologicalControl(EJBRequest request) throws GeneralException, NullParameterException, EmptyListException{
		 List<MetrologicalControlHistory> metrologicalControls = new ArrayList<MetrologicalControlHistory>();
	    Map<String, Object> params = request.getParams();
	  //revisar query para que devuelva el ultimo
	    StringBuilder sqlBuilder = new StringBuilder("SELECT h FROM MetrologicalControlHistory h, MetrologicalControl m WHERE h.metrologicalControl.id=m.id AND m.expirationDate BETWEEN ?1 AND ?2");
	    if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
	        throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
	    }
	
	    if (params.containsKey(QueryConstants.PARAM_BRAUND_ID)) {
	        sqlBuilder.append(" AND h.metrologicalControl.braund.id=").append(params.get(QueryConstants.PARAM_BRAUND_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_MODEL_ID)) {
	        sqlBuilder.append(" AND h.metrologicalControl.model.id=").append(params.get(QueryConstants.PARAM_MODEL_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_ENTER_CALIBRATION_ID)) {
	        sqlBuilder.append(" AND h.metrologicalControl.enterCalibration.id=").append(params.get(QueryConstants.PARAM_ENTER_CALIBRATION_ID));
	    }
	    if (params.containsKey(QueryConstants.PARAM_SERIAL)) {
	        sqlBuilder.append(" AND h.metrologicalControl.serie=").append(params.get(QueryConstants.PARAM_SERIAL));
	    }
	    sqlBuilder.append(" AND ORDER BY ID DESC");
	    Query query = null;
	    try {
	        System.out.println("query:********"+sqlBuilder.toString());
	        query = createQuery(sqlBuilder.toString());
	        query.setParameter("1", EjbUtils.getBeginningDate((Date) params.get(QueryConstants.PARAM_BEGINNING_DATE)));
	        query.setParameter("2", EjbUtils.getEndingDate((Date) params.get(QueryConstants.PARAM_ENDING_DATE)));
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
	
	public List<Product> getProductsByParams(EJBRequest request) throws GeneralException, EmptyListException, NullParameterException {

		 List<Product> products = new ArrayList<Product>();
		    Map<String, Object> params = request.getParams();
		
		    StringBuilder sqlBuilder = new StringBuilder("SELECT p FROM Product p WHERE p.enabled =1");
		    if (!params.containsKey(QueryConstants.PARAM_BEGINNING_DATE) || !params.containsKey(QueryConstants.PARAM_ENDING_DATE)) {
		        throw new NullParameterException(sysError.format(EjbConstants.ERR_NULL_PARAMETER, this.getClass(), getMethodName(), "beginningDate & endingDate"), null);
		    }
		
		    if (params.containsKey(QueryConstants.PARAM_FILTER_TEXT)) {
		        sqlBuilder.append(" AND ").append(params.get(QueryConstants.PARAM_FILTER_TEXT));
		    }
		    
		    Query query = null;
		    try {
		        System.out.println("query:********"+sqlBuilder.toString());
		        query = createQuery(sqlBuilder.toString());
		        if (request.getLimit() != null && request.getLimit() > 0) {
		            query.setMaxResults(request.getLimit());
		        }
		        if (request.getFirst() != null && request.getFirst() >= 0) {
		            query.setFirstResult(request.getFirst());
		        }
		        products = query.setHint("toplink.refresh", "true").getResultList();
		    } catch (Exception e) {
		        e.printStackTrace();
		        throw new GeneralException(logger, sysError.format(EjbConstants.ERR_GENERAL_EXCEPTION, this.getClass(), getMethodName(), e.getMessage()), null);
		    }
		    if (products.isEmpty()) {
		        throw new EmptyListException(logger, sysError.format(EjbConstants.ERR_EMPTY_LIST_EXCEPTION, this.getClass(), getMethodName()), null);
		    }
		    return products;
    }

}
