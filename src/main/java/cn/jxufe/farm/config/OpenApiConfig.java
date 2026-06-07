package cn.jxufe.farm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI farmOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Farm Management System API")
                        .description("农场管理系统 RESTful API 文档\n\n"
                                + "## 模块\n"
                                + "- **用户模块** — 用户 CRUD、会话管理、头像上传\n"
                                + "- **文件模块** — 本地文件上传/删除/访问\n"
                                + "- **种子商店** — 种子类型管理、商店购买/出售、交易记录\n"
                                + "- **作物生命周期** — 种植、收获、养护(杀虫)、铲除\n"
                                + "- **地块经营** — 地块解锁、扩地、状态查询\n"
                                + "- **农场查询** — 农场概览、种植面板、可种地块\n"
                                + "- **地块配置** — 土壤类型管理、全局策略配置\n"
                                + "- **WebSocket** — 作物状态实时推送\n\n"
                                + "## 通用约定\n"
                                + "- 统一响应体: `R<T>` = `{code, msg, data}`\n"
                                + "- 分页响应: `PageResult<T>` = `{pageNo, pageSize, total, records}`\n"
                                + "- 幂等接口需传 `requestId`\n"
                                + "- 全局异常统一转 `R.failed(...)`")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Farm Dev Team")
                                .email("farm@jxufe.local"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8080")
                                .description("本地开发环境")
                ));
    }
}
