package com.example.inventory.dao;

import com.example.inventory.config.Database;
import com.example.inventory.model.*;
import com.example.inventory.service.OrderWorkflowValidator;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

public final class OrderDao {
    private final Database database;
    public OrderDao(Database d){database=d;}
    public List<PurchaseOrder> purchases()throws SQLException{try(var c=database.getConnection()){return purchaseList(c,null);}}
    public Optional<PurchaseOrder> purchase(int id)throws SQLException{try(var c=database.getConnection()){return purchaseList(c,id).stream().findFirst();}}
    private List<PurchaseOrder> purchaseList(Connection c,Integer only)throws SQLException{
        String q="SELECT order_id,supplier_id,order_date,status,approved_by_manager_id FROM purchase_orders"+(only==null?"":" WHERE order_id=?")+" ORDER BY order_date DESC,order_id DESC";List<PurchaseOrder> o=new ArrayList<>();
        try(var s=c.prepareStatement(q)){if(only!=null)s.setInt(1,only);try(var r=s.executeQuery()){while(r.next()){int id=r.getInt(1);List<PurchaseOrder.Detail>d=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;try(var x=c.prepareStatement("SELECT order_detail_id,product_id,quantity,unit_price FROM purchase_order_details WHERE order_id=? ORDER BY order_detail_id")){x.setInt(1,id);try(var z=x.executeQuery()){while(z.next()){BigDecimal p=z.getBigDecimal(4);d.add(new PurchaseOrder.Detail(z.getInt(1),z.getInt(2),z.getInt(3),p));total=total.add(p.multiply(BigDecimal.valueOf(z.getInt(3))));}}}o.add(new PurchaseOrder(id,r.getInt(2),r.getDate(3).toLocalDate(),r.getString(4),(Integer)r.getObject(5),total,d));}}}return o;
    }
    public PurchaseOrder createPurchase(int supplier,Date date,Integer manager,List<PurchaseOrder.Detail> details)throws SQLException{
        try(Connection c=database.getConnection()){c.setAutoCommit(false);try{
            // Enforce the Supplier -- Product M:N relationship during order creation.
            // The frontend filter is only a convenience; this check is authoritative.
            try(var check=c.prepareStatement("SELECT 1 FROM supplier_products WHERE supplier_id=? AND product_id=? FOR UPDATE")){
                for(var detail:details){
                    check.setInt(1,supplier); check.setInt(2,detail.productId());
                    try(var result=check.executeQuery()){
                        if(!result.next()) throw new IllegalStateException(
                            "Supplier " + supplier + " is not linked to product " + detail.productId() + ". Link the product to this supplier before purchasing it.");
                    }
                }
            }
            int id;try(var s=c.prepareStatement("INSERT INTO purchase_orders(supplier_id,order_date,status,approved_by_manager_id) VALUES(?,?, 'PENDING',?)",Statement.RETURN_GENERATED_KEYS)){s.setInt(1,supplier);s.setDate(2,date);if(manager==null)s.setNull(3,Types.INTEGER);else s.setInt(3,manager);s.executeUpdate();try(var k=s.getGeneratedKeys()){k.next();id=k.getInt(1);}}insertPurchaseDetails(c,id,details);c.commit();return purchase(id).orElseThrow();}catch(Exception e){c.rollback();throw e;}}
    }
    private void insertPurchaseDetails(Connection c,int id,List<PurchaseOrder.Detail>d)throws SQLException{try(var s=c.prepareStatement("INSERT INTO purchase_order_details(order_id,product_id,quantity,unit_price) VALUES(?,?,?,?)")){for(var x:d){s.setInt(1,id);s.setInt(2,x.productId());s.setInt(3,x.quantity());s.setBigDecimal(4,x.unitPrice());s.addBatch();}s.executeBatch();}}
    public void cancelPurchase(int id)throws SQLException{cancelOrder("purchase_orders","order_id",id,"PURCHASE");}
    private void cancelOrder(String table,String idColumn,int id,String kind)throws SQLException{
        try(Connection c=database.getConnection()){
            c.setAutoCommit(false);
            try{
                String status;
                try(var s=c.prepareStatement("SELECT status FROM "+table+" WHERE "+idColumn+"=? FOR UPDATE")){
                    s.setInt(1,id);
                    try(var r=s.executeQuery()){if(!r.next())throw new NoSuchElementException();status=r.getString(1);}
                }
                OrderWorkflowValidator.validateCancelStatus(status,kind);
                try(var s=c.prepareStatement("UPDATE "+table+" SET status='CANCELLED' WHERE "+idColumn+"=?")){
                    s.setInt(1,id);s.executeUpdate();
                }
                c.commit();
            }catch(Exception e){c.rollback();throw e;}
        }
    }
    public void receivePurchase(int id,Integer employee)throws SQLException{try(Connection c=database.getConnection()){c.setAutoCommit(false);try{String status;try(var s=c.prepareStatement("SELECT status FROM purchase_orders WHERE order_id=? FOR UPDATE")){s.setInt(1,id);try(var r=s.executeQuery()){if(!r.next())throw new NoSuchElementException();status=r.getString(1);}}OrderWorkflowValidator.validateReceiveStatus(status);try(var s=c.prepareStatement("SELECT product_id,quantity FROM purchase_order_details WHERE order_id=?")){s.setInt(1,id);try(var r=s.executeQuery()){while(r.next()){int p=r.getInt(1),q=r.getInt(2);try(var u=c.prepareStatement("UPDATE products SET quantity_in_stock=quantity_in_stock+? WHERE product_id=?")){u.setInt(1,q);u.setInt(2,p);if(u.executeUpdate()==0)throw new SQLException("Product not found");}insertTransaction(c,p,"PURCHASE_RECEIPT",q,employee,id,null);}}}try(var s=c.prepareStatement("UPDATE purchase_orders SET status='RECEIVED' WHERE order_id=?")){s.setInt(1,id);s.executeUpdate();}c.commit();}catch(Exception e){c.rollback();throw e;}}}
    public List<SalesOrder> sales()throws SQLException{try(var c=database.getConnection()){return salesList(c,null);}}
    public Optional<SalesOrder> sale(int id)throws SQLException{try(var c=database.getConnection()){return salesList(c,id).stream().findFirst();}}
    private List<SalesOrder> salesList(Connection c,Integer only)throws SQLException{String q="SELECT sales_order_id,customer_id,order_date,status FROM sales_orders"+(only==null?"":" WHERE sales_order_id=?")+" ORDER BY order_date DESC,sales_order_id DESC";List<SalesOrder>o=new ArrayList<>();try(var s=c.prepareStatement(q)){if(only!=null)s.setInt(1,only);try(var r=s.executeQuery()){while(r.next()){int id=r.getInt(1);List<SalesOrder.Detail>d=new ArrayList<>();BigDecimal total=BigDecimal.ZERO;try(var x=c.prepareStatement("SELECT sales_detail_id,product_id,quantity,unit_price FROM sales_order_details WHERE sales_order_id=? ORDER BY sales_detail_id")){x.setInt(1,id);try(var z=x.executeQuery()){while(z.next()){BigDecimal p=z.getBigDecimal(4);d.add(new SalesOrder.Detail(z.getInt(1),z.getInt(2),z.getInt(3),p));total=total.add(p.multiply(BigDecimal.valueOf(z.getInt(3))));}}}o.add(new SalesOrder(id,r.getInt(2),r.getDate(3).toLocalDate(),r.getString(4),total,d));}}}return o;}
    public SalesOrder createSale(int customer,Date date,List<SalesOrder.Detail>details)throws SQLException{try(Connection c=database.getConnection()){c.setAutoCommit(false);try{int id;try(var s=c.prepareStatement("INSERT INTO sales_orders(customer_id,order_date,status) VALUES(?,?, 'PENDING')",Statement.RETURN_GENERATED_KEYS)){s.setInt(1,customer);s.setDate(2,date);s.executeUpdate();try(var k=s.getGeneratedKeys()){k.next();id=k.getInt(1);}}try(var s=c.prepareStatement("INSERT INTO sales_order_details(sales_order_id,product_id,quantity,unit_price) VALUES(?,?,?,?)")){for(var x:details){s.setInt(1,id);s.setInt(2,x.productId());s.setInt(3,x.quantity());s.setBigDecimal(4,x.unitPrice());s.addBatch();}s.executeBatch();}c.commit();return sale(id).orElseThrow();}catch(Exception e){c.rollback();throw e;}}}
    public void cancelSale(int id)throws SQLException{cancelOrder("sales_orders","sales_order_id",id,"SALES");}
    public void completeSale(int id,Integer employee)throws SQLException{try(Connection c=database.getConnection()){c.setAutoCommit(false);try{String status;try(var s=c.prepareStatement("SELECT status FROM sales_orders WHERE sales_order_id=? FOR UPDATE")){s.setInt(1,id);try(var r=s.executeQuery()){if(!r.next())throw new NoSuchElementException();status=r.getString(1);}}OrderWorkflowValidator.validateCompleteStatus(status);try(var s=c.prepareStatement("SELECT product_id,quantity FROM sales_order_details WHERE sales_order_id=? ORDER BY product_id FOR UPDATE")){s.setInt(1,id);try(var r=s.executeQuery()){while(r.next()){int p=r.getInt(1),q=r.getInt(2);int stock;try(var x=c.prepareStatement("SELECT quantity_in_stock FROM products WHERE product_id=? FOR UPDATE")){x.setInt(1,p);try(var z=x.executeQuery()){if(!z.next())throw new SQLException("Product not found");stock=z.getInt(1);}}if(stock<q)throw new IllegalStateException("Insufficient stock for product "+p);try(var u=c.prepareStatement("UPDATE products SET quantity_in_stock=quantity_in_stock-? WHERE product_id=?")){u.setInt(1,q);u.setInt(2,p);u.executeUpdate();}insertTransaction(c,p,"SALE",-q,employee,null,id);}}}try(var s=c.prepareStatement("UPDATE sales_orders SET status='COMPLETED' WHERE sales_order_id=?")){s.setInt(1,id);s.executeUpdate();}c.commit();}catch(Exception e){c.rollback();throw e;}}}
    public List<StockTransaction> transactions()throws SQLException{List<StockTransaction>o=new ArrayList<>();String q="SELECT transaction_id,product_id,transaction_type,quantity,transaction_date,recorded_by_employee_id,purchase_order_id,sales_order_id FROM stock_transactions ORDER BY transaction_date DESC,transaction_id DESC";try(var c=database.getConnection();var s=c.prepareStatement(q);var r=s.executeQuery()){while(r.next())o.add(new StockTransaction(r.getInt(1),r.getInt(2),r.getString(3),r.getInt(4),r.getTimestamp(5).toLocalDateTime(),(Integer)r.getObject(6),(Integer)r.getObject(7),(Integer)r.getObject(8)));}return o;}
    private void insertTransaction(Connection c,int p,String type,int q,Integer employee,Integer po,Integer so)throws SQLException{try(var s=c.prepareStatement("INSERT INTO stock_transactions(product_id,transaction_type,quantity,recorded_by_employee_id,purchase_order_id,sales_order_id) VALUES(?,?,?,?,?,?)")){s.setInt(1,p);s.setString(2,type);s.setInt(3,q);if(employee==null)s.setNull(4,Types.INTEGER);else s.setInt(4,employee);if(po==null)s.setNull(5,Types.INTEGER);else s.setInt(5,po);if(so==null)s.setNull(6,Types.INTEGER);else s.setInt(6,so);s.executeUpdate();}}
}
