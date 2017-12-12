package cn.org.hentai.server.app;

import cn.org.hentai.server.controller.TestController;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Created by matrixy on 2017-12-12.
 */
@ComponentScan(value = {"cn.org.hentai"})
@EnableAutoConfiguration
public class ServerApplication
{
    public static void main(String[] args) throws Exception
    {
        SpringApplication.run(ServerApplication.class, args);
    }
}
