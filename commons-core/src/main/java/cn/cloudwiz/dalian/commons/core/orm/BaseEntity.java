package cn.cloudwiz.dalian.commons.core.orm;

import cn.cloudwiz.dalian.commons.core.BaseData;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public abstract class BaseEntity extends BaseData {

	private static final long serialVersionUID = -6166056994846399546L;

	private Long primaryKey;

	public Long getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(Long primaryKey) {
		this.primaryKey = primaryKey;
	}

	@Override
	public void merge(Object... others) {
		super.merge(others);
		if(others != null){
			Object key = null;
			for(Object other : others){
				try {
					if(PropertyUtils.isReadable(other, "key")){
						Object otherKey = PropertyUtils.getProperty(other, "key");
						if(otherKey != null){
							key = otherKey;
						}
					}
				} catch (Exception e) {
					//找不到忽略
				}
			}
			if(key != null){
				this.setPrimaryKey((Long)ConvertUtils.convert(key, Long.TYPE));
			}
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(this == obj)  return true;
		if(!getClass().equals(obj.getClass())) return false;
		BaseEntity that = (BaseEntity)obj;
		return Objects.equals(that.primaryKey, this.primaryKey);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(getClass(), primaryKey);
	}

	
	public static Long[] getPrimaryKeys(BaseEntity[] entitys){
		Set<Long> ids = new HashSet<>();
		if(ArrayUtils.isNotEmpty(entitys)){
			for(BaseEntity item : entitys){
				ids.add(item.getPrimaryKey());
			}
		}
		return ids.toArray(new Long[ids.size()]);
	}
	
}
