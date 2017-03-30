package com.huangmei.commonhm.model;

public class Vote implements Entity {
	
	private static final long serialVersionUID = 1L;
	
	/**  */
	protected Integer id;
	
	/** 发起人id */
	protected Integer organizerUserId;
	
	/**  */
	protected Integer roomId;
	
	/**  */
	protected Integer state;
	
	/**  */
	protected Integer type;
	
	/**  投票人id*/
	protected Integer voterUserId;
	
 	public Integer getId() {
		return id;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public Integer getOrganizerUserId() {
		return organizerUserId;
	}
	
	public void setOrganizerUserId(Integer organizerUserId) {
		this.organizerUserId = organizerUserId;
	}
	
	public Integer getRoomId() {
		return roomId;
	}
	
	public void setRoomId(Integer roomId) {
		this.roomId = roomId;
	}
	
	public Integer getState() {
		return state;
	}
	
	public void setState(Integer state) {
		this.state = state;
	}
	
	public Integer getType() {
		return type;
	}
	
	public void setType(Integer type) {
		this.type = type;
	}
	
	public Integer getVoterUserId() {
		return voterUserId;
	}
	
	public void setVoterUserId(Integer voterUserId) {
		this.voterUserId = voterUserId;
	}
	
 	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("id = ").append(id).append(", ");
		builder.append("organizerUserId = ").append(organizerUserId).append(", ");
		builder.append("roomId = ").append(roomId).append(", ");
		builder.append("state = ").append(state).append(", ");
		builder.append("type = ").append(type).append(", ");
		builder.append("voterUserId = ").append(voterUserId);
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		Vote other = (Vote) obj;
		if (id == null) {
			if (other.id != null) {
				return false;
			}
		} else if (!id.equals(other.id)) {
			return false;
		}
		return true;
	}

}