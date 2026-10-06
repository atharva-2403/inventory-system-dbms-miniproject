package com.example.inventory.service;
import com.example.inventory.dao.OrderDao;
import com.example.inventory.model.*;
import java.math.BigDecimal;import java.sql.Date;import java.sql.SQLException;import java.util.List;import java.util.Optional;
import java.util.HashSet;import java.util.Set;
public final class OrderService {
 public static class ValidationException extends RuntimeException{public ValidationException(String m){super(m);}}
 private final OrderDao dao;public OrderService(OrderDao d){dao=d;}
 public List<PurchaseOrder> purchases()throws SQLException{return dao.purchases();}public Optional<PurchaseOrder> purchase(int id)throws SQLException{check(id);return dao.purchase(id);}
 public PurchaseOrder createPurchase(int s,Date d,Integer m,List<PurchaseOrder.Detail>x)throws SQLException{if(s<=0||d==null||x==null||x.isEmpty())throw new ValidationException("Supplier, order date, and at least one detail are required.");checkEmployee(m);Set<Integer> products=new HashSet<>();for(var a:x){if(a==null)throw new ValidationException("Order details cannot be null.");detail(a.productId(),a.quantity(),a.unitPrice());if(!products.add(a.productId()))throw new ValidationException("An order cannot contain duplicate products.");}return dao.createPurchase(s,d,m,x);}
 public void receive(int id,Integer e)throws SQLException{check(id);checkEmployee(e);dao.receivePurchase(id,e);}
 public List<SalesOrder> sales()throws SQLException{return dao.sales();}public Optional<SalesOrder> sale(int id)throws SQLException{check(id);return dao.sale(id);}
 public SalesOrder createSale(int c,Date d,List<SalesOrder.Detail>x)throws SQLException{if(c<=0||d==null||x==null||x.isEmpty())throw new ValidationException("Customer, order date, and at least one detail are required.");Set<Integer> products=new HashSet<>();for(var a:x){if(a==null)throw new ValidationException("Order details cannot be null.");detail(a.productId(),a.quantity(),a.unitPrice());if(!products.add(a.productId()))throw new ValidationException("An order cannot contain duplicate products.");}return dao.createSale(c,d,x);}
 public void complete(int id,Integer e)throws SQLException{check(id);checkEmployee(e);dao.completeSale(id,e);}public List<StockTransaction> transactions()throws SQLException{return dao.transactions();}
 private void detail(int p,int q,BigDecimal price){if(p<=0||q<=0||price==null||price.signum()<0)throw new ValidationException("Each detail requires a product, positive quantity, and non-negative unit price.");}private void check(int i){if(i<=0)throw new ValidationException("ID must be positive.");}private void checkEmployee(Integer employee){if(employee!=null&&employee<=0)throw new ValidationException("Employee ID must be positive.");}
}
