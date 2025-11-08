package com.sinloingok.app.service.pay;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import com.sinloingok.app.models.net4g.NetSite;
import com.sinloingok.app.config.SBeanUtils;
import com.sinloingok.app.service.netsite.NetSiteService;
import com.sinloingok.app.util.pay.SignUtil;
import org.springframework.stereotype.Service;

/**
 * Payment service providing minimal Java 8 implementations of the required
 * Shouqianba API calls.
 */
@Service
public class PaymentService {

    private String vendorSn = "91802590";
    private String vendorKey = "6dd250c034d7ae904e0780c6b9e8279b";
    private String appId = "2025053000009159";
    private String apiUrl = "https://vsi-api.shouqianba.com";
    
    public PaymentService() {}

    /**
     * Activate all terminals using the provided activation code.
     *
     * @param code activation code
     * @return {@code true} if activation requests were sent
     */
    public boolean activate(String code) {
        NetSiteService netSiteService = SBeanUtils.getBean(NetSiteService.class);
        List<NetSite> sites = netSiteService.listActive();
    	
    	for(NetSite ns : sites) {
    		ns.mapFromRecord(ns);
            String url = apiUrl + "/terminal/activate";
            JSONObject request = new JSONObject();
            request.put("vendor_sn", vendorSn);
            request.put("vendor_key", vendorKey);
            request.put("app_id", appId);
            request.put("device_id", "sln"+ ns.getOnlyCode());
            request.put("code", code);
            
            String sign = SignUtil.sign(request.toJSONString(), vendorKey);
            String authorization = vendorSn + " " + sign;

            String response = sendPostRequest(url, request.toJSONString(), authorization);
            JSONObject res = JSONObject.parseObject(response);
            if(res != null){
            	String terminal_sn = res.getString("terminal_sn");
                String terminal_key = res.getString("terminal_key"); 
                if(terminal_key != null && terminal_sn != null) {
                        ns.setTerminalSn(terminal_sn);
                    ns.setTerminalKey(terminal_key);
                    netSiteService.updateTerminal(ns.getId(), terminal_sn, terminal_key);
                }
            }
    	}
        return true;
    }

    /**
     * Re-checkin all active terminals.
     */
    public void checkin() {
    	List<NetSite> list = getNetSites();
        String url = apiUrl + "/terminal/checkin";
    	for(NetSite ns : list) {
            JSONObject request = new JSONObject();
            request.put("terminal_sn", ns.getTerminalSn());
            request.put("device_id", ns.getOnlyCode());
            JSONObject res = sendPost(request, ns.getTerminalKey(), ns.getTerminalSn(), url);
        	String terminal_sn = res.getString("terminal_sn");
            String terminal_key = res.getString("terminal_key");  
            if(terminal_key != null && terminal_sn != null) {
                ns.setTerminalSn(terminal_sn);
                ns.setTerminalKey(terminal_key);
                NetSiteService netSiteService = SBeanUtils.getBean(NetSiteService.class);
                netSiteService.updateTerminal(ns.getId(), terminal_sn, terminal_key);
            }
    	}
    }

    /**
     * Precreate an order for payment.
     */
    public JSONObject precreate(JSONObject requestBody) {
        String url = apiUrl + "/upay/v2/precreate";

    	NetSite ns = getNSite();
    	ns.mapFromRecord(ns);
    	String terminalSn = ns.getTerminalSn();
    	String terminalKey = ns.getTerminalKey(); 
        requestBody.put("terminal_sn", terminalSn);
        String sign = SignUtil.sign(requestBody.toJSONString(), terminalKey);
        String authorization = terminalSn + " " + sign;
        String response = sendPostRequest(url, requestBody.toJSONString(), authorization);
        return JSONObject.parseObject(response);
    }
    
    /**
     * 查询
     * @param  terminal_sn:终端号
     * @param  terminal_key:终端密钥
     * @return
     */
    /**
     * Query order status.
     */
    public JSONObject query(String sn, String clientSn){
        String url = apiUrl + "/upay/v2/query";
        JSONObject params = new JSONObject();
        try{
        	NetSite ns = getNSite();
            params.put("terminal_sn", ns.getTerminalSn());           //终端号
            params.put("sn",sn);             //收钱吧系统内部唯一订单号
            params.put("client_sn", clientSn);  //商户系统订单号,必须在商户系统内唯一；且长度不超过64字节
            String sign = SignUtil.sign(params.toJSONString(), ns.getTerminalKey());
            String authorization = ns.getTerminalSn() + " " + sign;
            String response = sendPostRequest(url, params.toJSONString(), authorization);
            return JSONObject.parseObject(response);
        }catch (Exception e){
            return null;
        }
    }
    /**
     * 退款
     * @param sn
     * @param clientSn
     * @param amount
     * @param rerfundNo
     * @return
     */
    /**
     * Request a refund.
     */
    public JSONObject refund(JSONObject params){
        String url = apiUrl + "/upay/v2/refund";
        try{
        	NetSite ns = getNSite();
            params.put("terminal_sn", ns.getTerminalSn()); 
            String sign = SignUtil.sign(params.toJSONString(), ns.getTerminalKey());
            String authorization = ns.getTerminalSn() + " " + sign;
            String response = sendPostRequest(url, params.toJSONString(), authorization);
            return  JSONObject.parseObject(response);
        }catch (Exception e){
            return null;
        }
    }

    /**
     * 发送 POST 请求
     */
    /**
     * Send an HTTP POST request with JSON body.
     */
    private String sendPostRequest(String urlStr, String jsonBody, String authorization) {
        StringBuilder response = new StringBuilder();
        try {
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Authorization", authorization);
            conn.setDoOutput(true);

            // 写入请求体
            try (OutputStream os = conn.getOutputStream()) {
                os.write(jsonBody.getBytes("UTF-8"));
                os.flush();
            }

            // 读取响应
            int responseCode = conn.getResponseCode();
            InputStream is = (responseCode >= 200 && responseCode < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();

            try (BufferedReader in = new BufferedReader(new InputStreamReader(is, "UTF-8"))) {
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
            }

            conn.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("请求失败: " + e.getMessage());
        }

        return response.toString();
    }
    
    /**
     * Convenience method to send a request with terminal credentials.
     */
    private JSONObject sendPost(JSONObject request, String terminalKey, String terminalSn, String url) {
	   String sign = SignUtil.sign(request.toJSONString(), terminalKey);
       String authorization = terminalSn + " " + sign;
       String response = sendPostRequest(url, request.toJSONString(), authorization);
       JSONObject retObj = JSONObject.parseObject(response);
       String resCode = retObj.get("result_code").toString();
       if(!resCode.equals("200"))
           return null;
       String responseStr = retObj.get("biz_response").toString();
       JSONObject terminal = JSONObject.parseObject(responseStr);
       if(terminal.get("terminal_sn")==null || terminal.get("terminal_key")==null)
           return null;
       return  terminal;
    }
    
    /**
     * Retrieve one active NetSite for operations.
     */
    private NetSite getNSite() {
        List<NetSite> list = getNetSites();
    	NetSite ns = list.get(1);
    	ns.mapFromRecord(ns);
    	return ns;
    } 
    
    /**
     * Get all active NetSites from the database.
     */
    private List<NetSite> getNetSites(){
        NetSiteService netSiteService = SBeanUtils.getBean(NetSiteService.class);
        return netSiteService.listActive();
    }
}
