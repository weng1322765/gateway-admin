package jrx.anydmp.gateway.admin.dto;

import java.io.Serializable;

/**
 * @author zhao tingting
 * @date 2019/1/3
 */
public class CommonProperty  implements Serializable {

	private static final long serialVersionUID = 5433301751667403637L;
	private Integer id;
	private String serverId;

	/**
	 * 类型为路由或限流或降级
	 *
	 */
	private String type;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
