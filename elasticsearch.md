### ElasticSearch

---



#### 一: 安装和启动

#####  1. ElasticSearch下载

> 官方地址: https://www.elastic.co/cn/downloads/elasticsearch
>
> github: https://github.com/elastic/elasticsearch

下载或clone后解压



##### 2. 单实例节点启动

```
# cd elasticsearch目录下
bin/elasticsearch
bin/elasticsearch -d # 后台启动
```

默认端口9200, 启动完成后访问`http://ip:9200` 即可查看到节点信息

启动中我遇到两个错误: 



**错误一:** 

```
can not run elasticsearch as root  
-- 不能以root用户启动
```

```
[root@01 bin]# groupadd xiefy
[root@01 bin]# useradd xiefy -g xiefy -p 123123
[root@01 bin]# chown -R xiefy:xiefy elasticsearch
```



**错误二:**

```shell
ERROR: [2] bootstrap checks failed
[1]: max file descriptors [65535] for elasticsearch process is too low, increase to at least [65536]
[2]: max virtual memory areas vm.max_map_count [65530] is too low, increase to at least [262144]

-- 错误[1]: max file descriptors过小
-- 错误[2]: max_map_count过小, max_map_count文件包含限制一个进程可以拥有的VMA(虚拟内存区域)的数量，系			  统默认是65530，修改成262144
```

```
# 切换到root用户
vi /etc/security/limits.conf

# 添加如下
* soft nofile 65536
* hard nofile 65536
```

```
# 切换到root用户
vi /etc/sysctl.conf

# 添加如下
vm.max_map_count=655360
# 重新加载配置文件或重启
sysctl -p # 从配置文件“/etc/sysctl.conf”加载内核参数设置
```



##### 3. elasticsearch-head插件安装

>   elasticsearch-head 是一个用于浏览Elastic Search集群并与之进行交互(操作和管理)的web界面
>
>   GitHub: https://github.com/mobz/elasticsearch-head
>
>   要使用elasticsearch-head, 需要具备nodejs环境: 

**nodejs安装**

>  下载源码: https://nodejs.org/en/download/

安装方式有多种, 我用源码安装方式(包含npm)

1. 下载源码:

   ```
   https://nodejs.org/dist/v8.9.4/node-v8.9.4.tar.gz
   ```

2. 解压源码: 

   ```
   tar xzvf node-v* && cd node-v*
   ```

3. 安装必要的编译软件

   ```
   sudo yum install gcc gcc-c++
   ```

4. 编译

   ```
   ./configure
   make
   ```

5. 编译&安装

   ```
   sudo make install
   ```

6. 查看版本

   ```
   node --version
   npm -version
   ```

   ​

下载或克隆`elasticsearch-head`后, 进入`elasticsearch-head-master`目录:

- `npm install` 

  速度太慢可以使用淘宝镜像: `npm install -g cnpm --registry=https://registry.npm.taobao.org`

- `npm run start`

- open <http://localhost:9100/>

这时可以访问到页面, 并没有监听到集群.

解决head插件和elasticsearch之间访问跨域问题. 

修改elasticsearch目录下的`elasticsearch.yml`

```
# 加入以下内容
http.cors.enabled: true
http.cors.allow-origin: "*"
```

然后: http://localhost:9100/ 即可访问到管理页面. 



##### 4. 分布式安装启动

elasticsearch的横向扩展很容易: 这里建立一个主节点(node-master), 两个随从节点(node-1, node-2)

我提前拷贝了三个es:

```shell
[xiefy@01 elk]$ ll
total 33620
-rw-r--r-- 1 root  root  33485703 Aug 17 22:42 elasticsearch-5.5.2.tar.gz
drwxr-xr-x 7 root  root      4096 Jan  8 11:17 elasticsearch-head-master
drwxr-xr-x 9 xiefy xiefy     4096 Jan  8 10:15 elasticsearch-master
drwxr-xr-x 9 xiefy xiefy     4096 Jan  8 14:13 elasticsearch-node1
drwxr-xr-x 9 xiefy xiefy     4096 Jan  8 14:16 elasticsearch-node2
-rw-r--r-- 1 root  root    921421 Jan  8 11:14 master.zip
```

分别配置三个es目录中的`config/elasticsearch.yml`

**node-master**:

