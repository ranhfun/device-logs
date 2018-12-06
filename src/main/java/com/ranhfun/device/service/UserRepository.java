package com.ranhfun.device.service;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import com.ranhfun.device.domain.User;

@Repository
public interface UserRepository extends CrudRepository<User, Integer> {

	User findByEmail(String email);
	
}
