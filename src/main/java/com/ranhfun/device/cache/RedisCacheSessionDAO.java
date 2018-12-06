package com.ranhfun.device.cache;

import java.io.Serializable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.SimpleSession;
import org.apache.shiro.session.mgt.eis.CachingSessionDAO;
import org.springframework.data.redis.core.PartialUpdate;
import org.springframework.data.redis.core.RedisKeyValueTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;

public class RedisCacheSessionDAO extends CachingSessionDAO  {

	private static final Log log = LogFactory.getLog(RedisCacheSessionDAO.class);
	
	private RedisTemplate<String, byte[]> template;
	
	private final JdkSerializationRedisSerializer serializer = new JdkSerializationRedisSerializer();;
	
	public RedisCacheSessionDAO(RedisTemplate<String, byte[]> template) {
		this.template = template;
	}

	@Override
	protected void doUpdate(Session session) {
//		PartialUpdate<SimpleSession> update = new PartialUpdate<SimpleSession>(session.getId(), SimpleSession.class)
//				.set("attributes", ((SimpleSession)session).getAttributes());
		//template.delete(session);
		log.debug("update shiro session " + session.getId() + " " + session);
//		template.update(session.getId(), update);
//		template.update(session.getId(), serializer((SimpleSession)session));
		template.boundHashOps("shiro").put(session.getId(), serializer((SimpleSession)session));
	}

	@Override
	protected void doDelete(Session session) {
		log.debug("delete shiro session " + session.getId() + " " + session);
//		template.delete(session.getId(), byte[].class);
		template.boundHashOps("shiro").delete(session.getId());
	}

	@Override
	protected Serializable doCreate(Session session) {
		Serializable sessionId = generateSessionId(session);
		assignSessionId(session, sessionId);
		log.debug("create shiro session " + session.getId() + " " + session);
//		template.insert(sessionId, serializer((SimpleSession)session));
		template.boundHashOps("shiro").put(session.getId(), serializer((SimpleSession)session));
		return sessionId;
	}

	@Override
	protected Session doReadSession(Serializable sessionId) {
		log.debug("read shiro session " + sessionId);
//		return deserialize(template.findById(sessionId, byte[].class).orElse(null));
		return deserialize((byte[])template.boundHashOps("shiro").get(sessionId));
	}

	public SimpleSession deserialize(byte[] value) {
		if (value==null) {
			return null;
		}
		return (SimpleSession) serializer.deserialize(value);
	}
	
	public byte[] serializer(SimpleSession value) {
		if (value==null) {
			return null;
		}
		return serializer.serialize(value);
	}
}
