package com.ranhfun.device.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ranhfun.device.domain.LogInfo;

@Repository
public interface LogInfoRepository extends CrudRepository<LogInfo, Long> {
	
}