```
cluster.name: xiefy_elastic 
node.name: node-master 
node.master: true 
network.host: 0.0.0.0 

# 除此之外, head插件需要连接到port: 9200的节点上, 还需要这个配置
http.cors.enabled: true
http.cors.allow-origin: "*"
```

**node-1**:

```
cluster.name: xiefy_elastic 
node.name: node-1
network.host: 0.0.0.0
http.port: 9201 
discovery.zen.ping.unicast.hosts: ["127.0.0.1"]
```

**node-2**: 参考node-1

**相关配置解释**: 

* cluster.name: 集群名称, 默认是elasticsearch

* node.name: 节点名称, 默认随机分配

* node.master: 是否是主节点, 默认情况下不写也可以, 第一个起来的就是Master

* network.host: 默认情况下只允许本机通过localhost或127.0.0.1访问, 为了测试方便, 

  我需要远程访问所以配成了`0.0.0.0`

* http.port: 默认为9200, 同一个服务器下启动多个es节点, 默认端口号会从9200默认递增1, 这里我手动指定了

* discovery.zen.ping.unicast.hosts: ["host1", "host2"]

  Elasticsearch默认使用服务发现(Zen discovery)作为集群节点间发现和通信的机制, 当启动新节点时，通过这个ip列表进行节点发现，组建集群.



分别启动三个es实例和head插件:

访问`http://ip:9100`:

