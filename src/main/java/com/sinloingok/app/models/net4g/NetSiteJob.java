package com.sinloingok.app.models.net4g;


import lombok.Data;

/**
 * 	网络控制器-命令发送任务
 * @author kaptenkabu
 * 所属权归 Sinloingok
 *
 */
@Data
public class NetSiteJob {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5968825670095069087L;
	private long id;

	private long netSiteId ;

	private String msg;

	private String sendTime;

}
