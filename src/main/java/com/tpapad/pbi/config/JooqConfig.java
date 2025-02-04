package com.tpapad.pbi.config;

import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.conf.Settings;
import org.jooq.impl.DSL;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class JooqConfig {
    @Bean
    public DSLContext dsl(DataSource dataSource) {
        return DSL.using(dataSource, SQLDialect.POSTGRES, new Settings().withRenderFormatted(true));
    }
}
