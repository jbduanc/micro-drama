package com.tcm.rx.config;


import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.generator.AutoGenerator;
import com.baomidou.mybatisplus.generator.InjectionConfig;
import com.baomidou.mybatisplus.generator.config.*;
import com.baomidou.mybatisplus.generator.config.converts.PostgreSqlTypeConvert;
import com.baomidou.mybatisplus.generator.config.rules.DbColumnType;
import com.baomidou.mybatisplus.generator.config.rules.IColumnType;
import com.baomidou.mybatisplus.generator.config.rules.NamingStrategy;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * @author shouhan
 * @version 1.0
 * @date 2021/3/16
 * @desc mybatis-plus代码生成器（PostgreSQL，与 micro-drama-admin 对齐）
 * @see
 * @since 1.0
 */
public class MybatisPlusGenerator {


    private static final String serviceName = "";    //项目路径
    private static final String parentPackage = "com.tcm.rx"; //项目包

    //**************************运行时需要修改部分（begin）*******************************
    private static final String packageName = "msg";   //包名
    private static final String authorName = "djbo";  //作者s
    /** 表名（PostgreSQL 表名默认小写，需与库中一致） */
    private static final String table = "rx_msg";
    private static final String prefix = "rx_";             //表的前缀
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
        // 数据源配置（与 com.series.admin.config.MybatisPlusGenerator 使用同一套 PG 连接参数）
        return new DataSourceConfig() {{
            setDbType(DbType.POSTGRE_SQL);
            setTypeConvert(new PostgreSqlTypeConvert() {
                @Override
                public IColumnType processTypeConvert(GlobalConfig globalConfig, String fieldType) {
                    System.out.println("转换类型：" + fieldType);
                    String t = fieldType.toLowerCase();
                    if (t.contains("datetime") || t.contains("timestamp")) {
                        return DbColumnType.DATE;
                    }
                    return super.processTypeConvert(globalConfig, fieldType);
                }
            });
            setDriverName("org.postgresql.Driver");
            setUsername("postgres");
            setPassword("djbo1616");
            setUrl("jdbc:postgresql://144.202.122.212:5432/postgres?ssl=false&TimeZone=Asia/Shanghai&stringtype=unspecified&reWriteBatchedInserts=true&prepareThreshold=0&loginTimeout=10&socketTimeout=30");

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
            //setXml("mapper." + packageName);
        }};
    }

    private static StrategyConfig getStrategy() {
        // 策略配置（PostgreSQL 表名/字段名约定与 admin 生成器一致）
        return new StrategyConfig() {{
            setTablePrefix(prefix);
            setNaming(NamingStrategy.underline_to_camel);
            setInclude(table);
            setRestControllerStyle(true);
            setColumnNaming(NamingStrategy.underline_to_camel);
            setCapitalMode(false);
        }};
    }

    private static GlobalConfig getGlobalConfig(String path) {
        //全局配置
        return new GlobalConfig() {{
            //输出目录
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
