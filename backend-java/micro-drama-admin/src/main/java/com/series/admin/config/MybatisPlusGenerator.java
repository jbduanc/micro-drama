package com.series.admin.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert; // 替换为PostgreSQL类型转换器
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author djbo
 * @version 1.0
 * @date 2021/3/16
 * @desc mybatis-plus代码生成器
 * @see
 * @since 1.0
 */
public class MybatisPlusGenerator {

    private static final String serviceName = "";    // 项目根路径（建议手动指定绝对路径，比如G:/workspace/code/micro-drama/micro-drama-admin）
    private static final String parentPackage = "com.series.admin"; //项目包

    //**************************运行时需要修改部分（begin）*******************************
    private static final String packageName = "business";   //包名
    private static final String authorName = "djbo";  //作者
    private static final String table = "t_drama_episodes";          //表名（PostgreSQL表名默认小写，需和数据库一致）
    private static final String prefix = "t_";             //表的前缀
    //**************************运行时需要修改部分（end）*********************************
    private static final File file = new File(serviceName);
    private static final String path = file.getAbsolutePath();

    /**
     * 入口run
     *
     * @param args
     **/
    public static void main(String[] args) {
        // 代码生成器
        AutoGenerator serviceAutoGenerator = new AutoGenerator();
        serviceAutoGenerator.setGlobalConfig(getGlobalConfig(path));
        serviceAutoGenerator.setDataSource(getDataSourceConfig());
        serviceAutoGenerator.setStrategy(getStrategy());
        serviceAutoGenerator.setPackageInfo(getPackageInfo());
        serviceAutoGenerator.setTemplate(getTemplateConfig());
        serviceAutoGenerator.setCfg(getInjectionConfig());
        // 执行生成
        serviceAutoGenerator.execute();
    }

    private static DataSourceConfig getDataSourceConfig() {
        // 数据源配置 - 修正为PostgreSQL适配
        return new DataSourceConfig() {{
            // 1. 修正数据库类型为PostgreSQL
            setDbType(DbType.POSTGRE_SQL);
            // 2. 替换为PostgreSQL类型转换器
            setTypeConvert(new PostgreSqlTypeConvert() {
                @Override
                public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                    System.out.println("转换类型：" + fieldType);
                    String t = fieldType.toLowerCase();
                    // 适配PostgreSQL的datetime/timestamp类型转换为DATE
                    if (t.contains("datetime") || t.contains("timestamp")) {
                        return DbColumnType.DATE;
                    }
                    return super.processTypeConvert(globalConfig, fieldType);
                }
            });
            // PostgreSQL驱动、账号密码、URL保持不变（确认URL正确性）
            setDriverName("org.postgresql.Driver");
            setUsername("postgres");
            setPassword("djbo1616");
            setUrl("jdbc:postgresql://47.84.207.243:5432/postgres?ssl=false&TimeZone=Asia/Shanghai&stringtype=unspecified&reWriteBatchedInserts=true&prepareThreshold=0&loginTimeout=10&socketTimeout=30");
        }};
    }

    private static InjectionConfig getInjectionConfig() {
        // 注入自定义配置，可以在 VM 中使用 cfg.abc 设置的值
        return new InjectionConfig() {
            @Override
            public void initMap() {
                Map<String, Object> map = new HashMap<>();
                map.put("name", this.getConfig().getGlobalConfig().getAuthor());
                this.setMap(map);
            }
        };
    }

    private static TemplateConfig getTemplateConfig() {
        // 关闭默认 xml 生成，调整生成 至 根目录
        return new TemplateConfig() {{
            setXml(null);
        }};
    }

    private static PackageConfig getPackageInfo() {
        // 包配置
        return new PackageConfig() {{
            // 自定义包路径
            setParent(parentPackage);
            // 这里是控制器包名，默认 web
            setController("controller." + packageName);
            setEntity("entity." + packageName);
            setMapper("mapper." + packageName);
            setService("service." + packageName);
            setServiceImpl("service." + packageName + ".impl");
        }};
    }

    private static StrategyConfig getStrategy() {
        // 策略配置 - 增强PostgreSQL表名适配
        return new StrategyConfig() {{
            setTablePrefix(prefix);
            // 表名生成策略
            setNaming(NamingStrategy.underline_to_camel);
            // 需要生成的表（PostgreSQL表名默认小写，确保和数据库一致）
            setInclude(table);
            setRestControllerStyle(true);
            // 可选：开启字段名下划线转驼峰
            setColumnNaming(NamingStrategy.underline_to_camel);
            // 可选：忽略表名/字段名的大小写（适配PostgreSQL）
            setCapitalMode(false);
        }};
    }

    private static GlobalConfig getGlobalConfig(String path) {
        //全局配置
        return new GlobalConfig() {{
            // 确认输出目录正确性（建议手动指定绝对路径，避免空值问题）
            setOutputDir(path + "/src/main/java");
            // 是否覆盖文件
            setFileOverride(false);
            // 开启 activeRecord 模式
            setActiveRecord(true);
            // XML 二级缓存
            setEnableCache(false);
            // XML ResultMap
            setBaseResultMap(true);
            // XML columList
            setBaseColumnList(true);
            //生成后打开文件夹
            setOpen(false);
            setAuthor(authorName);
            // 自定义文件命名，注意 %s 会自动填充表实体属性！
            setMapperName("%sMapper");
            setXmlName("%sMapper");
            setServiceName("I%sService");
            setServiceImplName("%sServiceImpl");
            setControllerName("%sController");
        }};
    }

    public static boolean deleteFile(File dirFile) {
        // 如果dir对应的文件不存在，则退出
        if (!dirFile.exists()) {
            return false;
        }

        if (dirFile.isFile()) {
            return dirFile.delete();
        } else {
            for (File file : dirFile.listFiles()) {
                deleteFile(file);
            }
        }
        return dirFile.delete();
    }
}