package com.example.inventory.controller;

import com.example.inventory.model.*;
import com.example.inventory.server.HttpUtil;
import com.example.inventory.service.MasterDataService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpExchange;
import java.io.IOException;
import java.time.LocalDate;
import java.sql.SQLException;

public final class MasterDataController {
    private final MasterDataService service; private final ObjectMapper mapper; private final String prefix, kind;
    public MasterDataController(MasterDataService s,ObjectMapper m,String prefix,String kind){service=s;mapper=m;this.prefix=prefix;this.kind=kind;}
    public void handle(HttpExchange e)throws IOException{
        try{String method=e.getRequestMethod().toUpperCase();String path=e.getRequestURI().getPath();
            if("OPTIONS".equals(method)){HttpUtil.sendEmpty(e,204);return;}
            if(path.contains("/contacts")){handleContacts(e,method,path);return;}
            if(handleExtras(e,method,path))return;
            Integer id=parseId(path);
            switch(method){case "GET"->get(e,id);case "POST"->post(e,id);case "PUT"->put(e,id);case "DELETE"->del(e,id);default->HttpUtil.sendError(e,405,"METHOD_NOT_ALLOWED","Method is not supported.");}
        }catch(MasterDataService.ValidationException x){HttpUtil.sendError(e,400,"VALIDATION_ERROR",x.getMessage());}
        catch(SQLException x){if(x.getErrorCode()==1062)HttpUtil.sendError(e,409,"DUPLICATE","This value already exists.");else if(x.getErrorCode()==1451)HttpUtil.sendError(e,409,"REFERENCED_RECORD","Record is still referenced.");else if(x.getErrorCode()==1452)HttpUtil.sendError(e,400,"INVALID_REFERENCE","Referenced record does not exist.");else HttpUtil.sendError(e,500,"DATABASE_ERROR","The database operation could not be completed.");}
        catch(Exception x){HttpUtil.sendError(e,400,"INVALID_REQUEST","Request JSON is invalid.");}
    }
    // Handles /api/{suppliers|customers|employees}/{id}/contacts[/{contactId}]
    private void handleContacts(HttpExchange e,String method,String path)throws IOException,SQLException{
        if(kind.equals("warehouse")){HttpUtil.sendError(e,404,"NOT_FOUND","Contacts are not supported for this resource.");return;}
        String[] p=path.substring(prefix.length()).split("/");   // e.g. ["", "5", "contacts", "9"]
        if((p.length!=3&&p.length!=4)||!p[0].isEmpty()||!p[2].equals("contacts")){HttpUtil.sendError(e,404,"NOT_FOUND","Unknown path.");return;}
        int ownerId=num(p[1]);
        if(p.length==3&&method.equals("GET")){HttpUtil.sendJson(e,200,service.contacts(kind,ownerId));return;}
        if(p.length==3&&method.equals("POST")){
            JsonNode n=mapper.readTree(HttpUtil.readBody(e));
            HttpUtil.sendJson(e,201,service.addContact(kind,ownerId,t(n,"value")));return;
        }
        if(p.length==4&&method.equals("DELETE")){
            if(service.deleteContact(kind,ownerId,num(p[3])))HttpUtil.sendEmpty(e,204);
            else HttpUtil.sendError(e,404,"NOT_FOUND","Contact was not found.");
            return;
        }
        HttpUtil.sendError(e,405,"METHOD_NOT_ALLOWED","Method is not supported.");
    }
    private int num(String v){try{return Integer.parseInt(v);}catch(NumberFormatException x){throw new MasterDataService.ValidationException("ID must be a number.");}}
    // GET /api/employees/managers | GET,PUT /api/employees/{id}/role
    // GET,POST /api/suppliers/{id}/products | DELETE /api/suppliers/{id}/products/{productId}
    private boolean handleExtras(HttpExchange e,String method,String path)throws IOException,SQLException{
        String rest=path.substring(prefix.length());
        if(kind.equals("employee")&&rest.equals("/managers")){
            if(method.equals("GET"))HttpUtil.sendJson(e,200,service.managers());
            else HttpUtil.sendError(e,405,"METHOD_NOT_ALLOWED","Method is not supported.");
            return true;
        }
        String[] p=rest.split("/");
        if(p.length<3||!p[0].isEmpty())return false;
        if(kind.equals("employee")&&p.length==3&&p[2].equals("role")){
            int id=num(p[1]);
            if(service.employee(id).isEmpty()){HttpUtil.sendError(e,404,"NOT_FOUND","Record was not found.");return true;}
            if(method.equals("GET")){HttpUtil.sendJson(e,200,service.role(id));return true;}
            if(method.equals("PUT")){
                JsonNode n=mapper.readTree(HttpUtil.readBody(e));
                HttpUtil.sendJson(e,200,service.setRole(id,new EmployeeRole(t(n,"type"),t(n,"department"),ni(n,"teamSize"),ni(n,"warehouseId"),t(n,"shiftTiming"))));
                return true;
            }
            HttpUtil.sendError(e,405,"METHOD_NOT_ALLOWED","Method is not supported.");
            return true;
        }
        if(kind.equals("supplier")&&(p.length==3||p.length==4)&&p[2].equals("products")){
            int id=num(p[1]);
            if(service.supplier(id).isEmpty()){HttpUtil.sendError(e,404,"NOT_FOUND","Record was not found.");return true;}
            if(p.length==3&&method.equals("GET")){HttpUtil.sendJson(e,200,service.supplierProducts(id));return true;}
            if(p.length==3&&method.equals("POST")){
                JsonNode n=mapper.readTree(HttpUtil.readBody(e));
                HttpUtil.sendJson(e,201,service.linkProduct(id,i(n,"productId")));
                return true;
            }
            if(p.length==4&&method.equals("DELETE")){
                if(service.unlinkProduct(id,num(p[3])))HttpUtil.sendEmpty(e,204);
                else HttpUtil.sendError(e,404,"NOT_FOUND","Link was not found.");
                return true;
            }
            HttpUtil.sendError(e,405,"METHOD_NOT_ALLOWED","Method is not supported.");
            return true;
        }
        return false;
    }
    private void get(HttpExchange e,Integer id)throws IOException,SQLException{Object result=switch(kind){case"supplier"->id==null?service.suppliers():service.supplier(id).orElse(null);case"warehouse"->id==null?service.warehouses():service.warehouse(id).orElse(null);case"customer"->id==null?service.customers():service.customer(id).orElse(null);default->id==null?service.employees():service.employee(id).orElse(null);};if(id!=null&&result==null)HttpUtil.sendError(e,404,"NOT_FOUND","Record was not found.");else HttpUtil.sendJson(e,200,result);}
    private void post(HttpExchange e,Integer id)throws IOException,SQLException{if(id!=null){HttpUtil.sendError(e,405,"METHOD_NOT_ALLOWED","POST is only supported on the collection.");return;}JsonNode n=mapper.readTree(HttpUtil.readBody(e));Object x=switch(kind){case"supplier"->service.create(new Supplier(0,t(n,"name"),t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode")));case"warehouse"->service.create(new Warehouse(0,t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode"),i(n,"capacity"),ni(n,"managerId")));case"customer"->service.create(new Customer(0,t(n,"firstName"),t(n,"lastName"),t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode")));default->service.create(new Employee(0,t(n,"firstName"),t(n,"lastName"),LocalDate.parse(t(n,"dob"))));};HttpUtil.sendJson(e,201,x);}
    private void put(HttpExchange e,Integer id)throws IOException,SQLException{if(id==null)throw new MasterDataService.ValidationException("ID is required.");JsonNode n=mapper.readTree(HttpUtil.readBody(e));Object x=switch(kind){case"supplier"->service.update(id,new Supplier(id,t(n,"name"),t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode"))).orElse(null);case"warehouse"->service.update(id,new Warehouse(id,t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode"),i(n,"capacity"),ni(n,"managerId"))).orElse(null);case"customer"->service.update(id,new Customer(id,t(n,"firstName"),t(n,"lastName"),t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode"))).orElse(null);default->service.update(id,new Employee(id,t(n,"firstName"),t(n,"lastName"),LocalDate.parse(t(n,"dob")))).orElse(null);};if(x==null)HttpUtil.sendError(e,404,"NOT_FOUND","Record was not found.");else HttpUtil.sendJson(e,200,x);}
    private void del(HttpExchange e,Integer id)throws IOException,SQLException{if(id==null)throw new MasterDataService.ValidationException("ID is required.");boolean ok=switch(kind){case"supplier"->service.deleteSupplier(id);case"warehouse"->service.deleteWarehouse(id);case"customer"->service.deleteCustomer(id);default->service.deleteEmployee(id);};if(ok)HttpUtil.sendEmpty(e,204);else HttpUtil.sendError(e,404,"NOT_FOUND","Record was not found.");}
    private Integer parseId(String p){if(p.equals(prefix)||p.equals(prefix+"/"))return null;try{return Integer.valueOf(p.substring(prefix.length()).replace("/",""));}catch(Exception x){throw new MasterDataService.ValidationException("ID must be a number.");}}
    private String t(JsonNode n,String f){JsonNode v=n==null?null:n.get(f);if(v==null||v.isNull())return null;return v.asText();} private int i(JsonNode n,String f){JsonNode v=n==null?null:n.get(f);if(v==null||!v.canConvertToInt())throw new MasterDataService.ValidationException(f+" must be an integer.");return v.asInt();} private Integer ni(JsonNode n,String f){JsonNode v=n==null?null:n.get(f);return v==null||v.isNull()?null:i(n,f);}
}
