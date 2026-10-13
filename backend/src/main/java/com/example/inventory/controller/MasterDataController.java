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
        try{String method=e.getRequestMethod().toUpperCase();Integer id=parseId(e.getRequestURI().getPath());
            if("OPTIONS".equals(method)){HttpUtil.sendEmpty(e,204);return;}
            switch(method){case "GET"->get(e,id);case "POST"->post(e,id);case "PUT"->put(e,id);case "DELETE"->del(e,id);default->HttpUtil.sendError(e,405,"METHOD_NOT_ALLOWED","Method is not supported.");}
        }catch(MasterDataService.ValidationException x){HttpUtil.sendError(e,400,"VALIDATION_ERROR",x.getMessage());}
        catch(SQLException x){if(x.getErrorCode()==1451)HttpUtil.sendError(e,409,"REFERENCED_RECORD","Record is still referenced.");else if(x.getErrorCode()==1452)HttpUtil.sendError(e,400,"INVALID_REFERENCE","Referenced record does not exist.");else HttpUtil.sendError(e,500,"DATABASE_ERROR","The database operation could not be completed.");}
        catch(Exception x){HttpUtil.sendError(e,400,"INVALID_REQUEST","Request JSON is invalid.");}
    }
    private void get(HttpExchange e,Integer id)throws IOException,SQLException{Object result=switch(kind){case"supplier"->id==null?service.suppliers():service.supplier(id).orElse(null);case"warehouse"->id==null?service.warehouses():service.warehouse(id).orElse(null);case"customer"->id==null?service.customers():service.customer(id).orElse(null);default->id==null?service.employees():service.employee(id).orElse(null);};if(id!=null&&result==null)HttpUtil.sendError(e,404,"NOT_FOUND","Record was not found.");else HttpUtil.sendJson(e,200,result);}
    private void post(HttpExchange e,Integer id)throws IOException,SQLException{if(id!=null){HttpUtil.sendError(e,405,"METHOD_NOT_ALLOWED","POST is only supported on the collection.");return;}JsonNode n=mapper.readTree(HttpUtil.readBody(e));Object x=switch(kind){case"supplier"->service.create(new Supplier(0,t(n,"name"),t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode")));case"warehouse"->service.create(new Warehouse(0,t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode"),i(n,"capacity"),ni(n,"managerId")));case"customer"->service.create(new Customer(0,t(n,"firstName"),t(n,"lastName"),t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode")));default->service.create(new Employee(0,t(n,"firstName"),t(n,"lastName"),LocalDate.parse(t(n,"dob"))));};HttpUtil.sendJson(e,201,x);}
    private void put(HttpExchange e,Integer id)throws IOException,SQLException{if(id==null)throw new MasterDataService.ValidationException("ID is required.");JsonNode n=mapper.readTree(HttpUtil.readBody(e));Object x=switch(kind){case"supplier"->service.update(id,new Supplier(id,t(n,"name"),t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode"))).orElse(null);case"warehouse"->service.update(id,new Warehouse(id,t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode"),i(n,"capacity"),ni(n,"managerId"))).orElse(null);case"customer"->service.update(id,new Customer(id,t(n,"firstName"),t(n,"lastName"),t(n,"street"),t(n,"city"),t(n,"state"),t(n,"pincode"))).orElse(null);default->service.update(id,new Employee(id,t(n,"firstName"),t(n,"lastName"),LocalDate.parse(t(n,"dob")))).orElse(null);};if(x==null)HttpUtil.sendError(e,404,"NOT_FOUND","Record was not found.");else HttpUtil.sendJson(e,200,x);}
    private void del(HttpExchange e,Integer id)throws IOException,SQLException{if(id==null)throw new MasterDataService.ValidationException("ID is required.");boolean ok=switch(kind){case"supplier"->service.deleteSupplier(id);case"warehouse"->service.deleteWarehouse(id);case"customer"->service.deleteCustomer(id);default->service.deleteEmployee(id);};if(ok)HttpUtil.sendEmpty(e,204);else HttpUtil.sendError(e,404,"NOT_FOUND","Record was not found.");}
    private Integer parseId(String p){if(p.equals(prefix)||p.equals(prefix+"/"))return null;try{return Integer.valueOf(p.substring(prefix.length()).replace("/",""));}catch(Exception x){throw new MasterDataService.ValidationException("ID must be a number.");}}
    private String t(JsonNode n,String f){JsonNode v=n==null?null:n.get(f);if(v==null||v.isNull())return null;return v.asText();} private int i(JsonNode n,String f){JsonNode v=n==null?null:n.get(f);if(v==null||!v.canConvertToInt())throw new MasterDataService.ValidationException(f+" must be an integer.");return v.asInt();} private Integer ni(JsonNode n,String f){JsonNode v=n==null?null:n.get(f);return v==null||v.isNull()?null:i(n,f);}
}