![](http://thank-bucket-01.oss-cn-beijing.aliyuncs.com/md_pic/es%E4%B8%89%E4%B8%AA%E8%8A%82%E7%82%B9%E9%9B%86%E7%BE%A4.png)

#### 二: 基础概念

....



#### 三: 基础用法

##### 索引创建

* 方式一: head插件可以直接新建/删除/查询索引(Index)

* 方式二: 通过rest api 

  ```shell
  # 新建
  curl -X PUT 'localhost:9200/book'

  # 删除
  curl -X DELETE 'localhost:9200/book'

  # 查看当前节点下所有Index
  curl -X GET 'http://localhost:9200/_cat/indices?v'
  ```

这样创建的Index是没有结构的. 可以看到索引信息的mappings:{}

下面来定义一个有结构映射的Index

> PUT http://ip:9200/people

```json
{
	"settings": {
		"number_of_shards": 3,
		"number_of_replicas": 1
	},
	"mappings": {
		"man": {
			"properties": {
				"name": {"type": "text"},
				"country": {"type": "keyword"},
				"age": {"type": "integer"},
				"birthday": {
					"type": "date",
					"format": "yyyy-MM-dd HH:mm:ss||yyyy-MM-dd||epoch_millis"
				}
			}
		},
		"women": {
		}
	}
}
```

里面设置了分片数, 备份数, 一个Index和两个type的结构映射.



##### 插入数据

> PUT http://47.94.210.157:9200/people/man/1
>
> 指定ID为1 

```
{
	"name": "伊布",
	"country": "瑞典",
	"age": 30,
	"birthday": "1988-12-12"
}
```

如果不指定ID, 会生成为随机字符串, 此时需要改为POST方式 `POST http://47.94.210.157:9200/people/man`



##### 修改数据

> POST http://47.94.210.157:9200/people/man/1/_update  -- 修改ID为1的文档

```json
{
	"doc": {
		"name": "梅西梅西很像很强"
	}
}
```

还可以通过脚本方式修改: 

```json
{ "script": "ctx._source.age += 10" }
```



**删除数据**

删除文档: `DELETE http://47.94.210.157:9200/people/man/1`



##### 查看数据

* 根据ID查询

> GET http://ip:9200/people/man/1?pretty=true

```
{
  "_index": "people",
  "_type": "man",
  "_id": "2",
  "_version": 1,
  "found": true,
  "_source": {
    "name": "伊布",
    "country": "瑞典",
    "age": 34,
    "birthday": "1972-12-12"
  }
}
```

`found`字段表示查到与否

  ​

* 查询全部

> GET http://ip:9200/Index/Type/_search

或带排序带分页的查询: 

> POST http://47.94.210.157:9200/people/_search

ES 默认 从0开始(from), 一次返回10条(size), 并按照_score字段倒排,  也可以自己指定

```
# 带排序带分页的查询
{
	"query": { "match_all": {} },
	"sort": [{
		"birthday": {"order": "desc"}
		}
	],
	"from": 0, 
	"size": 5
}
```


```json
{
  "took": 5,
  "timed_out": false,
  "_shards": {
    "total": 3,
    "successful": 3,
    "failed": 0
  },
  "hits": {
    "total": 3,
    "max_score": 1,
    "hits": [
      {
        "_index": "people",
        "_type": "man",
        "_id": "2",
        "_score": 1,
        "_source": {
          "name": "伊布",
          "country": "瑞典",
          "age": 34,
          "birthday": "1972-12-12"
        }
      },
      ....
      ....
    ]
  }
}
```

```
# 返回结果解释
- took: 耗时(单位毫秒)
- timed_out: 是否超时
- hits: 命中的记录数组 
  - total: 返回的记录数
  - max_score: 最高匹配度分数
  - hits: 记录数组
    - _score: 匹配度
```



* 关键字查询

```
{
	"query": {
		"match": {"name": "梅西"}
	}
}
```

**注意**: 这里是模糊匹配查询, 例如查询的值是"西2", 那么会查询所有记录name有"西"和name有"2"的.

关于查询多个关键字之间的逻辑运算: 

如果这样写, 两个关键字会被认为是 `or`的关系来查询

```
{
	"query": {
		"match": {"name": "西 布"}
	}
}
```

如果是`and`关系来搜索, 需要布尔查询

```
{
	"query": {
		"bool": {
			"must": [
				{"match": {"name": "西"}} ,
				{"match": {"name": "2"}} 
			]
		}
	}
}
```



##### 聚合查询

> POST  http://47.94.210.157:9200/people/_search

* 分组聚合

```
{
	"aggs": {
		"group_by_age": {
			"terms": {"field": "age"}
		}
	}
}
```

返回结果中, 除了有hits数组, 还有聚合查询的结果: 

```
"aggregations": {
	"group_by_age": {
	  "doc_count_error_upper_bound": 0,
	  "sum_other_doc_count": 0,
	  "buckets": [
		{
		  "key": 24,
		  "doc_count": 2
		},
		{
		  "key": 32,
		  "doc_count": 1
		}
	  ]
	}
}
```

支持多个聚合, 聚合结果也会返回多个:

```
{
	"aggs": {
		"group_by_age": {
			"terms": {"field": "age"}
		},
		"group_by_age": {
			"terms": {"field": "age"}
		}
	}
}
```

* 其他功能函数

```
{
	"aggs": {
		"tongji_age": {
			"stats": {"field": "age"}
		}
	}
}
```

`stats`指定计算字段, 返回结果包括了总数, 最小值, 最大值, 平均值和求和

```
 "aggregations": {
    "tongji_age": {
      "count": 3,
      "min": 24,
      "max": 32,
      "avg": 26.666666666666668,
      "sum": 80
    }
  }
```

也可指定某种类型的计算

```
{
	"aggs": {
		"tongji_age": {
			"sum": {"field": "age"}
		}
	}
}
```

返回结果

```
"aggregations": {
    "tongji_age": {
      "value": 80
    }
 }
```





#### 四: 高级查询

分为**子条件查询**和**复合条件**查询: 

类型: 

* **全文本查询**: 针对文本类型数据
* **字段级别查询**: 针对结构化数据, 如日期, 数字


##### 文本查询

* 模糊匹配

```
{
	"query": {
		"match": {"name": "西2"}
	}
}
```

* 短语匹配

```
{
	"query": {
		"match_phrase": {"name": "西2"}
	}
}
```

* 多个字段匹配

```
{
	"query": {
		"multi_match": {
			"query": "瑞典",
			"fields": ["name", "country"]
		}
	}
}
```



**语法查询**: 根据语法规则查询:

* 带有布尔逻辑的查询

```
{
	"query": {
		"query_string": {
			"query": "(西 AND 梅) OR 布"
		}
	}
}
```

* query_string 查询多个字段

```
{
	"query": {
		"query_string": {
			"query": "西梅 OR  瑞典",
			"fields": ["country", "name"]
		}
	}
}
```



##### 结构化数据查询

```
{
	"query": {"term": { "age": 24}}
}	
```

* 带范围的查询

```
{
	"query": {
		"range": {
			"birthday": {
				"gte": "1980-01-01",
				"lte": "now"
			}
		}
	}
}	
```





##### 子条件查询

Filter Context: 用来做数据过滤, 在查询过程中, 只判断该文档是否满足条件(y or not)

Filter和Query的区别? 

Filter要结合bool使用, 查询结果会放入缓存中, 速度较Query更快

```
{
	"query": {
		"bool": {
			"filter": {
				"term": { "age": 24 }
			}
		}
	}
}
```



##### 复合查询

* 固定分数查询

```
{
	"query": {
		"match": {
			"name": "梅西"
		}
	}
}
```

可以看到查询的结果, 每条数据的`_score`不同, 代表了与查询值的匹配程度的分数.

```
{
	"query": {
		"constant_score": {
			"filter": {
				"match": {
					"name": "梅西"
				}
			},
			"boost": 2
		}
	}
}
```

可以看到查询结果, 每条数据的`_score`都为2, 如果不指定`boost`则默认为1

* 布尔查询

```
{
	"query": {
		"bool": {
			"should": [
				{
					"match": {"name": "梅西"}
				},
				{
					"match": {"country": "阿"}
				}
			]
		}
	}	
}
```

这里两个match之间是或的逻辑关系. `should` 如果改为 `must` 代表与逻辑.

再加一层Filter, 只有age=32的能返回

```
{
	"query": {
		"bool": {
			"must": [
				{
					"match": {"name": "梅西"}
				},
				{
					"match": {"country": "阿根廷"}
				}
			],
			"filter": [{
				"term": {
					"age": 32
				}
			}]
		}
	}	
}
```

country=阿根廷的不返回:

```
{
	"query": {
		"bool": {
			"must_not": {
				"term": {
					"country": "阿根廷"
				}
			}
		}
	}
}
```





#### 五: Spring Boot 集成 Elastic Search



##### 版本参考

| Spring Boot Version (x) | Spring Data Elasticsearch Version (y) | Elasticsearch Version (z) |
| ----------------------- | ------------------------------------- | ------------------------- |
| x <= 1.3.5              | y <= 1.3.4                            | z <= 1.7.2*               |
| x >= 1.4.x              | 2.0.0 <=y < 5.0.0**                   | 2.0.0 <= z < 5.0.0**      |



| 服务器集群ES版本      | 5.5.2         |
| -------------- | ------------- |
| Spring boot    | 1.5.9.RELEASE |
| Elastic Search | 5.5.2         |
| log4j-core     | 2.7           |



##### 集成步骤

1. 引入Maven依赖:

   ```xml
   <properties>
   	<log4j-core.version>2.7</log4j-core.version>
   	<elasticsearch-version>5.5.2</elasticsearch-version>
   </properties>

   <dependency>
   	<groupId>org.elasticsearch.client</groupId>
   	<artifactId>transport</artifactId>
   	<version>${elasticsearch-version}</version>
   </dependency>

   <dependency>
   	<groupId>org.elasticsearch</groupId>
   	<artifactId>elasticsearch</artifactId>
   	<version>${elasticsearch-version}</version>
   </dependency>
   ```

   **注意**: 

   1. 虽然transport中也引入了elasticsearch, 但默认是`2.4.6`版本, 需要指定下版本`5.5.2`


   2. 也可以直接引入:

      ```xml
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-elasticsearch</artifactId>
      </dependency>
      ```

      但是`spring-boot-starter-data-elasticsearch`只支持到`2.4.x`版本的es. 

      如果使用`5.x.x`版本ES, 就用上面那种方式单独引入ES依赖. 

   ​

2. 添加配置类

   ```java
   @Configuration
   public class ElasticSearchConfig {

       /** 集群host */
       @Value("${spring.data.elasticsearch.cluster-nodes}")
       private String clusterNodes;

       /** 集群名称 */
       @Value("${spring.data.elasticsearch.cluster-name}")
       private String clusterName;

       @Bean
       public TransportClient client() throws UnknownHostException{

           InetSocketTransportAddress node = new InetSocketTransportAddress(
                   InetAddress.getByName(clusterNodes), 9300
           );

           Settings settings = Settings.builder().put("cluster.name", clusterName).build();

           TransportClient client = new PreBuiltTransportClient(settings);
           client.addTransportAddress(node);
           return client;
       }
   }
   ```

   `application.properties`中配置: 

   * spring.data.elasticsearch.cluster-nodes=xxx
   * spring.data.elasticsearch.cluster-name=xxx



##### 测试用例

简单的CRUL操作: 

> github: https://github.com/thank037/elasticsearch_demo.git

`@Link com.thank.elasticsearch.TestElasticSearchCRUD.java`



