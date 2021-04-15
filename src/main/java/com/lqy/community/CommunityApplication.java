package com.lqy.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@SpringBootApplication
public class CommunityApplication {

	//构造器调用完以后被执行
	@PostConstruct
	public void init(){
		//解决redis和elasticsearch的底层netty冲突
		//Netty4Utils.setAvailableProcessors()
		System.getProperty("es.set.netty.runtime.available.processors", "false");
	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
	}

}
