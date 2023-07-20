package com.dlut.community;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.*;
import org.springframework.boot.context.TypeExcludeFilter;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Indexed;

@SpringBootApplication

public class CommunityApplication {

//	@PostConstruct
//	public void init() {
//		// 解决netty启动冲突问题
//		// see Netty4Utils.setAvailableProcessors()
//		System.setProperty("es.set.netty.runtime.available.processors", "false");
//	}

	public static void main(String[] args) {
		SpringApplication.run(CommunityApplication.class, args);
//		SpringApplication.run(CommunityApplication.class, "--debug");
	}

}
