# JWD2City - 经纬度查城市服务

基于 Spring Boot 2.7.6 的经纬度查询城市服务，启动时将数据库中的城市区域数据加载到内存，使用 Java 实现多边形包含判断算法。

## 致谢

感谢 [wuwenrufeng/amap](https://github.com/wuwenrufeng/amap) 提供的城市区域数据。

## 技术栈

- Java 8
- Spring Boot 2.7.6
- Spring Data JPA
- MySQL 

## 功能特性

- 启动时自动将城市区域数据（约2347条）加载到内存缓存
- 使用射线法（Ray Casting Algorithm）判断点是否在多边形内
- 使用 MBR（外包矩形）进行快速过滤优化查询性能
- 不依赖 MySQL 地理函数，纯 Java 计算

## 数据库表结构

```sql
CREATE TABLE `city_region` (
  `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL,
  `citycode` int(6) DEFAULT NULL,
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `polyline` longtext CHARACTER SET utf8 COLLATE utf8_general_ci,
  `center` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `adcode` int(6) DEFAULT NULL,
  `max` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `min` varchar(100) CHARACTER SET utf8 COLLATE utf8_general_ci DEFAULT NULL,
  `geom` polygon /*!80003 SRID 4326 */ DEFAULT NULL COMMENT '行政区域边界（WGS84坐标系）',
  `mbr` polygon /*!80003 SRID 4326 */ DEFAULT NULL COMMENT '外包矩形（用于空间索引快速过滤）',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE=InnoDB DEFAULT CHARSET=utf8 ROW_FORMAT=DYNAMIC;
```

## 配置说明

在 `src/main/resources/application.yml` 中配置数据库连接：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/example_db?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: admin
    password: password
```

## 构建与运行

```bash
# 编译打包
mvn clean package

# 运行
java -jar target/jwd2city-1.0.0.jar
```

## API 接口

### 根据经纬度查询城市

**请求**

```
GET /api/city?lon=<经度>&lat=<纬度>
```

**响应示例**

```json
{
    "city": "北京城区",
    "adcode": 110000,
    "status": "success"
}
```

**参数说明**

- `lon`: 经度（double）
- `lat`: 纬度（double）

**响应字段**

- `city`: 城市名称
- `adcode`: 行政区划代码
- `status`: 状态（success/not\_found）

### 获取统计信息

**请求**

```
GET /api/stats
```

**响应示例**

```json
{
    "cachedCount": 2347
}
```

## 项目结构

```
jwd2city/
├── src/main/java/com/example/jwd2city/
│   ├── Jwd2cityApplication.java    # 启动类
│   ├── controller/
│   │   └── CityController.java     # REST API 控制器
│   ├── service/
│   │   └── CityService.java        # 业务逻辑服务
│   ├── repository/
│   │   └── CityRegionRepository.java  # 数据访问层
│   ├── entity/
│   │   └── CityRegion.java         # 实体类
│   └── util/
│       ├── Point.java              # 坐标点
│       └── PolygonUtil.java        # 多边形算法工具
├── src/main/resources/
│   └── application.yml             # 配置文件
├── pom.xml                         # Maven 配置
└── .gitignore                      # Git 忽略文件
```

## 使用示例

```bash
# 查询北京城区（天安门附近）
curl "http://localhost:8080/api/city?lon=116.405285&lat=39.904989"

# 查询上海城区
curl "http://localhost:8080/api/city?lon=121.473701&lat=31.230416"

# 获取缓存数量
curl "http://localhost:8080/api/stats"
```

## License

MIT License
