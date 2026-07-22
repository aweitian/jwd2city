# JWD2City - 经纬度查城市服务

基于 Spring Boot 2.7.6 的经纬度查询城市服务，启动时将 JSON 文件中的城市区域数据加载到内存，使用 Java 实现多边形包含判断算法。

**当前分支**: `jar文件直接运行无任何依赖` - 无需数据库，数据从 JSON 文件加载

## 致谢

感谢 [wuwenrufeng/amap](https://github.com/wuwenrufeng/amap) 提供的城市区域数据。


## 构建与运行

```bash
# 编译打包
下载 https://github.com/aweitian/jwd2city/releases/download/v1.1/jwd2city-1.1.0.jar

# 运行（无需数据库，直接启动）
java -jar -Dserver.port=12508 jwd2city-1.1.0.jar
```

## 测试
```bash
# 查询北京城区（天安门附近）
curl "http://localhost:12508/api/city?lon=116.405285&lat=39.904989"
{"province":"北京市","city":"北京市","adcode":110100,"status":"success"}

# 查询上海城区
curl "http://localhost:12508/api/city?lon=121.473701&lat=31.230416"
{"province":"上海市","city":"上海市","adcode":310100,"status":"success"}

# 查询安徽省/安庆市
curl "http://localhost:12508/api/city?lon=116.156261&lat=30.782063“
{"province":"安徽省","city":"安庆市","adcode":340800,"status":"success"}

# 获取缓存数量
curl "http://localhost:12508/api/stats"
```




## 技术栈

- Java 8
- Spring Boot 2.7.6
- Jackson（JSON解析）
- Lombok

## 功能特性

- 启动时自动将城市区域数据（约2347条）从 JSON 文件加载到内存缓存
- 使用射线法（Ray Casting Algorithm）判断点是否在多边形内
- 使用 MBR（外包矩形）进行快速过滤优化查询性能
- 纯 Java 计算，不依赖任何数据库地理函数
- **无需数据库**，开箱即用

## 数据准备

城市区域数据存储在 `src/main/resources/cityData/` 目录下，每个城市一个 JSON 文件。

数据格式示例：
```json
[
  {
    "id": 1,
    "name": "北京城区",
    "adcode": 110000,
    "polyline": "116.812384,39.615914;116.812081,39.615689;...",
    "min": "115.423411,39.442758",
    "max": "117.514625,41.060816"
  }
]
```

`

## API 接口

### 根据经纬度查询城市

**请求**
```
curl "http://127.0.0.1:12508/api/city?lon=116.14&lat=39.74"
```

**响应示例**
```json
{"city":"北京城区","adcode":110100,"status":"success"}
```

**参数说明**
- `lon`: 经度（double）
- `lat`: 纬度（double）

**响应字段**
- `city`: 城市名称
- `adcode`: 行政区划代码
- `status`: 状态（success/not_found）

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
│   │   └── CityService.java        # 业务逻辑服务（从JSON加载数据）
│   ├── entity/
│   │   └── CityRegion.java         # 城市区域实体类
│   └── util/
│       ├── Point.java              # 坐标点
│       ├── PolygonUtil.java        # 多边形算法工具
│       └── DataExportUtil.java     # 数据导出工具（从数据库导出到JSON）
├── src/main/resources/
│   ├── application.yml             # 配置文件
│   └── cityData/                   # 城市区域JSON数据目录
├── pom.xml                         # Maven 配置
├── .gitignore                      # Git 忽略文件
└── README.md                       # 项目说明
```

## 使用示例

### 获取经纬度

可以在 [高德地图拾取器](https://lbs.amap.com/tools/picker) 获取经纬度来验证接口，本服务使用的是高德坐标系（GCJ-02）。

### 接口调用


## 数据导出工具

如果需要从数据库导出数据到 JSON 文件，可以使用 `DataExportUtil`。需要先在该文件中配置数据库连接信息，然后运行：

```bash
# 添加 MySQL 依赖后运行导出
运行
com.example.jwd2city.util.DataExportUtil 的main
```

## License

MIT License
